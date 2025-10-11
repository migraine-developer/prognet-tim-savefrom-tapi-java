import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FileDownloader {

    private static final int BUFFER_SIZE = 32 * 1024; // 32 KB

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        private int counter = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "file-downloader-" + counter++);
            thread.setDaemon(true);
            return thread;
        }
    });

    private FileDownloader() {

    }

    public static DownloadHandle download(String url, Path destination, DownloadObserver observer) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(observer, "observer");

        DownloadTask task = new DownloadTask(url, destination, observer);
        Future<?> future = EXECUTOR.submit(task);
        task.attachFuture(future);
        return task;
    }

    public interface DownloadObserver {
        void onStarted(long totalBytes);

        void onProgress(long downloadedBytes, long totalBytes);

        void onCompleted(Path file);

        void onCancelled(Path partialFile);

        void onFailed(DownloadError error, Exception exception);
    }

    public interface DownloadHandle {
        void cancel();

        boolean isDone();

        boolean isCompletedSuccessfully();
    }

    private static final class DownloadTask implements Runnable, DownloadHandle {

        private final String url;
        private final Path destination;
        private final DownloadObserver observer;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private volatile boolean done;
        private volatile boolean succeeded;
        private Future<?> future;
        private volatile HttpURLConnection activeConnection;
        private volatile InputStream currentInput;
        private volatile OutputStream currentOutput;

        DownloadTask(String url, Path destination, DownloadObserver observer) {
            this.url = url;
            this.destination = destination;
            this.observer = observer;
        }

        @Override
        public void run() {
            boolean notifiedStart = false;
            try {
                if (destination.getParent() != null) {
                    Files.createDirectories(destination.getParent());
                }

                URL targetUrl = URI.create(url).toURL();
                HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
                this.activeConnection = connection;
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(15_000);
                connection.setReadTimeout(30_000);
                connection.connect();

                int statusCode = connection.getResponseCode();
                if (statusCode >= 400) {
                    throw new IOException("Server returned HTTP " + statusCode);
                }

                long totalBytes = connection.getContentLengthLong();
                observer.onStarted(totalBytes);
                notifiedStart = true;

                InputStream rawInput = connection.getInputStream();
                BufferedInputStream input = new BufferedInputStream(rawInput);
                this.currentInput = input;
                BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(destination,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                this.currentOutput = output;

                byte[] buffer = new byte[BUFFER_SIZE];
                long downloaded = 0;
                int read;

                while ((read = input.read(buffer)) != -1) {
                    if (cancelled.get() || Thread.currentThread().isInterrupted()) {
                        observer.onCancelled(destination);
                        cleanupPartialFile();
                        return;
                    }

                    output.write(buffer, 0, read);
                    downloaded += read;
                    observer.onProgress(downloaded, totalBytes);
                }

                if (cancelled.get() || Thread.currentThread().isInterrupted()) {
                    observer.onCancelled(destination);
                    cleanupPartialFile();
                    return;
                }

                output.flush();

                succeeded = true;
                observer.onCompleted(destination);
            } catch (AccessDeniedException ex) {
                observer.onFailed(DownloadError.PERMISSION_DENIED, ex);
                cleanupPartialFile();
            } catch (ClosedByInterruptException ex) {
                observer.onCancelled(destination);
                cleanupPartialFile();
            } catch (IOException ex) {
                if (cancelled.get()) {
                    observer.onCancelled(destination);
                    cleanupPartialFile();
                } else {
                    observer.onFailed(classifyError(ex), ex);
                    cleanupPartialFile();
                }
            } catch (SecurityException ex) {
                observer.onFailed(DownloadError.PERMISSION_DENIED, ex);
                cleanupPartialFile();
            } finally {
                done = true;
                closeResources();
                if (!notifiedStart && !cancelled.get() && !succeeded) {
                    observer.onFailed(DownloadError.GENERAL_FAILURE,
                            new IllegalStateException("Unduhan gagal dimulai."));
                }
            }
        }

        @Override
        public void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                closeResources();
                if (future != null) {
                    future.cancel(true);
                }
            }
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean isCompletedSuccessfully() {
            return done && succeeded;
        }

        void attachFuture(Future<?> future) {
            this.future = future;
        }

        private void cleanupPartialFile() {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {

            }
        }

        private void closeResources() {
            OutputStream output = currentOutput;
            currentOutput = null;
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }

            InputStream input = currentInput;
            currentInput = null;
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }

            HttpURLConnection connection = activeConnection;
            activeConnection = null;
            if (connection != null) {
                connection.disconnect();
            }
        }

        private DownloadError classifyError(IOException ex) {
            if (ex instanceof FileSystemException fse) {
                String reason = fse.getReason();
                if (reason != null) {
                    String lowerReason = reason.toLowerCase();
                    if (lowerReason.contains("space") || lowerReason.contains("disk")) {
                        return DownloadError.INSUFFICIENT_STORAGE;
                    }
                    if (lowerReason.contains("denied")) {
                        return DownloadError.PERMISSION_DENIED;
                    }
                }
            }

            String message = ex.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                if (lowerMessage.contains("space") || lowerMessage.contains("disk")) {
                    return DownloadError.INSUFFICIENT_STORAGE;
                }
                if (lowerMessage.contains("denied") || lowerMessage.contains("permission")) {
                    return DownloadError.PERMISSION_DENIED;
                }
                if (lowerMessage.contains("refused") || lowerMessage.contains("reset")
                        || lowerMessage.contains("timed out")) {
                    return DownloadError.NETWORK_INTERRUPTED;
                }
            }

            if (ex instanceof ConnectException || ex instanceof SocketException) {
                return DownloadError.NETWORK_INTERRUPTED;
            }

            return DownloadError.GENERAL_FAILURE;
        }
    }
}

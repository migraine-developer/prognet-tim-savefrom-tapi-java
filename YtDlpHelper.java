import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YtDlpHelper {

    private static final long PROCESS_KILL_TIMEOUT_MS = 2_000;
    private static final Pattern PCT = Pattern.compile("(\\d{1,3}(?:\\.\\d+)?)%");

    private YtDlpHelper() {
    }

    public interface Observer {
        void onStarted();

        void onProgress(int percent, String rawLine);

        void onTitleExtracted(String title);

        void onCompleted(Path producedFile);

        void onCancelled();

        void onFailed(String message);
    }

    public interface Handle {
        void cancel();

        boolean isDone();
    }

    public static boolean isYouTube(String url) {
        if (url == null) {
            return false;
        }
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("youtube.com") || u.contains("youtu.be");
    }

    public static String extractVideoTitle(String url) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(findYtDlpCommand());
            cmd.add("--get-title");
            cmd.add(url);
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String title = reader.readLine();
                if (title != null && !title.trim().isEmpty()) {
                    // Sanitize filename
                    return sanitizeFilename(title.trim());
                }
            }
            
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Failed to extract video title: " + e.getMessage());
        }
        
        return null;
    }
    
    private static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "video";
        }
        
        // Remove or replace invalid filename characters
        String sanitized = filename.replaceAll("[<>:\"/\\\\|?*]", "_")
                                  .replaceAll("\\s+", " ")
                                  .trim();
        
        // Limit length to avoid filesystem issues
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100).trim();
        }
        
        // Ensure it's not empty
        if (sanitized.isEmpty()) {
            sanitized = "video";
        }
        
        return sanitized;
    }

    public static Handle downloadMp4(String url, Path outDir, String baseName, Observer obs) {
        // If baseName is null or empty, extract title first
        if (baseName == null || baseName.trim().isEmpty()) {
            return downloadWithTitleExtraction(url, outDir, "mp4", obs);
        }
        
        List<String> cmd = new ArrayList<>();
        cmd.add(findYtDlpCommand());
        cmd.add("-f");
        cmd.add("bv*+ba/b");
        cmd.add("--merge-output-format");
        cmd.add("mp4");
        cmd.add("-o");
        cmd.add(outDir.resolve(baseName + ".%(ext)s").toString());
        cmd.add(url);
        Path produced = outDir.resolve(baseName + ".mp4");
        return runAsync(cmd, produced, obs);
    }

    public static Handle downloadMp3(String url, Path outDir, String baseName, Observer obs) {
        // If baseName is null or empty, extract title first
        if (baseName == null || baseName.trim().isEmpty()) {
            return downloadWithTitleExtraction(url, outDir, "mp3", obs);
        }
        
        List<String> cmd = new ArrayList<>();
        cmd.add(findYtDlpCommand());
        cmd.add("-x");
        cmd.add("--audio-format");
        cmd.add("mp3");
        cmd.add("--audio-quality");
        cmd.add("0");
        cmd.add("-o");
        cmd.add(outDir.resolve(baseName + ".%(ext)s").toString());
        cmd.add(url);
        Path produced = outDir.resolve(baseName + ".mp3");
        return runAsync(cmd, produced, obs);
    }

    private static Handle downloadWithTitleExtraction(String url, Path outDir, String format, Observer obs) {
        final ProcessHolder holder = new ProcessHolder();
        Thread t = new Thread(() -> {
            holder.worker = Thread.currentThread();
            if (obs != null) {
                obs.onStarted();
            }

            try {
                // First, extract the title
                String title = extractVideoTitle(url);
                if (title == null) {
                    title = "video_" + System.currentTimeMillis();
                }
                
                if (obs != null) {
                    obs.onTitleExtracted(title);
                }

                // Now download with the extracted title
                List<String> cmd = new ArrayList<>();
                cmd.add(findYtDlpCommand());
                
                if ("mp4".equals(format)) {
                    cmd.add("-f");
                    cmd.add("bv*+ba/b");
                    cmd.add("--merge-output-format");
                    cmd.add("mp4");
                } else {
                    cmd.add("-x");
                    cmd.add("--audio-format");
                    cmd.add("mp3");
                    cmd.add("--audio-quality");
                    cmd.add("0");
                }
                
                cmd.add("-o");
                cmd.add(outDir.resolve(title + ".%(ext)s").toString());
                cmd.add(url);
                
                Path producedFile = outDir.resolve(title + "." + format);
                
                // Start the actual download
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                
                Process process = pb.start();
                holder.process = process;
                holder.processInput = process.getInputStream();
                holder.processOutput = process.getOutputStream();
                holder.reader = new BufferedReader(new InputStreamReader(holder.processInput));

                String line;
                while ((line = holder.reader.readLine()) != null) {
                    int percent = parsePercent(line);
                    if (obs != null) {
                        obs.onProgress(percent, line);
                    }
                }

                int code = process.waitFor();
                holder.done = true;
                if (holder.cancelled) {
                    cleanupPartialOutput(producedFile);
                    if (obs != null) {
                        obs.onCancelled();
                    }
                } else if (code == 0) {
                    if (obs != null) {
                        obs.onCompleted(producedFile);
                    }
                } else if (obs != null) {
                    obs.onFailed("yt-dlp gagal (exit code " + code + ")");
                }
                
            } catch (IOException e) {
                holder.done = true;
                if (holder.cancelled) {
                    if (obs != null) {
                        obs.onCancelled();
                    }
                } else if (obs != null) {
                    String msg = e.getMessage();
                    if (msg != null && msg.toLowerCase(Locale.ROOT).contains("cannot run program")) {
                        obs.onFailed("yt-dlp tidak ditemukan di PATH. Pastikan instalasi benar.");
                    } else {
                        obs.onFailed("Gagal menjalankan yt-dlp: " + e.getMessage());
                    }
                }
            } catch (InterruptedException ie) {
                holder.done = true;
                if (obs != null) {
                    obs.onCancelled();
                }
                Thread.currentThread().interrupt();
            } finally {
                closeQuietly(holder.reader);
                closeQuietly(holder.processInput);
                closeQuietly(holder.processOutput);
            }
        }, "yt-dlp-title-extractor");
        t.setDaemon(true);
        t.start();

        return new Handle() {
            @Override
            public void cancel() {
                holder.cancelled = true;
                Process process = holder.process;
                if (process != null) {
                    terminateProcessTree(process);
                    try {
                        if (!process.waitFor(PROCESS_KILL_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                            process.destroyForcibly();
                            process.waitFor(PROCESS_KILL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        }
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        process.destroyForcibly();
                    }
                }

                closeQuietly(holder.reader);
                closeQuietly(holder.processInput);
                closeQuietly(holder.processOutput);

                if (!holder.done) {
                    holder.done = true;
                }

                Thread worker = holder.worker;
                if (worker != null) {
                    worker.interrupt();
                }
            }

            @Override
            public boolean isDone() {
                return holder.done;
            }
        };
    }

    private static Handle runAsync(List<String> command, Path producedFile, Observer obs) {
        final ProcessHolder holder = new ProcessHolder();
        Thread t = new Thread(() -> {
            holder.worker = Thread.currentThread();
            if (obs != null) {
                obs.onStarted();
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            try {
                Process process = pb.start();
                holder.process = process;
                holder.processInput = process.getInputStream();
                holder.processOutput = process.getOutputStream();
                holder.reader = new BufferedReader(new InputStreamReader(holder.processInput));

                String line;
                while ((line = holder.reader.readLine()) != null) {
                    int percent = parsePercent(line);
                    if (obs != null) {
                        obs.onProgress(percent, line);
                    }
                }

                int code = process.waitFor();
                holder.done = true;
                if (holder.cancelled) {
                    cleanupPartialOutput(producedFile);
                    if (obs != null) {
                        obs.onCancelled();
                    }
                } else if (code == 0) {
                    if (obs != null) {
                        obs.onCompleted(producedFile);
                    }
                } else if (obs != null) {
                    obs.onFailed("yt-dlp gagal (exit code " + code + ")");
                }
            } catch (IOException e) {
                holder.done = true;
                if (holder.cancelled) {
                    cleanupPartialOutput(producedFile);
                    if (obs != null) {
                        obs.onCancelled();
                    }
                } else if (obs != null) {
                    String msg = e.getMessage();
                    if (msg != null && msg.toLowerCase(Locale.ROOT).contains("cannot run program")) {
                        obs.onFailed("yt-dlp tidak ditemukan di PATH. Pastikan instalasi benar.");
                    } else {
                        obs.onFailed("Gagal menjalankan yt-dlp: " + e.getMessage());
                    }
                }
            } catch (InterruptedException ie) {
                holder.done = true;
                cleanupPartialOutput(producedFile);
                if (obs != null) {
                    obs.onCancelled();
                }
                Thread.currentThread().interrupt();
            } finally {
                closeQuietly(holder.reader);
                closeQuietly(holder.processInput);
                closeQuietly(holder.processOutput);
            }
        }, "yt-dlp-runner");
        t.setDaemon(true);
        t.start();

        return new Handle() {
            @Override
            public void cancel() {
                holder.cancelled = true;
                Process process = holder.process;
                if (process != null) {
                    terminateProcessTree(process);
                    try {
                        if (!process.waitFor(PROCESS_KILL_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                            process.destroyForcibly();
                            process.waitFor(PROCESS_KILL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        }
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        process.destroyForcibly();
                    }
                }

                closeQuietly(holder.reader);
                closeQuietly(holder.processInput);
                closeQuietly(holder.processOutput);

                if (!holder.done) {
                    cleanupPartialOutput(producedFile);
                    holder.done = true;
                }

                Thread worker = holder.worker;
                if (worker != null) {
                    worker.interrupt();
                }
            }

            @Override
            public boolean isDone() {
                return holder.done;
            }
        };
    }

    private static String findYtDlpCommand() {
        String bundledPath = getBundledYtDlpPath();
        if (bundledPath != null) {
            return bundledPath;
        }

        try {
            return downloadYtDlpIfNeeded();
        } catch (Exception e) {
            System.err.println("Gagal download yt-dlp: " + e.getMessage());
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            return os.contains("win") ? "yt-dlp.exe" : "yt-dlp";
        }
    }

    private static String getBundledYtDlpPath() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String executable = os.contains("win") ? "yt-dlp.exe" : "yt-dlp";

        try {
            String classDir = System.getProperty("user.dir");
            File bundled = new File(classDir, "bin/" + executable);
            if (bundled.exists() && bundled.canExecute()) {
                return bundled.getAbsolutePath();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String downloadYtDlpIfNeeded() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String executable = os.contains("win") ? "yt-dlp.exe" : "yt-dlp";

        File binDir = new File("bin");
        File ytDlpFile = new File(binDir, executable);

        if (!ytDlpFile.exists()) {
            System.out.println("yt-dlp tidak ditemukan. Mengunduh dari GitHub...");

            if (!binDir.exists()) {
                binDir.mkdirs();
            }

            String downloadUrl = os.contains("win")
                    ? "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
                    : "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";

            downloadFileSync(downloadUrl, ytDlpFile.toPath());

            if (!os.contains("win")) {
                ytDlpFile.setExecutable(true);
            }

            System.out.println("yt-dlp berhasil diunduh ke: " + ytDlpFile.getAbsolutePath());
        }

        return ytDlpFile.getAbsolutePath();
    }

    private static void downloadFileSync(String url, Path destination) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> errorRef = new AtomicReference<>();

        FileDownloader.DownloadObserver observer = new FileDownloader.DownloadObserver() {
            @Override
            public void onStarted(long totalBytes) {
                System.out.println("Memulai unduhan yt-dlp (" + formatBytes(totalBytes) + ")...");
            }

            @Override
            public void onProgress(long downloadedBytes, long totalBytes) {
                if (totalBytes > 0) {
                    int percent = (int) ((downloadedBytes * 100) / totalBytes);
                    if (percent % 10 == 0) {
                        System.out.println("Unduhan: " + percent + "% (" + formatBytes(downloadedBytes) + " / "
                                + formatBytes(totalBytes) + ")");
                    }
                }
            }

            @Override
            public void onCompleted(Path file) {
                System.out.println("Unduhan yt-dlp selesai!");
                latch.countDown();
            }

            @Override
            public void onCancelled(Path partialFile) {
                errorRef.set(new Exception("Unduhan dibatalkan"));
                latch.countDown();
            }

            @Override
            public void onFailed(DownloadError error, Exception exception) {
                errorRef.set(new Exception("Gagal mengunduh yt-dlp: " + error.toUserMessage(), exception));
                latch.countDown();
            }
        };

        FileDownloader.download(url, destination, observer);

        latch.await();

        Exception error = errorRef.get();
        if (error != null) {
            throw error;
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "Unknown";
        }
        String[] units = { "B", "KB", "MB", "GB" };
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex]);
    }

    private static int parsePercent(String line) {
        if (line == null) {
            return -1;
        }
        if (!line.contains("%")) {
            return -1;
        }
        Matcher matcher = PCT.matcher(line);
        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                int pct = (int) Math.round(value);
                if (pct < 0) {
                    pct = 0;
                }
                if (pct > 100) {
                    pct = 100;
                }
                return pct;
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static void cleanupPartialOutput(Path producedFile) {
        if (producedFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(producedFile);
        } catch (IOException ignored) {
        }

        String fileName = producedFile.getFileName().toString();
        Path parent = producedFile.getParent();
        if (parent == null) {
            return;
        }

        Path partFile = parent.resolve(fileName + ".part");
        try {
            Files.deleteIfExists(partFile);
        } catch (IOException ignored) {
        }
    }

    private static void terminateProcessTree(Process process) {
        if (process == null) {
            return;
        }

        process.descendants().forEach(ph -> {
            try {
                if (!ph.isAlive()) {
                    return;
                }

                ph.destroy();
                try {
                    ph.onExit().get(PROCESS_KILL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                } catch (ExecutionException | TimeoutException ex) {
                    ph.destroyForcibly();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                ph.destroyForcibly();
            }
        });

        process.destroy();
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private static final class ProcessHolder {
        volatile Process process;
        volatile boolean cancelled;
        volatile boolean done;
        volatile Thread worker;
        volatile BufferedReader reader;
        volatile InputStream processInput;
        volatile OutputStream processOutput;
    }
}
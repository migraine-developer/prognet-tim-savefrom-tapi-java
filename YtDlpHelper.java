import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YtDlpHelper {

    private YtDlpHelper() {}

    public interface Observer {
        void onStarted();                                    
        void onProgress(int percent, String rawLine);        
        void onCompleted(Path producedFile);                 
        void onCancelled();                                  
        void onFailed(String message);                       
    }

    public interface Handle {
        void cancel();
        boolean isDone();
    }

    public static boolean isYouTube(String url) {
        if (url == null) return false;
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("youtube.com") || u.contains("youtu.be");
    }

    public static Handle downloadMp4(String url, Path outDir, String baseName, Observer obs) {
        List<String> cmd = new ArrayList<>();
        cmd.add(findYtDlpCommand());
        cmd.add("-f"); cmd.add("bv*+ba/b");                       
        cmd.add("--merge-output-format"); cmd.add("mp4");         
        cmd.add("-o"); cmd.add(outDir.resolve(baseName + ".%(ext)s").toString());
        cmd.add(url);
        Path produced = outDir.resolve(baseName + ".mp4");
        return runAsync(cmd, produced, obs);
    }

    public static Handle downloadMp3(String url, Path outDir, String baseName, Observer obs) {
        List<String> cmd = new ArrayList<>();
        cmd.add(findYtDlpCommand());
        cmd.add("-x");                                            
        cmd.add("--audio-format"); cmd.add("mp3");
        cmd.add("--audio-quality"); cmd.add("0");                 
        cmd.add("-o"); cmd.add(outDir.resolve(baseName + ".%(ext)s").toString());
        cmd.add(url);
        Path produced = outDir.resolve(baseName + ".mp3");
        return runAsync(cmd, produced, obs);
    }

    private static Handle runAsync(List<String> command, Path producedFile, Observer obs) {
        final ProcessHolder holder = new ProcessHolder();
        Thread t = new Thread(() -> {
            if (obs != null) obs.onStarted();
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            try {
                Process p = pb.start();
                holder.process = p;

                
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        int percent = parsePercent(line);
                        if (obs != null) obs.onProgress(percent, line);
                    }
                }

                int code = p.waitFor();
                holder.done = true;
                if (holder.cancelled) {
                    if (obs != null) obs.onCancelled();
                } else if (code == 0) {
                    if (obs != null) obs.onCompleted(producedFile);
                } else {
                    if (obs != null) obs.onFailed("yt-dlp gagal (exit code " + code + ")");
                }
            } catch (IOException e) {
                holder.done = true;
                if (holder.cancelled) {
                    if (obs != null) obs.onCancelled();
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
                if (obs != null) obs.onCancelled();
                Thread.currentThread().interrupt();
            }
        }, "yt-dlp-runner");
        t.setDaemon(true);
        t.start();

        return new Handle() {
            @Override
            public void cancel() {
                holder.cancelled = true;
                Process p = holder.process;
                if (p != null) {
                    p.destroy();
                }
            }

            @Override
            public boolean isDone() {
                return holder.done;
            }
        };
    }

    private static String findYtDlpCommand() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        
        return os.contains("win") ? "yt-dlp.exe" : "yt-dlp";
    }

    private static final Pattern PCT = Pattern.compile("(\\d{1,3}(?:\\.\\d+)?)%");

    private static int parsePercent(String line) {
        if (line == null) return -1;
        if (!line.contains("%")) return -1;
        Matcher m = PCT.matcher(line);
        if (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                int pct = (int)Math.round(v);
                if (pct < 0) pct = 0;
                if (pct > 100) pct = 100;
                return pct;
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static final class ProcessHolder {
        volatile Process process;
        volatile boolean cancelled;
        volatile boolean done;
    }
}

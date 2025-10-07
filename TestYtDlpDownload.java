public class TestYtDlpDownload {
    public static void main(String[] args) {
        System.out.println("Testing yt-dlp auto-download functionality...");

        // Test URL detection
        String youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String regularUrl = "https://www.example.com/file.pdf";

        System.out.println("YouTube URL detected: " + YtDlpHelper.isYouTube(youtubeUrl));
        System.out.println("Regular URL detected as YouTube: " + YtDlpHelper.isYouTube(regularUrl));

        System.out.println("Test completed!");
    }
}
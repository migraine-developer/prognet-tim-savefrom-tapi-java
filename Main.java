public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DownloaderGUI downloaderGUI = new DownloaderGUI();
            downloaderGUI.setVisible(true);
        });
    }
}

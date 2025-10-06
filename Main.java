public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DownloaderGUI downloaderGUI = new DownloaderGUI();

            downloaderGUI.getDownloadButton().addActionListener(e -> {
                ProgressGUI progressDialog = new ProgressGUI(downloaderGUI);
                progressDialog.setVisible(true);
            });

            downloaderGUI.setVisible(true);
        });
    }
}

//GUI Utama

import javax.swing.*;
import java.awt.*;

public class DownloaderGUI extends JFrame {

    private JTextField linkField;
    private JTextField pathField;
    private JTextField nameField;
    private JComboBox<String> formatBox;
    private JButton downloadButton;

    public DownloaderGUI() {
        setTitle("Downloader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Komponen
        JLabel linkLabel = new JLabel("Download Link");
        JLabel pathLabel = new JLabel("File Path");
        JLabel nameLabel = new JLabel("File Name");
        JLabel formatLabel = new JLabel("File Format");

        linkField = new JTextField(20);
        pathField = new JTextField(20);
        nameField = new JTextField(20);
        formatBox = new JComboBox<>(new String[]{"mp4", "mp3"});
        downloadButton = new JButton("Download");

        // Baris 1
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(linkLabel, gbc);
        gbc.gridx = 1;
        panel.add(linkField, gbc);

        // Baris 2
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(pathLabel, gbc);
        gbc.gridx = 1;
        panel.add(pathField, gbc);

        // Baris 3
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Baris 4
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(formatLabel, gbc);
        gbc.gridx = 1;
        panel.add(formatBox, gbc);

        // Baris 5 (tombol)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(downloadButton, gbc);

        add(panel);
    }

    // Getter biar kelas lain bisa ambil input pengguna nanti
    public JTextField getLinkField() { return linkField; }
    public JTextField getPathField() { return pathField; }
    public JTextField getNameField() { return nameField; }
    public JComboBox<String> getFormatBox() { return formatBox; }
    public JButton getDownloadButton() { return downloadButton; }
}

//GUI Utama

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class DownloaderGUI extends JFrame {

    private static final Color CARD_BACKGROUND = new Color(255, 255, 255, 255);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color FIELD_BACKGROUND = new Color(248, 250, 253);
    private static final Color FIELD_BORDER = new Color(225, 229, 240);
    private static final Color BUTTON_COLOR = new Color(92, 134, 255);
    private static final Color BUTTON_COLOR_HOVER = new Color(75, 112, 224);
    private static final Color BROWSE_BUTTON_BG = new Color(200, 30, 45);
    private static final Color BROWSE_BUTTON_HOVER = new Color(220, 54, 70);
    private static final Color BROWSE_BUTTON_TEXT = Color.WHITE;
    private static final Color INFO_PANEL_BACKGROUND = new Color(255, 255, 255, 180);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font INFO_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Gunakan look and feel default jika gagal.
        }
    }

    private JTextField linkField;
    private JTextField pathField;
    private JTextField nameField;
    private JComboBox<String> formatBox;
    private JButton downloadButton;
    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    public DownloaderGUI() {
        setTitle("SaveFrom Tapi Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 620);
        setMinimumSize(new Dimension(780, 480));
        setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(Color.WHITE);
        backgroundPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        RoundedPanel cardPanel = new RoundedPanel(26);
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBackground(CARD_BACKGROUND);
        cardPanel.setBorder(new EmptyBorder(32, 38, 36, 38));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weightx = 1;
        cardGbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("Youtube Content Downloader", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);
        cardGbc.gridy = 0;
        cardGbc.insets = new Insets(0, 0, 12, 0);
        cardPanel.add(titleLabel, cardGbc);

        JLabel subtitleLabel = new JLabel("Paste link, tentukan lokasi file, lalu tekan Download.",
                SwingConstants.CENTER);
        subtitleLabel.setFont(BASE_FONT);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        cardGbc.gridy = 1;
        cardGbc.insets = new Insets(0, 0, 18, 0);
        cardPanel.add(subtitleLabel, cardGbc);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(235, 239, 247));
        separator.setBackground(new Color(235, 239, 247));
        cardGbc.gridy = 2;
        cardGbc.insets = new Insets(0, 0, 24, 0);
        cardPanel.add(separator, cardGbc);

        linkField = new JTextField(20);
        pathField = new JTextField(20);
        nameField = new JTextField(20);
        formatBox = new JComboBox<>(new String[] { "mp4", "mp3" });
        downloadButton = new RoundedButton("Download", 24);

        styleTextField(linkField, "Tempelkan URL dari YouTube, Instagram, dll.");
        styleTextField(pathField, "Pilih folder tujuan atau ketik secara manual");
        styleTextField(nameField, "Nama file tanpa format");
        styleComboBox(formatBox);
        styleButton(downloadButton);

        historyTableModel = createHistoryTableModel();
        historyTable = createHistoryTable(historyTableModel);
        seedHistoryPreview();

        cardGbc.gridy = 3;
        cardGbc.gridx = 0;
        cardGbc.gridwidth = 1;
        cardGbc.weightx = 0.58;
        cardGbc.weighty = 1;
        cardGbc.insets = new Insets(0, 0, 0, 24);
        cardGbc.fill = GridBagConstraints.BOTH;
        cardPanel.add(createFormPanel(), cardGbc);

        cardGbc.gridx = 1;
        cardGbc.weightx = 0.42;
        cardGbc.insets = new Insets(-12, 0, 0, 0);
        cardPanel.add(createHistoryPanel(), cardGbc);

        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.anchor = GridBagConstraints.CENTER;
        outerGbc.fill = GridBagConstraints.NONE;
        backgroundPanel.add(cardPanel, outerGbc);

        setContentPane(backgroundPanel);
    }

    // Getter biar kelas lain bisa ambil input pengguna nanti
    public JTextField getLinkField() {
        return linkField;
    }

    public JTextField getPathField() {
        return pathField;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JComboBox<String> getFormatBox() {
        return formatBox;
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }

    private void styleFieldLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_PRIMARY);
    }

    private void styleTextField(JTextField field, String tooltip) {
        field.setFont(BASE_FONT);
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        field.setToolTipText(tooltip);
        field.setPreferredSize(new Dimension(360, 46));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(BASE_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        comboBox.setPreferredSize(new Dimension(180, 42));
        comboBox.setMaximumRowCount(5);
        comboBox.setFocusable(false);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 36, 12, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        boolean rounded = button instanceof RoundedButton;
        button.setOpaque(!rounded);
        button.setContentAreaFilled(!rounded);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_COLOR_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });
    }

    private void styleBrowseButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(BROWSE_BUTTON_BG);
        button.setForeground(BROWSE_BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BROWSE_BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BROWSE_BUTTON_BG);
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel linkLabel = new JLabel("Download Link");
        JLabel pathLabel = new JLabel("File Path");
        JLabel nameLabel = new JLabel("File Name");
        JLabel formatLabel = new JLabel("File Format");

        styleFieldLabel(linkLabel);
        styleFieldLabel(pathLabel);
        styleFieldLabel(nameLabel);
        styleFieldLabel(formatLabel);

        JButton browseButton = createBrowseButton();

        JPanel pathFieldWrapper = new JPanel(new BorderLayout(8, 0));
        pathFieldWrapper.setOpaque(false);
        pathFieldWrapper.add(pathField, BorderLayout.CENTER);
        pathFieldWrapper.add(browseButton, BorderLayout.EAST);

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.gridx = 0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 1;

        int row = 0;

        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(linkLabel, formGbc);
        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(linkField, formGbc);

        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(pathLabel, formGbc);
        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(pathFieldWrapper, formGbc);

        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(nameLabel, formGbc);
        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(nameField, formGbc);

        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(formatLabel, formGbc);
        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(formatBox, formGbc);

        // formGbc.gridy = row++;
        // formGbc.insets = new Insets(0, 0, 4, 0);
        // formPanel.add(formatLabel, formGbc);
        // formGbc.gridy = row++;
        // formGbc.insets = new Insets(0, 0, 20, 0);
        // formPanel.add(formatBox, formGbc);

        JLabel hintLabel = new JLabel("Tip: Pastikan folder tujuan memiliki ruang yang cukup.");
        hintLabel.setFont(BASE_FONT);
        hintLabel.setForeground(TEXT_SECONDARY);
        hintLabel.setHorizontalAlignment(SwingConstants.LEFT);
        formGbc.gridy = row++;
        formGbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(hintLabel, formGbc);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(downloadButton);
        formGbc.gridy = row;
        formGbc.insets = new Insets(6, 0, 0, 0);
        formPanel.add(buttonWrapper, formGbc);

        return formPanel;
    }

    private JButton createBrowseButton() {
        JButton browseButton = new JButton("Browse");
        styleBrowseButton(browseButton);
        browseButton.addActionListener(e -> openDirectoryChooser());
        return browseButton;
    }

    private void openDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Folder Tujuan");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        String currentPath = pathField.getText().trim();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.exists()) {
                if (currentFile.isDirectory()) {
                    chooser.setCurrentDirectory(currentFile);
                    chooser.setSelectedFile(currentFile);
                } else {
                    chooser.setCurrentDirectory(currentFile.getParentFile());
                }
            }
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            pathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private JPanel createHistoryPanel() {
        RoundedPanel historyPanel = new RoundedPanel(22);
        historyPanel.setBackground(INFO_PANEL_BACKGROUND);
        historyPanel.setLayout(new BorderLayout(0, 16));
        historyPanel.setBorder(new EmptyBorder(0, 28, 28, 28));
        historyPanel.setPreferredSize(new Dimension(320, 0));

        JLabel titleLabel = new JLabel("Riwayat Unduhan");
        titleLabel.setFont(LABEL_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        JLabel footerLabel = new JLabel("Data terbaru akan muncul setelah proses unduh selesai.");
        footerLabel.setFont(BASE_FONT);
        footerLabel.setForeground(TEXT_SECONDARY);

        historyPanel.add(titleLabel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(footerLabel, BorderLayout.SOUTH);

        return historyPanel;
    }

    private DefaultTableModel createHistoryTableModel() {
        return new DefaultTableModel(new Object[] { "Judul", "Format", "Tujuan", "Waktu", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createHistoryTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setOpaque(false);
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setFont(BASE_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(new Color(255, 255, 255, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(238, 242, 248));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(new Color(240, 242, 247));
        header.setBorder(new LineBorder(new Color(222, 226, 233), 1, false));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer pathRenderer = new DefaultTableCellRenderer();
        pathRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(pathRenderer);

        DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer();
        timeRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(timeRenderer);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer();
        statusRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(statusRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);

        return table;
    }

    private void seedHistoryPreview() {
        if (historyTableModel.getRowCount() == 0) {
            historyTableModel.addRow(new Object[] { "Belum ada riwayat", "-", "-", "-" });
        }
    }

    public void addHistoryEntry(String title, String format, String destination, String completedAt) {
        if (historyTableModel.getRowCount() == 1 && "Belum ada riwayat".equals(historyTableModel.getValueAt(0, 0))) {
            historyTableModel.setRowCount(0);
        }
        historyTableModel.addRow(new Object[] { title, format, destination, completedAt });
    }
}

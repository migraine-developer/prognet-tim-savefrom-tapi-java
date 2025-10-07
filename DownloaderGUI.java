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
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

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
    private static final Color STATUS_SUCCESS = new Color(46, 125, 50);
    private static final Color STATUS_ERROR = new Color(200, 30, 45);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

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
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JLabel statusLabel;
    private JButton cancelButton;
    private JButton openFileButton;
    private JButton openFolderButton;
    private JPanel progressPanel;
    private FileDownloader.DownloadHandle currentDownload;
    private long lastTotalBytes = -1L;
    private String currentDownloadName;
    private String currentDownloadFormat;
    private Path currentDestination;

    public DownloaderGUI() {
        setTitle("SaveFrom Tapi Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(1100, 720));
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
        cancelButton = new JButton("Cancel");
        progressBar = new JProgressBar(0, 100);
        progressLabel = new JLabel(" ");
        statusLabel = new JLabel(" ");
        openFileButton = new JButton("Buka File");
        openFolderButton = new JButton("Buka Folder");

        styleTextField(linkField, "Tempelkan URL dari YouTube, Instagram, dll.");
        styleTextField(pathField, "Pilih folder tujuan atau ketik secara manual");
        styleTextField(nameField, "Nama file tanpa format");
        styleComboBox(formatBox);
        styleButton(downloadButton);
        styleCancelButton(cancelButton);
        styleHistoryActionButton(openFileButton);
        styleHistoryActionButton(openFolderButton);
        configureProgressComponents();

        historyTableModel = createHistoryTableModel();
        historyTable = createHistoryTable(historyTableModel);
        seedHistoryPreview();
        historyTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateHistoryActionState();
            }
        });
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedFile();
                }
            }
        });

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

        downloadButton.addActionListener(event -> {
            Objects.requireNonNull(event);
            startDownload();
        });
        cancelButton.addActionListener(event -> {
            Objects.requireNonNull(event);
            cancelActiveDownload();
        });
        openFileButton.addActionListener(event -> {
            Objects.requireNonNull(event);
            openSelectedFile();
        });
        openFolderButton.addActionListener(event -> {
            Objects.requireNonNull(event);
            openSelectedFolder();
        });

        updateHistoryActionState();
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

    private void styleCancelButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(230, 234, 240));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 28, 12, 28));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(214, 220, 228));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(230, 234, 240));
            }
        });
        button.setEnabled(false);
    }

    private void styleHistoryActionButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(242, 245, 250));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(226, 231, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(242, 245, 250));
            }
        });
        button.setEnabled(false);
    }

    private void configureProgressComponents() {
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(360, 20));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        progressLabel.setFont(BASE_FONT);
        progressLabel.setForeground(TEXT_SECONDARY);
        progressLabel.setVisible(false);

        statusLabel.setFont(BASE_FONT);
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setVisible(false);
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
        buttonWrapper.add(Box.createHorizontalStrut(12));
        buttonWrapper.add(cancelButton);
        formGbc.gridy = row;
        formGbc.insets = new Insets(6, 0, 0, 0);
        formPanel.add(buttonWrapper, formGbc);

        progressPanel = new JPanel();
        progressPanel.setOpaque(true);
        progressPanel.setBackground(new Color(247, 249, 253));
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 229, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(6));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(6));
        progressPanel.add(statusLabel);

        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressPanel.setVisible(false);

        formGbc.gridy = ++row;
        formGbc.insets = new Insets(16, 0, 0, 0);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(progressPanel, formGbc);

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        formGbc.gridy = ++row;
        formGbc.weighty = 1;
        formGbc.fill = GridBagConstraints.BOTH;
        formPanel.add(filler, formGbc);

        return formPanel;
    }

    private JButton createBrowseButton() {
        JButton browseButton = new JButton("Browse");
        styleBrowseButton(browseButton);
        browseButton.addActionListener(event -> {
            Objects.requireNonNull(event);
            openDirectoryChooser();
        });
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
        historyPanel.setLayout(new BorderLayout(0, 12));
        historyPanel.setBorder(new EmptyBorder(16, 28, 24, 28));
        historyPanel.setPreferredSize(new Dimension(340, 0));

        JLabel titleLabel = new JLabel("Riwayat Unduhan");
        titleLabel.setFont(LABEL_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 4, 0, 4));

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        JLabel footerLabel = new JLabel("Pilih riwayat untuk membuka file atau lokasinya.");
        footerLabel.setFont(BASE_FONT);
        footerLabel.setForeground(TEXT_SECONDARY);

        JPanel footerWrapper = new JPanel();
        footerWrapper.setLayout(new BorderLayout(0, 8));
        footerWrapper.setOpaque(false);
        footerWrapper.add(footerLabel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(openFolderButton);
        actionPanel.add(openFileButton);
        footerWrapper.add(actionPanel, BorderLayout.SOUTH);

        historyPanel.add(titleLabel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(footerWrapper, BorderLayout.SOUTH);

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
        table.setRowHeight(34);
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
        pathRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        pathRenderer.setBorder(new EmptyBorder(0, 12, 0, 12));
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

        int visibleRows = 6;
        int preferredHeight = table.getRowHeight() * visibleRows + table.getTableHeader().getPreferredSize().height;
        table.setPreferredScrollableViewportSize(new Dimension(320, preferredHeight));

        return table;
    }

    private void startDownload() {
        if (currentDownload != null && !currentDownload.isDone()) {
            showStatus("Masih ada unduhan yang berjalan.", TEXT_SECONDARY);
            return;
        }

        String url = linkField.getText().trim();
        String directoryText = pathField.getText().trim();
        String fileName = nameField.getText().trim();
        String format = (String) formatBox.getSelectedItem();

        if (url.isEmpty()) {
            showStatus("Download Link belum diisi.", STATUS_ERROR);
            return;
        }
        if (directoryText.isEmpty()) {
            showStatus("File Path belum diisi.", STATUS_ERROR);
            return;
        }
        if (fileName.isEmpty()) {
            showStatus("File Name belum diisi.", STATUS_ERROR);
            return;
        }

        Path directory;
        try {
            directory = Paths.get(directoryText);
        } catch (InvalidPathException ex) {
            showStatus("Path tujuan tidak valid.", STATUS_ERROR);
            return;
        }

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            showStatus("Folder tujuan tidak ditemukan.", STATUS_ERROR);
            return;
        }

        try {
            if (!Files.isWritable(directory)) {
                showStatus("Tidak memiliki izin menulis di folder tujuan.", STATUS_ERROR);
                return;
            }
        } catch (SecurityException ex) {
            showStatus("Tidak dapat mengakses folder tujuan karena izin.", STATUS_ERROR);
            return;
        }

        String extension = (format != null && !format.isBlank()) ? "." + format.trim() : "";
        Path destination = directory.resolve(fileName + extension);

        try {
            if (Files.exists(destination)) {
                showStatus("File tujuan sudah ada. Gunakan nama lain.", STATUS_ERROR);
                return;
            }
        } catch (SecurityException ex) {
            showStatus("Tidak dapat memeriksa file tujuan karena izin.", STATUS_ERROR);
            return;
        }

        currentDownloadName = fileName;
        currentDownloadFormat = format != null ? format : "";
        currentDestination = destination;
        lastTotalBytes = -1L;

        prepareUiForDownload();

        FileDownloader.DownloadObserver observer = new FileDownloader.DownloadObserver() {
            @Override
            public void onStarted(long totalBytes) {
                SwingUtilities.invokeLater(() -> handleDownloadStarted(totalBytes));
            }

            @Override
            public void onProgress(long downloadedBytes, long totalBytes) {
                SwingUtilities.invokeLater(() -> updateDownloadProgress(downloadedBytes, totalBytes));
            }

            @Override
            public void onCompleted(Path file) {
                SwingUtilities.invokeLater(() -> handleDownloadCompleted(file));
            }

            @Override
            public void onCancelled(Path partialFile) {
                SwingUtilities.invokeLater(() -> handleDownloadCancelled(partialFile));
            }

            @Override
            public void onFailed(DownloadError error, Exception exception) {
                SwingUtilities.invokeLater(() -> handleDownloadFailed(error));
            }
        };

        currentDownload = FileDownloader.download(url, destination, observer);
    }

    private void prepareUiForDownload() {
        setInputsEnabled(false);
        progressPanel.setVisible(true);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        progressBar.setString(null);
        progressLabel.setText("Menunggu respon server...");
        showStatus("Menghubungkan ke server...", TEXT_SECONDARY);
        cancelButton.setEnabled(true);
    }

    private void cancelActiveDownload() {
        if (currentDownload != null && !currentDownload.isDone()) {
            cancelButton.setEnabled(false);
            showStatus("Membatalkan unduhan...", TEXT_SECONDARY);
            currentDownload.cancel();
        }
    }

    private void handleDownloadStarted(long totalBytes) {
        lastTotalBytes = totalBytes;
        progressPanel.setVisible(true);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);

        if (totalBytes > 0) {
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            progressBar.setString("0%");
            progressLabel.setText("0 B / " + formatBytes(totalBytes));
        } else {
            progressBar.setIndeterminate(true);
            progressBar.setString(null);
            progressLabel.setText("0 B diunduh");
        }

        showStatus("Mengunduh...", TEXT_SECONDARY);
    }

    private void updateDownloadProgress(long downloaded, long total) {
        progressPanel.setVisible(true);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);

        if (total > 0) {
            int percent = (int) Math.min(100, (downloaded * 100) / Math.max(1, total));
            progressBar.setIndeterminate(false);
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
            progressLabel.setText(formatBytes(downloaded) + " / " + formatBytes(total));
        } else {
            progressBar.setIndeterminate(true);
            progressBar.setString(null);
            progressLabel.setText(formatBytes(downloaded) + " diunduh");
        }
    }

    private void handleDownloadCompleted(Path file) {
        setInputsEnabled(true);
        cancelButton.setEnabled(false);
        currentDownload = null;

        long fileSize = lastTotalBytes;
        try {
            if (Files.exists(file)) {
                fileSize = Files.size(file);
            }
        } catch (IOException ignored) {
            // best effort untuk mendapatkan ukuran file
        }

        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressBar.setString("100%");

        if (fileSize >= 0) {
            progressLabel.setText(formatBytes(fileSize) + " / " + formatBytes(fileSize));
        } else {
            progressLabel.setText("Selesai");
        }

        showStatus("Unduhan selesai: " + file.getFileName(), STATUS_SUCCESS);

        if (currentDownloadName != null && currentDestination != null) {
            String completedAt = HISTORY_FORMATTER.format(LocalDateTime.now());
            addHistoryEntry(currentDownloadName, currentDownloadFormat, file.toString(), completedAt, "Berhasil");
        }

        resetDownloadState();
    }

    private void handleDownloadFailed(DownloadError error) {
        setInputsEnabled(true);
        cancelButton.setEnabled(false);
        currentDownload = null;

        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(null);
        progressLabel.setVisible(true);
        progressLabel.setText("Unduhan dihentikan.");

        showStatus(error.toUserMessage(), STATUS_ERROR);

        if (currentDownloadName != null && currentDestination != null) {
            String completedAt = HISTORY_FORMATTER.format(LocalDateTime.now());
            addHistoryEntry(currentDownloadName, currentDownloadFormat, currentDestination.toString(), completedAt,
                    error.toUserMessage());
        }

        resetDownloadState();
    }

    private void handleDownloadCancelled(Path partialFile) {
        setInputsEnabled(true);
        cancelButton.setEnabled(false);
        currentDownload = null;

        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(null);
        progressLabel.setVisible(true);
        progressLabel.setText("Unduhan dibatalkan.");

        showStatus("Unduhan dibatalkan.", TEXT_SECONDARY);

        Path displayPath = partialFile != null ? partialFile : currentDestination;
        if (currentDownloadName != null && displayPath != null) {
            String completedAt = HISTORY_FORMATTER.format(LocalDateTime.now());
            addHistoryEntry(currentDownloadName, currentDownloadFormat, displayPath.toString(), completedAt,
                    "Dibatalkan");
        }

        resetDownloadState();
    }

    private void setInputsEnabled(boolean enabled) {
        linkField.setEnabled(enabled);
        pathField.setEnabled(enabled);
        nameField.setEnabled(enabled);
        formatBox.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
    }

    private void showStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }

    private void resetDownloadState() {
        currentDestination = null;
        currentDownloadName = null;
        currentDownloadFormat = null;
        lastTotalBytes = -1L;
    }

    private void openSelectedFile() {
        Path target = resolveSelectedPath();
        if (target == null) {
            showHistoryMessage("Silakan pilih riwayat unduhan yang valid.");
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            showHistoryMessage("Fitur membuka file tidak tersedia pada perangkat ini.");
            return;
        }
        try {
            if (!Files.exists(target) || !Files.isRegularFile(target)) {
                showHistoryMessage("File tidak ditemukan: " + target.getFileName());
                updateHistoryActionState();
                return;
            }
        } catch (SecurityException ex) {
            showHistoryMessage("Tidak memiliki izin untuk membuka file ini.");
            return;
        }

        try {
            Desktop.getDesktop().open(target.toFile());
        } catch (IOException | IllegalArgumentException ex) {
            showHistoryMessage("Gagal membuka file: " + ex.getMessage());
        }
    }

    private void openSelectedFolder() {
        Path target = resolveSelectedPath();
        if (target == null) {
            showHistoryMessage("Silakan pilih riwayat unduhan yang valid.");
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            showHistoryMessage("Fitur membuka folder tidak tersedia pada perangkat ini.");
            return;
        }
        Path folder;
        try {
            folder = Files.isDirectory(target) ? target : target.getParent();
        } catch (SecurityException ex) {
            showHistoryMessage("Tidak memiliki izin untuk membuka folder ini.");
            return;
        }

        if (folder == null) {
            showHistoryMessage("Folder tidak ditemukan untuk entri ini.");
            return;
        }

        try {
            if (!Files.exists(folder)) {
                showHistoryMessage("Folder tidak ditemukan: " + folder);
                updateHistoryActionState();
                return;
            }
        } catch (SecurityException ex) {
            showHistoryMessage("Tidak memiliki izin untuk membuka folder ini.");
            return;
        }

        try {
            Desktop.getDesktop().open(folder.toFile());
        } catch (IOException | IllegalArgumentException ex) {
            showHistoryMessage("Gagal membuka folder: " + ex.getMessage());
        }
    }

    private Path resolveSelectedPath() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        Object titleValue = historyTableModel.getValueAt(selectedRow, 0);
        if (titleValue instanceof String title && "Belum ada riwayat".equals(title)) {
            return null;
        }

        Object destinationValue = historyTableModel.getValueAt(selectedRow, 2);
        if (!(destinationValue instanceof String destination)) {
            return null;
        }

        if (destination.isBlank() || "-".equals(destination)) {
            return null;
        }

        try {
            return Paths.get(destination);
        } catch (InvalidPathException ex) {
            return null;
        }
    }

    private void updateHistoryActionState() {
        if (!Desktop.isDesktopSupported()) {
            openFileButton.setEnabled(false);
            openFolderButton.setEnabled(false);
            return;
        }

        Path target = resolveSelectedPath();
        boolean fileEnabled = false;
        boolean folderEnabled = false;

        if (target != null) {
            try {
                fileEnabled = Files.exists(target) && Files.isRegularFile(target);
                Path folder = Files.isDirectory(target) ? target : target.getParent();
                folderEnabled = folder != null && Files.exists(folder);
            } catch (SecurityException ex) {
                fileEnabled = false;
                folderEnabled = false;
            }
        }

        openFileButton.setEnabled(fileEnabled);
        openFolderButton.setEnabled(folderEnabled);
    }

    private void showHistoryMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Riwayat Unduhan", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "Tidak diketahui";
        }

        String[] units = { "B", "KB", "MB", "GB", "TB" };
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }

        if (unitIndex == 0) {
            return bytes + " B";
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, units[unitIndex]);
    }

    private void seedHistoryPreview() {
        if (historyTableModel.getRowCount() == 0) {
            historyTableModel.addRow(new Object[] { "Belum ada riwayat", "-", "-", "-", "-" });
        }
    }

    public void addHistoryEntry(String title, String format, String destination, String completedAt, String status) {
        if (historyTableModel.getRowCount() == 1 && "Belum ada riwayat".equals(historyTableModel.getValueAt(0, 0))) {
            historyTableModel.setRowCount(0);
        }
        historyTableModel.addRow(new Object[] { title, format, destination, completedAt, status });
        int lastRow = historyTableModel.getRowCount() - 1;
        if (lastRow >= 0) {
            historyTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
            historyTable.scrollRectToVisible(historyTable.getCellRect(lastRow, 0, true));
            updateHistoryActionState();
        }
    }
}

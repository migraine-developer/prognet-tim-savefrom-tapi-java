// dialog yang muncul setelah tekan download

import javax.swing.*;
import java.awt.*;

public class ProgressGUI extends JDialog {

    public ProgressGUI(DownloaderGUI parent) {
        super(parent, "Download Progress", false);//parent,title,modal
        //modal=true, tidak bisa interaksi dengan parent window
        //modal=false, bisa

        setLayout(new BorderLayout(10, 10));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String link = parent.getLinkField().getText();
        String path = parent.getPathField().getText();
        String name = parent.getNameField().getText();
        String format = (String) parent.getFormatBox().getSelectedItem();

        boolean allFilled = true;
        StringBuilder msg = new StringBuilder();

        if (link.isEmpty()) {
            msg.append("Download Link belum diisi!\n");
            allFilled = false;
        }
        if (path.isEmpty()) {
            msg.append("File Path belum diisi!\n");
            allFilled = false;
        }
        if (name.isEmpty()) {
            msg.append("File Name belum diisi!\n");
            allFilled = false;
        }
        if (format == null || format.isEmpty()) {
            msg.append("File Format belum diisi!\n");
            allFilled = false;
        }

        if (allFilled) {
            msg.append("Download Link: ").append(link).append("\n")
               .append("File Path: ").append(path).append("\n")
               .append("File Name: ").append(name).append("\n")
               .append("File Format: ").append(format).append("\n");
        }

        JTextArea textArea = new JTextArea(msg.toString());
        textArea.setEditable(false);
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(textArea);

        add(textPanel, BorderLayout.CENTER);

        if (allFilled) {
            JButton finishButton = new JButton("Finish");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(finishButton);
            add(buttonPanel, BorderLayout.SOUTH);

            finishButton.addActionListener(e -> dispose());
        }

        pack(); //atur size window fleksibel tergantung ukuran item di dalamnya
        setLocationRelativeTo(parent);
    }
}

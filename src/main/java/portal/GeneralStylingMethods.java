package portal;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class GeneralStylingMethods {

    // Method to do general styling of all labels inside of the JPanel.
    public void styleLabel(JLabel label) {
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }

    // Method to showcase the error message for errors which occur inside of the program.
    public void displayErrorMessage(String title, String message) {
        JLabel messageLabel = new JLabel("<html><div style='text-align:left;'><h3 style='color:#D32F2F; font-size:18px;'>" + title + "</h3><p style='font-size:14px;'>" + message + "</p></div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.addActionListener(evt -> SwingUtilities.getWindowAncestor(okButton).dispose());
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        JOptionPane.showOptionDialog(null, messageLabel, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] { okButton }, okButton);
    }

    // Creates the big button with the icon in the center of it which the user can click. This allows for better look wise GUI which the user can better interact with.
    public JButton createOptionButton(String title, Icon icon) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#213f9e"), 3), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        button.setPreferredSize(new Dimension(260, 260));
        button.setFocusPainted(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        button.add(iconLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        button.add(titleLabel, BorderLayout.SOUTH);

        return button;
    }

    // Method to do all general styling of all textfields inside of this JPanel.
    public void styleTextField(JTextComponent textField) {
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setCaretColor(Color.BLACK);
        textField.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }

    // Creating a styled textfield for all of the textfields inside of the PayrollYearlyTablePage class.
    public JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setCaretColor(Color.BLACK);
        textField.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return textField;
    }

    // Method to do all general styling of all buttons inside of the JPanel.
    public void styleButton(JButton button) {
        button.setBackground(Color.decode("#213f9e"));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 40));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder((Color.decode("#284fc9")), 2), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
    }

    // Creating a styled button for all of the required JButtons inside of the PayrollYearlyTablePage class.
    public JButton createStyledButton(String textForButton) {
        JButton button = new JButton(textForButton);
        button.setBackground(Color.decode("#213f9e"));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
    
        int padding = 30;
        int widthPerChar = 10;
        int buttonWidth = (textForButton.length() * widthPerChar) + padding;
    
        button.setPreferredSize(new Dimension(buttonWidth, 40));
    
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#284fc9"), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

    /// Method which is used to resize the logo for the JPanel. It's purpose is to maintain the aspect ratio of the image 
    /// maintaing the quality and integrity of the file while also making sure it is small enough to where it doesn't 
    /// take up alot of the visible space.
    public Icon resizeIcon(ImageIcon icon, int maxWidth, int maxHeight) {
        // Get original dimensions of the image file
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        double aspectRatio = (double) width / height;
        
        // Downsize width and height if the sizes inside of the image are bigger then the max allowed width and height.
        if (width > maxWidth) {
            width = maxWidth;
            height = (int) (width / aspectRatio);
        }

        if (height > maxHeight) {
            height = maxHeight;
            width = (int) (height * aspectRatio);
        }

        // Return the adjusted image.
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}

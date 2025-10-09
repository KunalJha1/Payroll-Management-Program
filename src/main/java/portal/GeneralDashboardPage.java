package portal;

import javax.swing.*;
import java.awt.*;

public class GeneralDashboardPage extends JPanel {
    private CardLayout cardLayout;
    private JPanel currentPanel;
    public GeneralStylingMethods stylingMethods = new GeneralStylingMethods();

    public GeneralDashboardPage(LoginPage parentPage, String logoImagePath) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);

        JButton homeButton = createHomeButton("Home", new ImageIcon("images\\home_icon.png"));
        // Home Button redirects to the dashboard screen, this allows the user to see all of their options and choose which section of the program to use.
        homeButton.addActionListener(e -> cardLayout.show(currentPanel, "Dashboard"));
        logoPanel.add(homeButton, BorderLayout.WEST);

        JButton signOutButtom = new JButton("SIGN OUT");
        // Utilizes the object of the GeneralStylingMethods class in order to style the buttons and other things which are used inside of this class, this allows for a consistent look across the program.
        stylingMethods.styleButton(signOutButtom);
        signOutButtom.addActionListener(e -> parentPage.showLoginPage("images\\insta_tax_services_logo_website_home_accountant_blue.png"));
        logoPanel.add(signOutButtom, BorderLayout.EAST);

        if (logoImagePath != null && !logoImagePath.isEmpty()) {
            ImageIcon logoIcon = new ImageIcon(logoImagePath);
            JLabel logoLabel = new JLabel();
            logoLabel.setIcon(stylingMethods.resizeIcon(logoIcon, 300, 100));
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            logoPanel.add(logoLabel, BorderLayout.CENTER);
        }

        add(logoPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        currentPanel = new JPanel(cardLayout);
        currentPanel.setBackground(Color.WHITE);
        add(currentPanel, BorderLayout.CENTER);

        currentPanel.add(createOptionsPanel(), "Dashboard");

        PayrollDashboardPage payrollDashboard = new PayrollDashboardPage(currentPanel, cardLayout);
        currentPanel.add(payrollDashboard, "Payroll");
    }


    /// Independent method which allows for the creation of the home button which is carried forward throughout the rest of the program. Through the usage of the topbar.
    private JButton createHomeButton(String text, ImageIcon icon) {
        JButton button = new JButton(text, stylingMethods.resizeIcon(icon, 24, 24));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#213f9e"));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        return button;
    }

    // Creates all of the options for the program. Currently, there is only a payroll option however the usage of this method allows for further expansion at a later date.
    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JButton payrollButton = stylingMethods.createOptionButton("Payroll", new ImageIcon("images\\insta-tax-services-icon-payroll.png"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        optionsPanel.add(payrollButton, gbc);

        payrollButton.addActionListener(e -> cardLayout.show(currentPanel, "Payroll"));

        return optionsPanel;
    }


}

package portal;

/// All imports which will be used for the JSwing Components along with making sure that the SQL driver 
/// and connection can be established with the system.
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage extends JFrame {

    private JPanel currentPanel;

    public GeneralStylingMethods stylingMethods = new GeneralStylingMethods();

    // Constructor which is automatically called when the class is called, this sets some basic elements for the GUI and also gives the application its name.
    public LoginPage(String iconImagePath, String logoImagePath) {
        setTitle("Insta Tax Services | Payroll Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);

        if (iconImagePath != null && !iconImagePath.isEmpty()) {
            setIconImage(new ImageIcon(iconImagePath).getImage());
        }

        showLoginPage(logoImagePath);
        setVisible(true);
    }

    // Initally called when the program is run and updates the empty JPanel to create and showcase all the attributes and elements required in the GUI.
    public void showLoginPage(String logoImagePath) {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        currentPanel = createLoginPage(logoImagePath);
        add(currentPanel);
        revalidate();
        repaint();
    }

    // Updates the current JPanel and "redirects" it to the create account page. Moreover, this method carries forward the path of the logo.
    private void showCreateAccountPage(String logoImagePath) {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        currentPanel = new CreateAccountPage(logoImagePath, () -> showLoginPage(logoImagePath));
        add(currentPanel);
        revalidate();
        repaint();
    }

    // When the login is successful this method is called to showcase the dashboardpage. This page is used in order to showcase all 3 options for the payroll processing which is currently built in.
    private void showDashboardPage(String logoImagePath) {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        currentPanel = new GeneralDashboardPage(this, logoImagePath);
        add(currentPanel);
        revalidate();
        repaint();
    }

    /// Creates all of the visible elements of the login panel. This is what executes all of the methods which are required for making the buttons,
    /// adding the action listeners for functions and also for adding the logo at the top of the page. It makes calls to several other methods inside of this class.
    private JPanel createLoginPage(String logoImagePath) {
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(Color.WHITE);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);

        if (logoImagePath != null && !logoImagePath.isEmpty()) {
            ImageIcon logoIcon = new ImageIcon(logoImagePath);
            JLabel logoLabel = new JLabel();
            logoLabel.setIcon(stylingMethods.resizeIcon(logoIcon, 300, 100));
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            logoPanel.add(logoLabel, BorderLayout.CENTER);
        }
        loginPanel.add(logoPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Create and add the styled SIGN IN button to the JPanel.
        JLabel signInLabel = new JLabel("SIGN IN");
        signInLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        signInLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(signInLabel, gbc);

        // Create and add the styled email textfield and label to the JPanel.
        JLabel emailLabel = new JLabel("Email");
        stylingMethods.styleLabel(emailLabel);
        JTextField emailField = new JTextField(20);
        stylingMethods.styleTextField(emailField);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(emailField, gbc);

        // Create and add the styled password textfield and label to the JPanel. This also ensures that the password is not visible to others as you write it inside of the textfield.
        JLabel passwordLabel = new JLabel("Password");
        stylingMethods.styleLabel(passwordLabel);
        JPasswordField passwordField = new JPasswordField(20);
        stylingMethods.styleTextField(passwordField);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(passwordField, gbc);

        JButton signInButton = new JButton("Sign In");
        stylingMethods.styleButton(signInButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(signInButton, gbc);

        /// Add's an action listener to the JButton, the action listener is responsible for making sure that the login information is valid before
        /// allowing a user to proceed inside of the program.
        signInButton.addActionListener((ActionEvent e) -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            if (checkLogin(email, password)) {
                showDashboardPage(logoImagePath);
            } else {
                stylingMethods.displayErrorMessage("Invalid credentials", "Please check your email and password, then try again.");
            }
        });
        
        getRootPane().setDefaultButton(signInButton);

        //
        JButton createAccountButton = new JButton("Create Account");
        stylingMethods.styleButton(createAccountButton);
        gbc.gridy = 4;
        contentPanel.add(createAccountButton, gbc);
        // Redirects the page to a new "Create Account Page" when the button is clicked by the user.
         createAccountButton.addActionListener(e -> showCreateAccountPage(logoImagePath));

        loginPanel.add(contentPanel, BorderLayout.CENTER);
        return loginPanel;
    }

    /// Method which has the purpose of accessing the SQL database in order to retrive login information. This allows a secure system and 
    /// functions as a easy to maintain way of keeping track of user information.
    private boolean checkLogin(String email, String password) {
        boolean isValid = false;
        String url = "jdbc:mysql://127.0.0.1:3306/program_information";
        DatabaseInformation dbPasswordGet = new DatabaseInformation();
        String dbPassword = dbPasswordGet.getPassword();
        String username = dbPasswordGet.getUsername();

        // Way of getting a connection user the driver for the SQL connection. This allows access to the SQL system and executing any query's in the system through the java code.
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword)) {
            String query = "SELECT * FROM email_and_password WHERE email = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();
                // Statement checks if the email and password both exist inside of the data table. If this is the case it will return true as the isValid statement which will allow the user to sign in.
                isValid = resultSet.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isValid;
    }

    /// Main method with the relative path of the file so it can be run on several systems granted that all assets are also downloaded. 
    /// Moreover, it will allow for a falicon (Icon) to be seen inside of the user's task bar.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage("images\\insta_tax_services_favicon_512_512.png","images\\insta_tax_services_logo_website_home_accountant_blue.png"));
    }
}

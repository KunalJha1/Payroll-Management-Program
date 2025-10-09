package portal;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CreateAccountPage extends JPanel {

    public GeneralStylingMethods stylingMethods = new GeneralStylingMethods();

    /// Constructor of the class which is responsible for the creation of applying the page charactersitcs of the CreateAccountPage class
    /// moreover, inside of this class it utilizes an object of the Loginpage class which allows for the use of all public methods in particular
    /// the styling methods which will allow for more managability of the program.
    public CreateAccountPage(String logoImagePath, Runnable onBackToLogin) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);

        if (logoImagePath != null && !logoImagePath.isEmpty()) {
            ImageIcon logoIcon = new ImageIcon(logoImagePath);
            JLabel logoLabel = new JLabel();
            logoLabel.setIcon(stylingMethods.resizeIcon(logoIcon, 300, 100));
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            logoPanel.add(logoLabel, BorderLayout.CENTER);
        }
        add(logoPanel, BorderLayout.NORTH);

        /// This section of the program creates and adds all of the GUI JSwing elements into the GUI for the user to iteract with,
        /// moreover it is the section of the program which issues a action to the SQL server which allows for the addition of a user
        /// into the database.

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel createAccountLabel = new JLabel("CREATE ACCOUNT");
        createAccountLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        createAccountLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(createAccountLabel, gbc);

        JLabel emailLabel = new JLabel("Email");
        stylingMethods.styleLabel(emailLabel);
        JTextField emailField = new JTextField(20);
        stylingMethods.styleTextField(emailField);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        stylingMethods.styleLabel(passwordLabel);
        JPasswordField passwordField = new JPasswordField(20);
        stylingMethods.styleTextField(passwordField);
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        JButton createButton = new JButton("Create Account");
        stylingMethods.styleButton(createButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(createButton, gbc);

        /// Showcases the use of encapsulation in order to get restricted data from the system. This allows for the protection of the 
        /// dbpassword which is sensitive to the integrity of the database which contains confedential client information.

        createButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            if (email.isEmpty() || password.isEmpty()) {
                stylingMethods.displayErrorMessage("Error", "All fields are required.");
            } else if (addUserToDatabase(email, password)) {
                stylingMethods.displayErrorMessage("Success", "Account created successfully!");
                onBackToLogin.run();
            } else {
                stylingMethods.displayErrorMessage("Error", "Failed to create account.");
            }
        });

        JButton backButton = new JButton("Back to Login");
        stylingMethods.styleButton(backButton);
        gbc.gridy = 4;
        contentPanel.add(backButton, gbc);

        backButton.addActionListener(e -> onBackToLogin.run());
        add(contentPanel, BorderLayout.CENTER);
    }

    /// This is the method which contacts the database, inside of this it uses the dbPassword which is obtianed through the getPassword method from the
    /// LoginPage class and then adds the email and password information to the approproiate table.

    private boolean addUserToDatabase(String email, String password) {
        DatabaseInformation dbInformation = new DatabaseInformation();
        String dbPassword = dbInformation.getPassword();       
        String username = dbInformation.getUsername();
        String url = "jdbc:mysql://127.0.0.1:3306/program_information";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword)) {
            String query = "INSERT INTO users (email, password) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, password);
                statement.executeUpdate();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}

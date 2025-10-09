package portal;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

import javax.swing.*;

public class PayrollInformationPage extends JPanel {
    /// Initalization of all of these attributes of the class allows for easier access and no need to pass every single textbox 
    /// or combofield into the individual SQL methods which are doing read and write functions.
    private JComboBox<String> yearComboBox;
    private JComboBox<String> employerProvinceField;
    private JComboBox<String> employeeFullNameComboBox;
    private JComboBox<String> employeeProvinceField;
    private JComboBox<String> paymentFrequencyField;
    private boolean employeeComboBoxInitialized = false;
    private JTextField employerCityField;
    private JTextField employerAddressField;
    private JTextField employerPostalCodeField;
    private JTextField employerRPField;
    private JTextField employeeFullNameField;
    private JTextField employeeSINField;
    private JTextField employeeAddressField;
    private JTextField employeeCityField;
    private JTextField employeePostalCodeField;
    private JTextField employeeNumberField;
    private JTextField beginPayPeriodField;
    private JTextField endPayPeriodField;
    private JTextField depositNumberField;
    private JTextField rateOfPaymentField;
    private JTextField amountOfHoursField;
    private JTextField employeeBonusField;
    private JTextField baseAmountField;
    private JTextField employeeMealAllowanceField;
    private JTextField overTimeHours;
    private JTextField joiningDateField;
    private JCheckBox cppExemptCheckBox;
    private JCheckBox eiExemptCheckBox;
    private JCheckBox hasOvertimeCheckBox;
    private JCheckBox useBaseAmountCheckBox;
    private JCheckBox useYearlyCheckBox;
    private JCheckBox employeeTD1CheckBox;
    private JComboBox<String> employerCompanyNameComboBox;
    public DatabaseInformation dbInformation = new DatabaseInformation();
    public GeneralFunctions generalMethods = new GeneralFunctions();

    public PayrollInformationPage(JPanel parentPanel, CardLayout cardLayout) {

        /// Uses the extension (inheritance) of the JPanel type to set the layout and other elements without having to specify which 		 
        /// panel it is regarding as all JPanel methods are extended.
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        /// Adds a back button which will redirect the user to the dashboard page, this allows for the user to move between the 
        /// sections of the program easier.
        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 30));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(0, 122, 204));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> cardLayout.show(parentPanel, "Dashboard"));
        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JPanel employerHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        employerHeaderPanel.setBackground(Color.WHITE);
        JLabel employerHeader = new JLabel("EMPLOYER INFORMATION");
        employerHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        employerHeader.setForeground(Color.decode("#213f9e")); // Darker blue color
        employerHeaderPanel.add(employerHeader);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        contentPanel.add(employerHeaderPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        JPanel employerFormPanel = new JPanel(new GridBagLayout());
        employerFormPanel.setBackground(Color.WHITE);
        addEmployerFields(employerFormPanel);
        contentPanel.add(employerFormPanel);
        // Creates the buttons which will allow for the reading and writing of Employer Information.
        JPanel employerButtonPanel = new JPanel();
        employerButtonPanel.setBackground(Color.WHITE);
        JButton createEmployerTableButton = createStyledButton("Create Employer Profile");
        JButton openEmployerTableButton = createStyledButton("Autofill Employer Profile");

        createEmployerTableButton.addActionListener(e -> createMySQLTable());
        openEmployerTableButton.addActionListener(e -> openMySQLTable());
        employerButtonPanel.add(createEmployerTableButton);
        employerButtonPanel.add(openEmployerTableButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        contentPanel.add(employerButtonPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel employeeHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        employeeHeaderPanel.setBackground(Color.WHITE);
        JLabel employeeHeader = new JLabel("EMPLOYEE INFORMATION");
        employeeHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        employeeHeader.setForeground(Color.decode("#213f9e"));
        employeeHeaderPanel.add(employeeHeader);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        contentPanel.add(employeeHeaderPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        JPanel employeeFormPanel = new JPanel(new GridBagLayout());
        employeeFormPanel.setBackground(Color.WHITE);
        addEmployeeFields(employeeFormPanel);
        contentPanel.add(employeeFormPanel);

        JPanel employeeButtonPanel = new JPanel();
        employeeButtonPanel.setBackground(Color.WHITE);
        JButton createEmployeeTableButton = createStyledButton("Create Employee Profile");
        JButton openEmployeeTableButton = createStyledButton("Open Employee Profile");

        JButton projectionsButton = createStyledButton("Payroll Projections");

        projectionsButton.addActionListener(e -> cardLayout.show(parentPanel, "PayrollProjectionsYearlong"));
        createEmployeeTableButton.addActionListener(e -> {
            createEmployeeSQLTable();
            // Show a message dialog after the table creation
            JOptionPane.showMessageDialog(this, 
                "Employee table has been created successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE); // You can change the icon here to JOptionPane.WARNING_MESSAGE or JOptionPane.ERROR_MESSAGE
        });
        
        openEmployeeTableButton.addActionListener(e -> {
            openEmployeeSQLTable();
            // Show a message dialog after opening the employee table
            JOptionPane.showMessageDialog(this, 
                "Employee table has been opened successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE); // Change icon to suit the message type
        });
        

        employeeButtonPanel.add(createEmployeeTableButton);
        employeeButtonPanel.add(openEmployeeTableButton);
        contentPanel.add(employeeButtonPanel);

        /// Does the functions which create the basic CSV for the client to use, this button and the SQL table it creates allows for 		 
        /// many of the process required in later steps of the program.
        JButton generateCSVButton = createStyledButton("Generate Year CSV");
        generateCSVButton.addActionListener(e -> generateYearlyCSV());

        JButton resetButton = createStyledButton("Reset Page");
        resetButton.addActionListener(e -> resetPage(parentPanel, cardLayout));
        employeeButtonPanel.add(resetButton);
        
        
        employeeButtonPanel.add(generateCSVButton);
        employeeButtonPanel.add(projectionsButton);


        /// Just in case that the page becomes too compacted and too many items are on the GUI it will make the entire page 
        /// scrollable.
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        containerPanel.setBackground(Color.WHITE);
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        containerPanel.setPreferredSize(new Dimension(600, 800));

        PayrollYearlyTablePage projectionsPanel = new PayrollYearlyTablePage(parentPanel, cardLayout);
        parentPanel.add(projectionsPanel, "PayrollProjectionsYearlong");

        add(containerPanel, BorderLayout.CENTER);
    }

    private int getCompanyIdByName(String companyName) {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT id FROM ALL_COMPANIES WHERE company_name = ?";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, companyName);
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                // Return the ID of the company
                return resultSet.getInt("id");
            } else {
                // If the company doesn't exist, return -1 (or you can throw an exception if preferred)
                return -1;
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving company ID for '" + companyName + "': " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    

    // Creates the employer information table which will allow for the information to be stored inside of a consistent format inside of the SQL server.
    private void createMySQLTable() {
        String companyNameRegular = getCompanyName();
        
        // Get the next available company ID by checking the last inserted ID
        int companyID = getNextCompanyId();
        
        // Use the company ID to create the table name
        String tableNamePrefix = "EmplI_";
        String tableName = tableNamePrefix + companyID;
        
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        
        // SQL queries for creating and inserting into the company table
        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
        String createTableSQL = "CREATE TABLE " + tableName + " ("
                + "EmployerCompanyName VARCHAR(255), "
                + "EmployerCity VARCHAR(255), "
                + "EmployerProvince VARCHAR(255), "
                + "EmployerAddress VARCHAR(255), "
                + "EmployerPostalCode VARCHAR(255), "
                + "EmployerRONumber VARCHAR(255))";
        
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement()) {
    
            // Drop the table if it exists and create a new one
            statement.executeUpdate(dropTableSQL);
            statement.executeUpdate(createTableSQL);
    
            // Insert company data into the created table
            String insertDataSQL = "INSERT INTO " + tableName
                    + " (EmployerCompanyName, EmployerCity, EmployerProvince, EmployerAddress, EmployerPostalCode, EmployerRONumber) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
                String employerName = ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).getText();
                preparedStatement.setString(1, employerName);
                preparedStatement.setString(2, employerCityField.getText());
                preparedStatement.setString(3, (String) employerProvinceField.getSelectedItem());
                preparedStatement.setString(4, employerAddressField.getText());
                preparedStatement.setString(5, employerPostalCodeField.getText());
                preparedStatement.setString(6, employerRPField.getText());
                preparedStatement.executeUpdate();
            }
    
            // Add the company to the "ALL_COMPANIES" table
            addCompanyToAllCompanies(companyNameRegular);
    
            JOptionPane.showMessageDialog(this, "Table '" + tableName + "' created and populated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to get the next available company ID
    private int getNextCompanyId() {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT MAX(id) AS max_id FROM ALL_COMPANIES";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
    
            if (resultSet.next()) {
                // Get the max ID and increment it by 1
                int maxId = resultSet.getInt("max_id");
                return (maxId == 0) ? 1 : maxId + 1; // If no companies exist, return 1
            } else {
                return 1; // No companies, start with ID 1
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving company ID: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return 1; // Default to ID 1 in case of an error
        }
    }
    
    
    /// This is what builds the autoselect combobox which is used inside of the JPanel, it adds all of the available company 
    /// names into a table from which you can select and autofill a companies information who already have their information created.
    private void addCompanyToAllCompanies(String companyName) {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String createAllCompaniesTableSQL = "CREATE TABLE IF NOT EXISTS ALL_COMPANIES (id INT AUTO_INCREMENT PRIMARY KEY, company_name VARCHAR(255) UNIQUE)";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword); 
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createAllCompaniesTableSQL);
    
            // Check if the company already exists in the table
            String checkCompanySQL = "SELECT COUNT(*) FROM ALL_COMPANIES WHERE company_name = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkCompanySQL)) {
                checkStatement.setString(1, companyName);
                ResultSet resultSet = checkStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    // Insert the company with a unique ID
                    String insertCompanySQL = "INSERT INTO ALL_COMPANIES (company_name) VALUES (?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertCompanySQL)) {
                        insertStatement.setString(1, companyName);
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding company to ALL_COMPANIES table: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    /// Checks to see if the employee table which all of the employees for the company even exists, this is important to not 
    /// cause a problem inside of the program.
    private boolean checkEmployeeTableExists() {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String checkTableExistsSQL = "SHOW TABLES LIKE 'ALLEMP'";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(checkTableExistsSQL)) {
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /// This is the SQL method whihc creates the table which will be used to autofill the names of employees into the table, 
    /// it is essential to the processing of the system and allows for quicker and more efficient use of the program for the user.
    private void addEmployeeToAllEmployees(String employeeName) {
        String companyNameRegular = getCompanyName();
        int companyID = getCompanyIdByName(companyNameRegular);

        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String createAllEmployeesTableSQL = "CREATE TABLE IF NOT EXISTS ALLEMP_" + companyID + " (employee_name VARCHAR(255) PRIMARY KEY)";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword); 
            Statement statement = connection.createStatement()) {
            statement.executeUpdate(createAllEmployeesTableSQL);

            String checkEmployeeSQL = "SELECT COUNT(*) FROM ALLEMP_" + companyID + " WHERE employee_name = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkEmployeeSQL)) {
                checkStatement.setString(1, employeeName);
                ResultSet resultSet = checkStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    String insertEmployeeSQL = "INSERT INTO ALLEMP_" + companyID + " (employee_name) VALUES (?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertEmployeeSQL)) {
                        insertStatement.setString(1, employeeName);
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding employee to ALLEMP table:" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String convertYearToDbNameID(JComboBox<String> yearComboBox) {
        String selectedYear = (String) yearComboBox.getSelectedItem();
    
        switch (selectedYear) {
            case "2024":
                return "1";
            case "2025":
                return "2";
            default:
                throw new IllegalArgumentException("Invalid year selected: " + selectedYear);
        }
    }




    // This is the main method of this class, it is what allows the program to generate the needed CSV and other files which will be carried forward into other parts of the program insdie of the future.
    private void generateYearlyCSV() {
        try {
            String employeeName = getEmployeeName();
            String employeeNameDatabase = getEmployeeNameTable();
            String companyNameRegular = getCompanyName();

            int companyID = getCompanyIdByName(companyNameRegular);

            String tableName = employeeNameDatabase + "_" + companyID + convertYearToDbNameID(yearComboBox) + "_pap";
            String url = dbInformation.getUrl();
            String username = dbInformation.getUsername();
            String dbPassword = dbInformation.getPassword();

            createEmployeeSQLTable();

            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName + "";
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "sl_no INT, pay_date DATE, hours_worked DECIMAL(10, 2), "
                    + "rate_per_hour DECIMAL(10, 2), basic_amount DECIMAL(10, 2), vacation_pay DECIMAL(10, 2), "
                    + "total_earnings DECIMAL(10, 2), federal_tax DECIMAL(10, 2), provincial_tax DECIMAL(10, 2), "
                    + "income_tax DECIMAL(10, 2), cpp DECIMAL(10, 2), cpp2 DECIMAL(10, 2), ei DECIMAL(10, 2), total_deductions DECIMAL(10, 2), "
                    + "net_pay DECIMAL(10, 2), cra_remittance DECIMAL(10, 2), "
                    + "has_overtime BOOLEAN, overtime_hours DECIMAL(10, 2), overtime_rate DECIMAL(10, 2), use_base_amount BOOLEAN, base_amount DECIMAL(10, 2), "
                    + "paid BOOLEAN DEFAULT FALSE, meal_Allowance DECIMAL(10,2), amount_bonus DECIMAL(10,2), ctc DECIMAL(10,2))";

            /// Creation of a folder which has the companies name if it doesn't already exist, 
            /// this will help in the organization of the program and the files generated to make sure that they are organized.
            String fileName = getCompanyName();
            File csvFolder = new File(fileName);
            double undoingCPP = 0.0;

            if (!csvFolder.exists()) {
                csvFolder.mkdir();
            }

            String csvFilePath = csvFolder.getAbsolutePath() + File.separator + employeeName + " Yearly Paystub.csv";

            try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                // Initalizes the writer to make the csv path for where the data will be saved.
                FileWriter writer = new FileWriter(csvFilePath)) {
                statement.executeUpdate(dropTableSQL);
                statement.executeUpdate(createTableSQL);

                // Gets all of the available employee information from the textfields inside of the information section. This is where the autofilled information also comes to.
                String employeeAddress = employeeAddressField.getText();
                String employeeCity = employeeCityField.getText();
                String employeePostalCode = employeePostalCodeField.getText();
                String employeeSIN = employeeSINField.getText();
                String employeeNumber = employeeNumberField.getText();
                // String joiningDate = joiningDateField.getText();
                int depositNumberDouble = Integer.parseInt(depositNumberField.getText());
                // Makes it so for every single period there isn't overtime hours, but you can add overtime hours if you adjust in inside of the yearly table section.
                double hoursOvertime = 0.0;

                // Trys to parse the overtimeHours section and if there is actual text then it will use those hours.
                try {
                    hoursOvertime = Double.parseDouble(overTimeHours.getText());
                    
                } catch (Exception e) {
                    //System.out.println("No Hours Worked");
                }

                // Gets all of the employer information which is avaialbe from the textfields which have likely been autofilled from the SQL table.
                String employerName = ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).getText();
                String employerAddress = employerAddressField.getText();
                String employerCity = employerCityField.getText();
                String employerPostalCode = employerPostalCodeField.getText();

                // Initalize and declares the values and types for several data types which will have their values updated inside of the future.
                double rate = 0;
                double hours = 0;
                double bonus = 0;
                double overtimeRate = 0;
                double mealAllowance = 0.0;

                try {
                    mealAllowance = Double.parseDouble(employeeMealAllowanceField.getText());
                } catch (NumberFormatException e) {
                    mealAllowance = 0.0;
                }

                String paymentFrequency = (String) paymentFrequencyField.getSelectedItem();
                int periodsPerYear;
                double hoursPerPeriod = 0.0;
                switch (paymentFrequency) {
                    case "Weekly (52)":
                        periodsPerYear = 52;
                        break;
                    case "Weekly (53)":
                        periodsPerYear = 53;
                        break;
                    case "Bi-Weekly (26)":
                        periodsPerYear = 26;
                        break;
                    case "Bi-Weekly (27)":
                        periodsPerYear = 27;
                        break;
                    case "Semi-Monthly (24)":
                        periodsPerYear = 24;
                        break;
                    case "Monthly (12)":
                        periodsPerYear = 12;
                        break;
                    case "Quarterly (4)":
                        periodsPerYear = 4;
                        break;
                    case "Semi-Annually (2)":
                        periodsPerYear = 2;
                        break;
                    case "Yearly (1)":
                        periodsPerYear = 1;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown payment frequency");
                }


                try{
                    hoursPerPeriod = useYearlyCheckBox.isSelected() ? 2080.0 / periodsPerYear: Double.parseDouble(amountOfHoursField.getText());
                } catch (Exception e){
                    //System.out.println("cannot parse hours");
                    hoursPerPeriod = 0.0;
                }

                double questionableRate = 0.00;

                try{
                    questionableRate = Double.parseDouble(rateOfPaymentField.getText());
                } catch (Exception e){
                    //System.out.println("cannot parse hours");
                    questionableRate = 0.0;
                }

                try{
                    bonus = Double.parseDouble(employeeBonusField.getText());
                } catch (Exception e){
                    //System.out.println("cannot parse hours");
                    questionableRate = 0.0;
                }


                // Makes it so if someone wants to pay a lumpsum amount then it will list the hours as 0 and the rate as 0 as there are other calculations which must be preformed.
                if (useBaseAmountCheckBox.isSelected()) {
                    rate = 0;
                    hours = 0;
                }

                // If the yearly checkbox is filled it will distrbute the amount of funds the employee should be paid yearly into the frequency of the payment schedule.
                if (useYearlyCheckBox.isSelected()) {
                    Double annualSum = Double.parseDouble(baseAmountField.getText());
                    Double amountRate = annualSum / periodsPerYear / hoursPerPeriod;
                    rate = amountRate;
                    hours = hoursPerPeriod;
                } else { // Other then that it will get the rate hours and the value for the amount of bonus to distrubte.
                    rate = questionableRate;
                    hours = hoursPerPeriod;
                    bonus = Double.parseDouble(employeeBonusField.getText());
                    overtimeRate = BigDecimal.valueOf(rate * 1.5).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    // System.out.println(overtimeRate + " OVERTIME RATE");
                
                }

                double earningsCurrent;
                // If the base amount checkbox is selected then it will reflect that inside of the GUI by making values showcase that as well.

                if (useBaseAmountCheckBox.isSelected()) {
                    earningsCurrent = Double.parseDouble(baseAmountField.getText());
                    rateOfPaymentField.setText("0.00");
                    amountOfHoursField.setText("0.00");
                } else {
                    earningsCurrent = rate * hours;
                }
                



                // Round to two decimal places
                double earningsCurrentUnrounded = earningsCurrent;
                BigDecimal earningsCurrentRounded = new BigDecimal(earningsCurrent).setScale(2, RoundingMode.HALF_UP);
                earningsCurrent = earningsCurrentRounded.doubleValue(); // Convert back to double if needed

                GeneralFunctions generalFunctionMethods = new GeneralFunctions();

                String provinceCode = generalFunctionMethods.getProvinceCode((String) employeeProvinceField.getSelectedItem());
                String employerProvinceCode = generalFunctionMethods.getProvinceCode((String) employerProvinceField.getSelectedItem());
                
                TaxCalculations taxCalculator = new TaxCalculations(convertYearToDbName(yearComboBox));
                LocalDate startDate = LocalDate.parse(beginPayPeriodField.getText(),
                
                DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                LocalDate endDate = null;
                LocalDate generationEndDate = parseEndPayPeriod();

                if (generationEndDate == null) {
                    JOptionPane.showMessageDialog(this, "CSV generation halted due to an invalid beginning date.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double maxCPPContribution = getMaxContribution("CPP");
                double maxCPPContributionOG = maxCPPContribution;
                //System.out.println("max CPP CONTRIBUTION first" + maxCPPContribution);
                double maxEIContribution = getMaxContribution("EI");
                boolean td1Status = taxCalculator.TD1Status(employeeSIN, employeeName, employerName, getCompanyIdByName(companyNameRegular));
                double cppExemption = taxCalculator.cppExemptionsGet();

                double ytdEarnings = 0.0;
                double ytdVacationPay = 0.0;
                double ytdBonus = 0.0;
                double ytdCPP = 0.0;
                double ytdEI = 0.0;
                double ytdPayrollTax = 0.0;
                double ytdNetPay = 0.0;
                double ytdCPP2 = 0.0;
                double ytdTotalEarnings = 0.0;
                double ytdCPPEarnings = 0.0;
                double ytdMealAllowance = 0.0;

                // This ensures that on the paystub itself it doesn't reveal sensitive information about the employee themselves.
                String lastThreeSIN = employeeSIN.length() >= 3 ? employeeSIN.substring(employeeSIN.length() - 3) : employeeSIN;

                // This parses the startdate and makes sure that it matches the formatting whihc is requried for the program.
                try {
                    String startDateText = beginPayPeriodField.getText().trim();
                    if (!startDateText.matches("\\d{4}/\\d{2}/\\d{2}")) {
                        JOptionPane.showMessageDialog(this, "Date must be in the format yyyy/MM/dd.", "Invalid Date Format", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    startDate = LocalDate.parse(startDateText, dateFormatter);

                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy/MM/dd format.", "Date Parsing Error", JOptionPane.ERROR_MESSAGE);
                }

                double overTimeMoney = 0;

                if (hasOvertimeCheckBox.isSelected()) {
                    overTimeMoney = hoursOvertime * overtimeRate;
                }

                boolean lastround = true;
                boolean firstRoundSecond = true;
                double cppPeriodsCompleted = 0.0;

                boolean isFirstRow = true;

                for (int period = 0; period < periodsPerYear; period++) {
                    if (period != 0) {
                        isFirstRow = false;
                    }

                    String depositNumber = String.valueOf((int) depositNumberDouble);
                    double amountVacationPayMultiply = 0.0;
                    
                    if (isFirstRow) {
                        amountVacationPayMultiply = earningsCurrent + bonus + mealAllowance + overTimeMoney;
                    } else {
                        amountVacationPayMultiply = earningsCurrent + bonus + mealAllowance;
                    }

                    
                    double vacationPay = amountVacationPayMultiply * 0.04;
                    double vacationPayUnrounded = vacationPay;
                    BigDecimal vacationPayRounded = new BigDecimal(vacationPay).setScale(2, RoundingMode.HALF_UP);
                    vacationPay = vacationPayRounded.doubleValue();


                    double unroundedEarnings = vacationPayUnrounded + bonus + earningsCurrentUnrounded;
                    double totalEarnings = earningsCurrent + vacationPay + bonus + mealAllowance;
                    // Since the program only treats the amount of overtime hours for one case it only adds that amount of money to the total earnings one time.
                    if (period == 0) {
                        totalEarnings += overTimeMoney;
                    }
                    // Amounmt of money you would make inside of a year if you made the same amount throughout the year.
                    double projectedAnnualEarnings = totalEarnings * periodsPerYear;
                    double projectedAnnualEarningsUnrounded = unroundedEarnings * periodsPerYear;

                    double cppDeduction = (taxCalculator.calculateCPP(projectedAnnualEarnings - cppExemption, cppExemptCheckBox.isSelected(), convertYearToDbName(yearComboBox))) / periodsPerYear;
                    double eiDeduction = taxCalculator.calculateEI(projectedAnnualEarnings, eiExemptCheckBox.isSelected()) / periodsPerYear;

                    double ympe2024 = 68500.0;
                    double ympe2025 = 71300.0;
                    double taxYear = 0.0;

                    if (convertYearToDbName(yearComboBox).equalsIgnoreCase("twenty_twenty_four")) {
                        taxYear = 2024;
                        // System.out.println("THIS IS USING 2024");
                    } 
                    
                    if (convertYearToDbName(yearComboBox).equalsIgnoreCase("twenty_twenty_five")) {
                        taxYear = 2025;
                        //System.out.println("THIS IS USING 2025");
                    } 

                    cppDeduction = new BigDecimal(cppDeduction).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    eiDeduction  = new BigDecimal(eiDeduction).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    //System.out.println(period + " " + cppDeduction + " CPP DEDUCTION FOR FIRST");
                    double ympe = (taxYear == 2025) ? ympe2025 : ympe2024;
                    
                    double cpp2Deduction = 0.0;
                    double maxCPP2Contribution = 0.0;

                    if (convertYearToDbName(yearComboBox).equalsIgnoreCase("twenty_twenty_five")) {
                        maxCPP2Contribution = 396.0; // Max annual employee and employer contribution for 2024
                    } else if (convertYearToDbName(yearComboBox).equalsIgnoreCase("twenty_twenty_four")) {
                        maxCPP2Contribution = 188.0; // Max annual employee and employer contribution for 2025
                    } else {
                        maxCPP2Contribution = 0.0; // Default value if no match is found (you could handle this with an error message if necessary)
                    }

                    double originalCPPDifference = 0.0;
                    double ytdandCPP = ytdCPP + cppDeduction;

                    if ((ytdandCPP > maxCPPContribution) && lastround == true) {
                        originalCPPDifference = cppDeduction - (maxCPPContribution - ytdCPP);

                        cppDeduction = maxCPPContribution - ytdCPP;

                        undoingCPP = cppDeduction;
                        cppDeduction = new BigDecimal(cppDeduction).setScale(2, RoundingMode.HALF_UP).doubleValue();

                        if (cppDeduction + ytdCPP != maxCPPContribution) {
                            double difference = maxCPPContribution - (cppDeduction + ytdCPP);
                            cppDeduction += difference;
                        }
                        
                        lastround = false;

                        double cppRate = getCppRate(convertYearToDbName(yearComboBox));

                        double cpp2EligibleIncome = (originalCPPDifference / cppRate)*periodsPerYear;
                        cpp2Deduction = new BigDecimal(taxCalculator.calculatePartialCPP2(cpp2EligibleIncome, convertYearToDbName(yearComboBox))).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        cpp2Deduction = cpp2Deduction / periodsPerYear;
                    }

                    if (ytdCPP >= maxCPPContribution) {
                        cppDeduction = 0.0;
                        //System.out.println(period + " CPP Original set to 0");
                    }

                    if (ytdCPP2 > 0) {
                        //System.out.println(projectedAnnualEarnings);
                        cpp2Deduction = new BigDecimal(taxCalculator.calculateCPP2(projectedAnnualEarnings)).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        cpp2Deduction = cpp2Deduction / periodsPerYear;
                        //System.out.println(period + "amount CPP 2 " + cpp2Deduction);
                    }
                    

                    if (ytdCPP2 + cpp2Deduction > maxCPP2Contribution) {
                        cpp2Deduction = maxCPP2Contribution - ytdCPP2;
                        cpp2Deduction = new BigDecimal(cpp2Deduction).setScale(2, RoundingMode.HALF_UP).doubleValue();

                        if (cpp2Deduction + ytdCPP2 != maxCPP2Contribution) {
                            double difference = maxCPP2Contribution - (cpp2Deduction + ytdCPP2);
                            cpp2Deduction += difference;
                        }

                    }

                    maxCPPContribution = maxCPPContributionOG;

                    if (ytdEI + eiDeduction > maxEIContribution) {
                        eiDeduction = maxEIContribution - ytdEI;
                        eiDeduction = Math.floor(eiDeduction * 100.0) / 100.0;
                    
                        // Double-check and adjust if necessary
                        if (eiDeduction + ytdEI != maxEIContribution) {
                            double difference = maxEIContribution - (eiDeduction + ytdEI);
                            eiDeduction += difference;
                        }
                    }
                    
                    

                    double provincialPayrollTax = taxCalculator.calculateProvincialPayrollTax(projectedAnnualEarnings, provinceCode, periodsPerYear, convertYearToDbName(yearComboBox));
                    provincialPayrollTax = new BigDecimal(provincialPayrollTax).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    
                    double provincialPayrollTaxUnrounded = taxCalculator.calculateProvincialPayrollTax(projectedAnnualEarningsUnrounded, provinceCode, periodsPerYear, convertYearToDbName(yearComboBox));
                    provincialPayrollTaxUnrounded = new BigDecimal(provincialPayrollTaxUnrounded).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    
                    double federalPayrollTax = taxCalculator.calculateFederalPayrollTax(projectedAnnualEarnings, "can", periodsPerYear, convertYearToDbName(yearComboBox));
                    federalPayrollTax = new BigDecimal(federalPayrollTax).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    
                    double federalPayrollTaxUnrounded = taxCalculator.calculateFederalPayrollTax(projectedAnnualEarningsUnrounded, "can", periodsPerYear, convertYearToDbName(yearComboBox));
                    federalPayrollTaxUnrounded = new BigDecimal(federalPayrollTaxUnrounded).setScale(2, RoundingMode.HALF_UP).doubleValue();

                    double totalProvinceExemption = taxCalculator.calculateTotalProvinceExemption(provinceCode, 1, convertYearToDbName(yearComboBox));

                    double totalFederalExemption = taxCalculator.calculateTotalFederalExemptions(provinceCode, 1, convertYearToDbName(yearComboBox));


                    if (td1Status == true) {
                        totalProvinceExemption = totalProvinceExemption * 2;
                        totalFederalExemption = totalFederalExemption * 2;
                    }

                    provincialPayrollTax = provincialPayrollTax - (totalProvinceExemption/periodsPerYear);
                    federalPayrollTax = federalPayrollTax - (totalFederalExemption/periodsPerYear);

                    // If the exemption is too big it will result in the value being negative, this is not possible so it will instead be set to 0.
                    if (provincialPayrollTax < 0) {
                        provincialPayrollTax = 0.0;
                    }

                    if (federalPayrollTax < 0) {
                        federalPayrollTax = 0.0;
                    }

                    double payrollTax = BigDecimal.valueOf(provincialPayrollTax + federalPayrollTax).setScale(2, RoundingMode.HALF_UP).doubleValue();



                    double totalDeductions = cppDeduction + eiDeduction + payrollTax;
                    double netPay = totalEarnings - totalDeductions;

                    ytdEarnings += earningsCurrent;
                    ytdTotalEarnings += totalEarnings;
                    ytdVacationPay += vacationPay;
                    ytdBonus += bonus;
                    ytdCPP += cppDeduction;
                    ytdCPP2 += cpp2Deduction;
                    //System.out.println("YTD CPP 2 "  + cpp2Deduction);
                    ytdEI += eiDeduction;
                    ytdPayrollTax += payrollTax;
                    ytdNetPay += netPay;
                    ytdMealAllowance += mealAllowance;
                    // ytdCPPEarnings += tdCPPEarnings;

                    System.out.println("Elements of CRA Remittance | " + "INCOME TAX " + payrollTax  + " | CPPDeduction * 2" + (cppDeduction*2) + "| CPP2 * 2 " + (cpp2Deduction*2) + "| EI REG " + eiDeduction + "| EI 1.4" + (eiDeduction * 1.4));

                    double craRemittance = payrollTax + (cppDeduction * 2) + (cpp2Deduction * 2)+ eiDeduction + (eiDeduction * 1.4);
                    double ctc = craRemittance + totalEarnings;
                    // This is all of the date calculations for when the payment is going to occur, this is very important in deciding the payment date as for things like biweekly payments it should always be on fridays.
                    switch (paymentFrequency) {
                        case "Weekly (52)":
                        case "Weekly (53)":
                            endDate = startDate.plusDays(6);
                            endDate = adjustToFriday(endDate);
                            break;
                    
                        case "Bi-Weekly (26)":
                        case "Bi-Weekly (27)":
                            endDate = startDate.plusDays(13);
                            endDate = adjustToFriday(endDate);
                            break;
                    
                        case "Semi-Monthly (24)":
                            if (startDate.getDayOfMonth() <= 15) {
                                endDate = startDate.withDayOfMonth(15);
                            } else {
                                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                            }
                            break;
                    
                        case "Monthly (12)":
                            endDate = startDate.plusDays(27).with(TemporalAdjusters.lastDayOfMonth());
                            break;
                    
                        case "Quarterly (4)":
                            endDate = startDate.plusDays(84).with(TemporalAdjusters.lastDayOfMonth());
                            break;
                    
                        case "Semi-Annually (2)":
                            endDate = startDate.plusDays(180).with(TemporalAdjusters.lastDayOfMonth());
                            break;
                    
                        case "Yearly (1)":
                            endDate = startDate.plusDays(350).with(TemporalAdjusters.lastDayOfMonth());
                            break;
                    
                        default:
                            throw new IllegalArgumentException("Unknown payment frequency");
                    }
                    

                    if (endDate.isAfter(generationEndDate)) {
                        break;
                    }

                    String payPeriodStart = startDate.format(dateFormatter);
                    String payPeriodEnd = endDate.format(dateFormatter);
                    // Sets the insert statement which will be used to set the values of the table inside of the program. 
                    String insertSQL = "INSERT INTO " + tableName + " (sl_no, pay_date, hours_worked, rate_per_hour, "
                            + "basic_amount, vacation_pay, total_earnings, federal_tax, provincial_tax, income_tax, "
                            + "cpp, cpp2, ei, total_deductions, net_pay, cra_remittance, "
                            + "has_overtime, overtime_hours, overtime_rate, use_base_amount, base_amount, paid, meal_Allowance, amount_bonus, ctc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                        pstmt.setInt(1, depositNumberDouble);
                        pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                        pstmt.setDouble(3, hours);
                        pstmt.setDouble(4, rate);
                        pstmt.setDouble(5, earningsCurrent);
                        pstmt.setDouble(6, vacationPay);
                        pstmt.setDouble(7, totalEarnings);
                        pstmt.setDouble(8, federalPayrollTax);
                        pstmt.setDouble(9, provincialPayrollTax);
                        pstmt.setDouble(10, payrollTax);
                        pstmt.setDouble(11, cppDeduction);
                        pstmt.setDouble(12, cpp2Deduction);
                        pstmt.setDouble(13, eiDeduction);
                        pstmt.setDouble(14, totalDeductions);
                        pstmt.setDouble(15, netPay);
                        pstmt.setDouble(16, craRemittance);
                        //System.out.println("Overtime Selected: " + hasOvertimeCheckBox.isSelected());
                        pstmt.setBoolean(17, hasOvertimeCheckBox.isSelected());

                        if (isFirstRow) {
                            pstmt.setDouble(18, hasOvertimeCheckBox.isSelected() ? Double.parseDouble(overTimeHours.getText()) : 0.0);
                        }
                        else {
                            pstmt.setDouble(18, 0.0);
                        }
                        pstmt.setDouble(19, overtimeRate);
                        pstmt.setBoolean(20, useBaseAmountCheckBox.isSelected());
                        pstmt.setDouble(21, useBaseAmountCheckBox.isSelected() ? Double.parseDouble(baseAmountField.getText()): 0.0);
                        pstmt.setBoolean(22, false);
                        pstmt.setDouble(23, mealAllowance);
                        if (isFirstRow) {
                            pstmt.setDouble(24, bonus);
                        }
                        else{
                            pstmt.setDouble(24, 0.0);
                        }
                        pstmt.setDouble(25, ctc);
                        pstmt.executeUpdate();
                    }

                    /// Writing portion of the program which allows the CSV file to be generated in the exact formatting 
                    /// which was needed by the client. 
                    writer.append("");
                    writer.append(employeeName).append(",,,,,,").append(employerName).append("\n");
                    writer.append(employeeAddress).append(",,,,,,").append(employerAddress).append("\n");
                    writer.append(employeeCity).append(" ").append(provinceCode).append(" ").append(employeePostalCode).append(",,,,,,").append(employerCity).append(" ").append(employerProvinceCode).append(" ").append(employerPostalCode).append("\n\n\n");

                    writer.append("EMPLOYEE NO.,").append(employeeNumber).append(",,,,SIN,XXX-XXX-").append(lastThreeSIN).append("\n");
                    writer.append("PAY PERIOD,").append(payPeriodStart).append(",TO,").append(payPeriodEnd).append(",,DEPOSIT NO.,").append(depositNumber).append("\n");
                    writer.append("DATE OF PAYMENT\n\n");

                    depositNumberDouble++;

                    writer.append("EARNINGS,,,,,DEDUCTION\n");
                    writer.append("DESCRIPTION,RATE,HOURS,CURRENT,YTD,DESCRIPTION,CURRENT,YTD\n");

                    writer.append("REGULAR EARNING,").append(String.format("$%.2f", rate)).append(",").append(String.format("%.2f", hours)).append(",").append(String.format("$%.2f", earningsCurrent)).append(",").append(String.format("$%.2f", ytdEarnings)).append(",").append("INCOME TAX,").append(String.format("$%.2f", payrollTax)).append(",").append(String.format("$%.2f", ytdPayrollTax)).append("\n");
                    // CSV File needs to be set up differently if there is overtime hours involved of they are not involved.
                    if (hasOvertimeCheckBox.isSelected()) {
                        String overtimeRateString = overtimeRate + "";
                        String overtimeHoursString = hoursOvertime + "";
                        String overtimeMoneyString = overTimeMoney + "";
                        writer.append("OVERTIME EARNINGS,$").append(overtimeRateString).append("$").append(overtimeHoursString).append(",$").append(overtimeMoneyString).append("$").append(overtimeMoneyString).append(",C.P.P,").append(String.format("$%.2f", cppDeduction)).append(",").append(String.format("$%.2f", ytdCPP)).append("\n");
                        writer.append("VACATION PAY,,").append(",").append(String.format("$%.2f", vacationPay)).append(",").append(String.format("$%.2f", ytdVacationPay)).append(",E.I,").append(String.format("$%.2f", eiDeduction)).append(",").append(String.format("$%.2f", ytdEI)).append("\n");

                        writer.append("BONUS,,").append(",").append(String.format("$%.2f", bonus)).append(",").append(String.format("$%.2f", ytdBonus)).append("\n");

                        writer.append("TOTAL,,").append(",").append(String.format("$%.2f", totalEarnings)).append(",").append(String.format("$%.2f", ytdEarnings + ytdVacationPay + ytdBonus)).append(",,").append(String.format("$%.2f", totalDeductions)).append(",").append(String.format("$%.2f", ytdCPP + ytdEI + ytdPayrollTax)).append("\n");

                        writer.append("NET PAY,,").append(",,,").append(String.format("$%.2f", netPay)).append(",").append(String.format("$%.2f", ytdNetPay)).append("\n\n");
                    } else {
                        writer.append("VACATION PAY,,").append(",").append(String.format("$%.2f", vacationPay)).append(",").append(String.format("$%.2f", ytdVacationPay)).append(",C.P.P,").append(String.format("$%.2f", cppDeduction)).append(",").append(String.format("$%.2f", ytdCPP)).append("\n");

                        writer.append("BONUS,,").append(",").append(String.format("$%.2f", bonus)).append(",").append(String.format("$%.2f", ytdBonus)).append(",E.I,").append(String.format("$%.2f", eiDeduction)).append(",").append(String.format("$%.2f", ytdEI)).append("\n");

                        writer.append("TOTAL,,").append(",").append(String.format("$%.2f", totalEarnings)).append(",").append(String.format("$%.2f", ytdEarnings + ytdVacationPay + ytdBonus)).append(",,").append(String.format("$%.2f", totalDeductions)).append(",").append(String.format("$%.2f", ytdCPP + ytdEI + ytdPayrollTax)).append("\n");

                        writer.append("NET PAY,,").append(",,,").append(String.format("$%.2f", netPay)).append(",").append(String.format("$%.2f", ytdNetPay)).append("\n\n");
                    }

                    writer.append("\n").append(employerName).append(",,,BANK\n");
                    writer.append(employerAddress).append(",,,BRANCH\n");
                    writer.append(employerCity).append(" ").append(employerPostalCode).append(",,,CITY\n\n,,,FINANCIAL INST NO.\n,,,TRANSIT NO.\n");

                    writer.append(employeeName).append(",,,ACCOUNT NO.\n");
                    writer.append(employeeAddress).append(",,,AMOUNT,,$").append(String.format("%.2f", netPay)).append("\n");
                    writer.append(employeeCity).append(" ").append(employeePostalCode).append(",,,DATE\n\n\n\n\nPAYROLL ADVICE ONLY-NON NEGOTIABLE\n\n\n");

                    startDate = endDate.plusDays(1);
                }

                JOptionPane.showMessageDialog(this, "Yearly CSV generated successfully.");

            } catch (IOException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error generating CSV file or interacting with the database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();  // ✅ This prints the full error stack trace in terminal
            JOptionPane.showMessageDialog(this, "Most Likley All Required Fields Not Filled" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public double getCppRate(String taxYearDbName) {
        if (taxYearDbName.equalsIgnoreCase("twenty_twenty_five")) return 0.0595;
        if (taxYearDbName.equalsIgnoreCase("twenty_twenty_four")) return 0.0595;
        return 0.0595; // default fallback
    }

    // Method which is used by the program to make sure that the payment is made on a friday for those payment schedules which require it 
    private LocalDate adjustToFriday(LocalDate date) {
        while (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    // This is the method which amkes the buttons inside of the program styled and makes it visually appealing. 
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
        BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        return button;
    }

    public void resetPage(JPanel parentPanel, CardLayout cardLayout) {
        parentPanel.remove(this); // Remove current instance
        PayrollInformationPage newPage = new PayrollInformationPage(parentPanel, cardLayout); // Create a new instance
        parentPanel.add(newPage, "Payroll"); // Add the new instance
        cardLayout.show(parentPanel, "Payroll"); // Show the new page
        parentPanel.revalidate();
        parentPanel.repaint();
    }
    

    // This is the method which adds all of the GUI components of textfields into the program, this is what adds it the panel and makes the attributes visible. 
    private void addEmployerFields(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension labelDimension = new Dimension(200, 30);
        Dimension fieldDimension = new Dimension(250, 30);
        int currentRow = 0;

        employerCompanyNameComboBox = new JComboBox<>();
        employerCompanyNameComboBox.setEditable(true);

        populateEmployerCompanyNameComboBox();
        employerAddressField = generalMethods.createPlaceHolderTextField("Enter employer's address");
        employerCityField = generalMethods.createPlaceHolderTextField("Enter employer's city");
        employerPostalCodeField = generalMethods.createPlaceHolderTextField("Enter postal code");
        employerRPField = generalMethods.createPlaceHolderTextField("Enter RP Number");

        addFormField(panel, gbc, currentRow, 0, "Employer Company Name:", employerCompanyNameComboBox, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Employer Address:", employerAddressField, labelDimension, fieldDimension);

        addFormField(panel, gbc, currentRow, 0, "Employer City:", employerCityField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Employer Postal Code:", employerPostalCodeField, labelDimension, fieldDimension);

        employerProvinceField = new JComboBox<>(new String[] {"Alberta", "British Columbia", "Manitoba", "New Brunswick", "Newfoundland and Labrador", "Nova Scotia", "Ontario", "Prince Edward Island", "Saskatchewan", "Northwest Territories", "Nunavut", "Yukon"});

        addFormField(panel, gbc, currentRow, 0, "Employer Province:", employerProvinceField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Employer RP Number", employerRPField, labelDimension, fieldDimension);
    }

    // This is the method which changes the employee name to the combobox if there is a list of employees already which can be used as a database.
    private void replaceEmployeeNameFieldWithComboBox() {
        if (employeeFullNameComboBox == null) {
            employeeFullNameComboBox = new JComboBox<>();
            employeeFullNameComboBox.setEditable(true);
            populateEmployeeFullNameComboBox();
            employeeComboBoxInitialized = true;
        }

        JPanel parentPanel = (JPanel) employeeFullNameField.getParent();
        parentPanel.remove(employeeFullNameField);
        addFormField(parentPanel, new GridBagConstraints(), 0, 0, "Employee Full Name:", employeeFullNameComboBox,
        new Dimension(200, 30), new Dimension(250, 30));
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    // This is the what populates the combobox inside of the GUI and makes it have elements. This is essential to the use of the program.
    private void populateEmployeeFullNameComboBox() {
        if (employeeFullNameComboBox == null) {
            return;
        }

        employeeFullNameComboBox.removeAllItems();
        String companyName = getCompanyNameTable();
        String companyNameRegular = getCompanyName(); 
        int companyID = getCompanyIdByName(companyNameRegular);
        
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT employee_name FROM ALLEMP_" + companyID;

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                employeeFullNameComboBox.addItem(resultSet.getString("employee_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Adds the action listeners and buttons to the panel, this is what allows the GUI be visible with accurate elements. 
    private void addEmployeeFields(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension labelDimension = new Dimension(200, 30);
        Dimension fieldDimension = new Dimension(250, 30);

        int currentRow = 0;

        if (checkEmployeeTableExists()) {
            employeeFullNameComboBox = new JComboBox<>();
            employeeFullNameComboBox.setEditable(true);
            populateEmployeeFullNameComboBox();
            addFormField(panel, gbc, currentRow, 0, "Employee Full Name:", employeeFullNameComboBox, labelDimension, fieldDimension);
        } else {
            employeeFullNameField = generalMethods.createPlaceHolderTextField("Enter employee's full name");
            addFormField(panel, gbc, currentRow, 0, "Employee Full Name:", employeeFullNameField, labelDimension, fieldDimension);
        }
        employeeSINField = generalMethods.createPlaceHolderTextField("Enter employee's SIN");
        employeeAddressField = generalMethods.createPlaceHolderTextField("Enter employee's address");
        employeeCityField = generalMethods.createPlaceHolderTextField("Enter employee's city");
        employeePostalCodeField = generalMethods.createPlaceHolderTextField("Enter postal code");
        employeeNumberField = generalMethods.createPlaceHolderTextField("Enter employee number");

        addFormField(panel, gbc, currentRow++, 1, "Employee SIN:", employeeSINField, labelDimension, fieldDimension);

        addFormField(panel, gbc, currentRow, 0, "Employee Address:", employeeAddressField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Employee City:", employeeCityField, labelDimension, fieldDimension);

        addFormField(panel, gbc, currentRow, 0, "Employee Postal Code:", employeePostalCodeField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Employee Number:", employeeNumberField, labelDimension, fieldDimension);

        employeeProvinceField = new JComboBox<>(new String[] {
                "Alberta", "British Columbia", "Manitoba", "New Brunswick", "Newfoundland and Labrador", "Nova Scotia", "Ontario", "Prince Edward Island", "Saskatchewan", "Northwest Territories", "Nunavut", "Yukon"});

        addFormField(panel, gbc, currentRow, 0, "Employee Province:", employeeProvinceField, labelDimension, fieldDimension);

        depositNumberField = generalMethods.createPlaceHolderTextField("Enter deposit number");
        overTimeHours = generalMethods.createPlaceHolderTextField("Enter overtime hours worked");

        overTimeHours.setEnabled(false);

        addFormField(panel, gbc, currentRow, 1, "Deposit No.:", depositNumberField, labelDimension, fieldDimension);

        paymentFrequencyField = new JComboBox<>(new String[] {"Weekly (52)", "Weekly (53)", "Bi-Weekly (26)", "Bi-Weekly (27)", "Semi-Monthly (24)", "Monthly (12)", "Quarterly (4)", "Semi-Annually (2)", "Yearly (1)"});

        addFormField(panel, gbc, currentRow++ + 1, 0, "Employee's Payment Frequency:", paymentFrequencyField, labelDimension, fieldDimension);

        addFormField(panel, gbc, currentRow++, 1, "Overtime Hours:", overTimeHours, labelDimension, fieldDimension);
        rateOfPaymentField = generalMethods.createPlaceHolderTextField("Enter rate of payment");
        amountOfHoursField = generalMethods.createPlaceHolderTextField("Enter hours worked");
        employeeBonusField = generalMethods.createPlaceHolderTextField("Enter bonus amount");
        baseAmountField = generalMethods.createPlaceHolderTextField("Enter base amount");

        addFormField(panel, gbc, currentRow, 0, "Rate of Payment:", rateOfPaymentField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Amount of Hours:", amountOfHoursField, labelDimension, fieldDimension);

        addFormField(panel, gbc, currentRow, 0, "Employee Bonus Amount:", employeeBonusField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Base Amount:", baseAmountField, labelDimension, fieldDimension);

        yearComboBox = new JComboBox<>(new String[] {"2025", "2024"});
        baseAmountField.setEnabled(false);

        beginPayPeriodField = generalMethods.createPlaceHolderTextField("Enter beginning of pay period (YYYY/MM/DD)");
        endPayPeriodField = generalMethods.createPlaceHolderTextField("Enter ending of pay period (YYYY/MM/DD) - Optional");
        addFormField(panel, gbc, currentRow, 0, "Beginning of Pay Period:", beginPayPeriodField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Ending of Pay Period (Optional):", endPayPeriodField, labelDimension,fieldDimension);

        joiningDateField = generalMethods.createPlaceHolderTextField("Enter joining date (YYYY/MM/DD) - Optional");
        addFormField(panel, gbc, currentRow, 0, "Enter joining date (YYYY/MM/DD) - Optional", joiningDateField, labelDimension, fieldDimension);
        addFormField(panel, gbc, currentRow++, 1, "Year of Payroll", yearComboBox, labelDimension, fieldDimension);

        employeeMealAllowanceField = generalMethods.createPlaceHolderTextField("Enter meal allowance (optional)");
        addFormField(panel, gbc, currentRow++, 0, "Meal Allowance:", employeeMealAllowanceField, labelDimension, fieldDimension);
        


        cppExemptCheckBox = new JCheckBox("CPP Exempt");
        eiExemptCheckBox = new JCheckBox("EI Exempt");
        hasOvertimeCheckBox = new JCheckBox("Has Overtime");
        useBaseAmountCheckBox = new JCheckBox("Use Base Amount");
        useYearlyCheckBox = new JCheckBox("Use Yearly Lump Sum");
        employeeTD1CheckBox = new JCheckBox("Use TD1");

        cppExemptCheckBox.setBackground(Color.WHITE);
        eiExemptCheckBox.setBackground(Color.WHITE);
        hasOvertimeCheckBox.setBackground(Color.WHITE);
        useBaseAmountCheckBox.setBackground(Color.WHITE);
        useYearlyCheckBox.setBackground(Color.WHITE);
        employeeTD1CheckBox.setBackground(Color.WHITE);

        gbc.gridy = currentRow++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(cppExemptCheckBox, gbc);

        gbc.gridx = 1;
        panel.add(eiExemptCheckBox, gbc);

        gbc.gridx = 2;
        panel.add(hasOvertimeCheckBox, gbc);

        gbc.gridx = 3;
        panel.add(useBaseAmountCheckBox, gbc);

        gbc.gridy = currentRow++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        panel.add(useYearlyCheckBox, gbc);

        gbc.gridx = 1;
        panel.add(employeeTD1CheckBox, gbc);

        hasOvertimeCheckBox.addActionListener(e -> {
            overTimeHours.setEnabled(hasOvertimeCheckBox.isSelected());
            overTimeHours.setText(hasOvertimeCheckBox.isSelected() ? "" : "");
            overTimeHours.setForeground(hasOvertimeCheckBox.isSelected() ? Color.BLACK : Color.GRAY);
        });

        useYearlyCheckBox.addActionListener(e -> {
            boolean useYearly = useYearlyCheckBox.isSelected();
            rateOfPaymentField.setEnabled(!useYearly);
            rateOfPaymentField.setForeground(useYearly ? Color.GRAY : Color.BLACK);
            if (useYearly) {
                rateOfPaymentField.setText("Disabled in Yearly Mode");
            } else {
                rateOfPaymentField.setText("");
            }

            amountOfHoursField.setEnabled(!useYearly);
            amountOfHoursField.setForeground(useYearly ? Color.GRAY : Color.BLACK);
            if (useYearly) {
                amountOfHoursField.setText("Disabled in Yearly Mode");
            } else {
                amountOfHoursField.setText("");
            }

            baseAmountField.setEnabled(useYearly);
            useBaseAmountCheckBox.setEnabled(!useYearly);

        });

        useBaseAmountCheckBox.addActionListener(e -> {
            boolean useBaseAmount = useBaseAmountCheckBox.isSelected();
            baseAmountField.setEnabled(useBaseAmount);
            rateOfPaymentField.setEnabled(!useBaseAmount);
            amountOfHoursField.setEnabled(!useBaseAmount);
            hasOvertimeCheckBox.setEnabled(!useBaseAmount);
            overTimeHours.setEnabled(!useBaseAmount && hasOvertimeCheckBox.isSelected());
            rateOfPaymentField.setForeground(useBaseAmount ? Color.GRAY : Color.BLACK);
            amountOfHoursField.setForeground(useBaseAmount ? Color.GRAY : Color.BLACK);
            baseAmountField.setForeground(useBaseAmount ? Color.BLACK : Color.GRAY);

            if (useBaseAmount) {
                overTimeHours.setText("Enter overtime hours worked");
                overTimeHours.setForeground(Color.GRAY);
            }
        });

        useBaseAmountCheckBox.addActionListener(e -> {
            boolean useBaseAmount = useBaseAmountCheckBox.isSelected();
            baseAmountField.setEnabled(useBaseAmount);
            rateOfPaymentField.setEnabled(!useBaseAmount);
            amountOfHoursField.setEnabled(!useBaseAmount);
            rateOfPaymentField.setForeground(useBaseAmount ? Color.GRAY : Color.BLACK);
            amountOfHoursField.setForeground(useBaseAmount ? Color.GRAY : Color.BLACK);
            baseAmountField.setForeground(useBaseAmount ? Color.BLACK : Color.GRAY);
        });

    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, int col, String labelText, JComponent field, Dimension labelDim, Dimension fieldDim) {
        int startX = col * 2; 
        gbc.gridx = startX;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setPreferredSize(labelDim);
        panel.add(label, gbc);
        gbc.gridx = startX + 1;
        field.setPreferredSize(fieldDim);
        panel.add(field, gbc);
    }

    // Returns the name of the company if it is a textfield or a combobox, it also gives it in the regular format it is written in originally. 
    private String getCompanyName() {
        String employerName;

        if (employerCompanyNameComboBox != null && employerCompanyNameComboBox.isEditable()) {
            employerName = ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).getText().trim();
        } else {
            employerName = employerAddressField.getText().trim();
        }

        return employerName;
    }

    // Returns the name of the company with replaced _ this is essential to the functioning of the SQL table system which will not allow renaming otherwise.
    private String getCompanyNameTable() {
        String employerName;

        if (employerCompanyNameComboBox != null && employerCompanyNameComboBox.isEditable()) {
            employerName = ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).getText().trim();
        } else {
            employerName = employerAddressField.getText().trim();
        }

        return employerName.replaceAll("\\W+", "_");
    }    

    // Returns the employee name with _ this is essential for the SQL system.
    private String getEmployeeNameTable() {
        String rawName;

        if (employeeFullNameComboBox != null && employeeComboBoxInitialized) {
            rawName = ((JTextField) employeeFullNameComboBox.getEditor().getEditorComponent()).getText().trim();
        } else {
            rawName = employeeFullNameField.getText().trim();
        }

        return rawName.replaceAll("\\W+", "_");
    }

    // Returns the employee name in the same format which it is written inside of in the GUI textfield.
    private String getEmployeeName() {
        if (employeeFullNameComboBox != null && employeeComboBoxInitialized) {
            return employeeFullNameComboBox.getSelectedItem().toString().trim();
        } else {
            return employeeFullNameField.getText().trim();
        }
    }

    // This is what allows the program to calculate tax'es and 
    public static String convertYearToDbName(JComboBox<String> yearComboBox) {
        String selectedYear = (String) yearComboBox.getSelectedItem();

        switch (selectedYear) {
            case "2024":
                return "twenty_twenty_four";
            case "2025":
                return "twenty_twenty_five";
            default:
                throw new IllegalArgumentException("Invalid year selected: " + selectedYear);
        }
    }

    // This allows the program to get the amount of max contribution for the CPP and EI based on the name. This allows for making sure you dont overcontribute.
    public double getMaxContribution(String contributionType) {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT max_amount FROM maxcontributions_" + convertYearToDbName(yearComboBox) + " WHERE contribution_type = ?";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, contributionType);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("max_amount");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving max contribution for " + contributionType + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0.0;
    }

    // This opening of table makes it autofill the employer information into the table, moreover it makes sure that all of the text which is input is the colour black as that is important for the look of the program. 
    private void openMySQLTable() {
        String companyName = getCompanyNameTable();
        String tableNamePrefix = "EmplI_";
        String companyNameRegular = getCompanyName();
        int companyID = getCompanyIdByName(companyNameRegular);

        String tableName = tableNamePrefix + companyID;
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String selectDataSQL = "SELECT EmployerCompanyName, EmployerCity, EmployerProvince, EmployerAddress, EmployerPostalCode, EmployerRONumber FROM " + tableName + " LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectDataSQL)) {

            if (resultSet.next()) {
                ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).setText(resultSet.getString("EmployerCompanyName"));
                ((JTextField) employerCompanyNameComboBox.getEditor().getEditorComponent()).setForeground(Color.BLACK);

                employerCityField.setText(resultSet.getString("EmployerCity"));
                employerCityField.setForeground(Color.BLACK);

                employerProvinceField.setSelectedItem(resultSet.getString("EmployerProvince"));

                employerAddressField.setText(resultSet.getString("EmployerAddress"));
                employerAddressField.setForeground(Color.BLACK);

                employerPostalCodeField.setText(resultSet.getString("EmployerPostalCode"));
                employerPostalCodeField.setForeground(Color.BLACK);

                employerRPField.setText(resultSet.getString("EmployerRONumber"));
                employerRPField.setForeground(Color.BLACK);

                JOptionPane.showMessageDialog(this, "Employer information loaded successfully from the table '" + tableName + "'.");
                replaceEmployeeNameFieldWithComboBox();

            } else {
                JOptionPane.showMessageDialog(this,
                        "No data found in table '" + tableName + "' in the database '" + tableName + "'.", "No Data",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing table '" + tableName + "' in database '" + tableName + "': " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This is what sets up the autofilling of the companies for the program which initalizes and sets up the combobox.
    private void populateEmployerCompanyNameComboBox() {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT company_name FROM ALL_COMPANIES";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String companyName = resultSet.getString("company_name");
                employerCompanyNameComboBox.addItem(companyName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This creates the SQL table which will store all of the employee information, this is important as this information and the table which is created are repeatedly used inside of the future. 
    private void createEmployeeSQLTable() {
        String companyName = getCompanyNameTable();
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();

        int companyID = getCompanyIdByName(companyNameRegular);


        String tableNamePrefix = employeeName + "_";
        String tableName = tableNamePrefix + companyID;
        
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
        String createTableSQL = "CREATE TABLE " + tableName + " ("
                + "FullName VARCHAR(255), "
                + "Address VARCHAR(255), "
                + "PostalCode VARCHAR(255), "
                + "Province VARCHAR(255), "
                + "Frequency VARCHAR(255), "
                + "AmountOfHours VARCHAR(255), "
                + "EmployeeSIN VARCHAR(255), "
                + "EmployeeCity VARCHAR(255), "
                + "EmployeeNumber VARCHAR(255), "
                + "DepositNo VARCHAR(255), "
                + "RateOfPayment VARCHAR(255), "
                + "EmployeeAmountBonus DOUBLE, "
                + "CPPExempt BOOLEAN, "
                + "EIExempt BOOLEAN, "
                + "HasOvertime BOOLEAN, "
                + "OvertimeHours DOUBLE, "
                + "UseBaseAmount BOOLEAN, "
                + "UseYearlyLumpSum BOOLEAN, "
                + "UseTD1 BOOLEAN, "
                + "BaseAmount DOUBLE, "
                + "meal_Allowance DOUBLE)";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement()) {
    
            // Drop the table if it already exists
            statement.executeUpdate(dropTableSQL);
            statement.executeUpdate(createTableSQL);
    
            String insertDataSQL = "INSERT INTO " + tableName
                    + " (FullName, Address, PostalCode, Province, Frequency, "
                    + "AmountOfHours, EmployeeSIN, EmployeeCity, EmployeeNumber, DepositNo, RateOfPayment, "
                    + "EmployeeAmountBonus, CPPExempt, EIExempt, HasOvertime, OvertimeHours, UseBaseAmount, UseYearlyLumpSum, UseTD1, BaseAmount, meal_Allowance) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
                String employeeNameList = getEmployeeName();
                addEmployeeToAllEmployees(employeeNameList);
    
                preparedStatement.setString(1, employeeNameList);
                preparedStatement.setString(2, employeeAddressField.getText());
                //System.out.println(employeeAddressField.getText());
                preparedStatement.setString(3, employeePostalCodeField.getText());
                preparedStatement.setString(4, (String) employeeProvinceField.getSelectedItem());
                preparedStatement.setString(5, (String) paymentFrequencyField.getSelectedItem());
    
                String paymentFrequency = (String) paymentFrequencyField.getSelectedItem();
                int periodsPerYear;
                double hoursPerPeriod = 0.0;
    
                switch (paymentFrequency) {
                    case "Weekly (52)":
                        periodsPerYear = 52;
                        break;
                    case "Weekly (53)":
                        periodsPerYear = 53;
                        break;
                    case "Bi-Weekly (26)":
                        periodsPerYear = 26;
                        break;
                    case "Bi-Weekly (27)":
                        periodsPerYear = 27;
                        break;
                    case "Semi-Monthly (24)":
                        periodsPerYear = 24;
                        break;
                    case "Monthly (12)":
                        periodsPerYear = 12;
                        break;
                    case "Quarterly (4)":
                        periodsPerYear = 4;
                        break;
                    case "Semi-Annually (2)":
                        periodsPerYear = 2;
                        break;
                    case "Yearly (1)":
                        periodsPerYear = 1;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown payment frequency");
                }
                
                try {
                    hoursPerPeriod = useYearlyCheckBox.isSelected() ? 2080.0 / periodsPerYear : Double.parseDouble(amountOfHoursField.getText());
                } catch (Exception e) {
                    //System.out.println("cannot parse hours");
                }
                


                double rateOfPayment = 0.00;
                String  amountOfHours = "";

                if(useBaseAmountCheckBox.isSelected()){
                    rateOfPayment = 0.00;
                    amountOfHours = "0.00";
                } else {
                    rateOfPayment = Double.parseDouble(rateOfPaymentField.getText());
                    amountOfHours = String.format("%.2f", hoursPerPeriod);
                }

                double doubleMealAllowance = 0.0;

                try{
                    doubleMealAllowance = Double.parseDouble(employeeMealAllowanceField.getText());
                } catch (Exception e){
                    doubleMealAllowance = 0.0;
                }

                preparedStatement.setString(6, amountOfHours);
                preparedStatement.setString(7, employeeSINField.getText());
                preparedStatement.setString(8, employeeCityField.getText());
                preparedStatement.setString(9, employeeNumberField.getText());
                preparedStatement.setString(10, depositNumberField.getText());
                preparedStatement.setDouble(11, rateOfPayment);
                preparedStatement.setDouble(12, Double.parseDouble(employeeBonusField.getText()));
                preparedStatement.setBoolean(13, cppExemptCheckBox.isSelected());
                preparedStatement.setBoolean(14, eiExemptCheckBox.isSelected());
                preparedStatement.setBoolean(15, hasOvertimeCheckBox.isSelected());
                preparedStatement.setDouble(16, hasOvertimeCheckBox.isSelected() ? Double.parseDouble(overTimeHours.getText()) : 0.0);
                preparedStatement.setBoolean(17, useBaseAmountCheckBox.isSelected());
                preparedStatement.setBoolean(18, useYearlyCheckBox.isSelected());
                preparedStatement.setBoolean(19, employeeTD1CheckBox.isSelected());
                preparedStatement.setDouble(20, useBaseAmountCheckBox.isSelected() ? Double.parseDouble(baseAmountField.getText()) : 0.0);
                preparedStatement.setDouble(21, doubleMealAllowance);
                preparedStatement.executeUpdate();
            } catch (NumberFormatException e) {
                //System.out.println("Error parsing numeric values: " + e.getMessage());
            } catch (Exception e) {
                //.out.println("Error during SQL data insertion: " + e.getMessage());
            }
    
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "General Error", JOptionPane.ERROR_MESSAGE);
        }
    }    

    // This allows the program to read the textbox and then figure out when to end the generation of paystubs, it also makes it so automatically it is assumed that it is the last day of the year.
    private LocalDate parseEndPayPeriod() {
        String endDateText = endPayPeriodField.getText().trim();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        int fallbackYear;
        try {
            LocalDate beginningDate = LocalDate.parse(beginPayPeriodField.getText().trim(), dateFormatter);
            fallbackYear = beginningDate.getYear();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid beginning date format. Ensure it is in yyyy/MM/dd format.", "Date Parsing Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (endDateText.isEmpty()) {
            return LocalDate.of(fallbackYear, 12, 31);
        }

        try {
            return LocalDate.parse(endDateText, dateFormatter);
        } catch (DateTimeParseException e) {
            return LocalDate.of(fallbackYear, 12, 31);
        }
    }

    // This is the method which opens and then allows for the autofilling of the employee information for the program. This is very important to expedite the process of generating paystubs for the user.
    private void openEmployeeSQLTable() {
        String companyName = getCompanyNameTable();
        String companyNameRegular = getCompanyName();

        int companyID = getCompanyIdByName(companyNameRegular);
        String tableName = getEmployeeNameTable() + "_" + companyID;

        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();

        String selectDataSQL = "SELECT FullName, Address, PostalCode, Province, Frequency, AmountOfHours, EmployeeSIN, EmployeeCity, EmployeeNumber, "
                + "DepositNo, RateOfPayment, EmployeeAmountBonus, CPPExempt, EIExempt, HasOvertime, OvertimeHours, UseBaseAmount, BaseAmount, meal_Allowance "
                + "FROM " + tableName + " LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectDataSQL)) {

            if (resultSet.next()) {
                ((JTextField) employeeFullNameComboBox.getEditor().getEditorComponent()).setText(resultSet.getString("FullName"));
                ((JTextField) employeeFullNameComboBox.getEditor().getEditorComponent()).setForeground(Color.BLACK);

                employeeAddressField.setText(resultSet.getString("Address"));
                employeeAddressField.setForeground(Color.BLACK);
                employeePostalCodeField.setText(resultSet.getString("PostalCode"));
                employeePostalCodeField.setForeground(Color.BLACK);
                employeeProvinceField.setSelectedItem(resultSet.getString("Province"));
                paymentFrequencyField.setSelectedItem(resultSet.getString("Frequency"));

                String amountOfHours = resultSet.getString("AmountOfHours");
                String rateOfPayment = resultSet.getString("RateOfPayment");
                boolean useBaseAmount = resultSet.getBoolean("UseBaseAmount");

                useBaseAmountCheckBox.setSelected(useBaseAmount);
                if (useBaseAmount) {
                    baseAmountField.setText(String.valueOf(resultSet.getDouble("BaseAmount")));
                    baseAmountField.setForeground(Color.BLACK);
                    baseAmountField.setEnabled(true);
                    rateOfPaymentField.setText("Base Amount");
                    rateOfPaymentField.setEnabled(false);
                    rateOfPaymentField.setForeground(Color.BLACK);

                    amountOfHoursField.setText("Base Amount");
                    amountOfHoursField.setEnabled(false);
                    amountOfHoursField.setForeground(Color.BLACK);
                } else {
                    useBaseAmountCheckBox.setSelected(false);
                    amountOfHoursField.setText(amountOfHours);
                    amountOfHoursField.setForeground(Color.BLACK);
                    amountOfHoursField.setEnabled(true);
                    rateOfPaymentField.setText(rateOfPayment);
                    rateOfPaymentField.setForeground(Color.BLACK);
                    rateOfPaymentField.setEnabled(true);
                    baseAmountField.setText("");
                    baseAmountField.setEnabled(false);
                }

                employeeSINField.setText(resultSet.getString("EmployeeSIN"));
                employeeSINField.setForeground(Color.BLACK);

                employeeCityField.setText(resultSet.getString("EmployeeCity"));
                employeeCityField.setForeground(Color.BLACK);

                employeeNumberField.setText(resultSet.getString("EmployeeNumber"));
                employeeNumberField.setForeground(Color.BLACK);

                depositNumberField.setText(resultSet.getString("DepositNo"));
                depositNumberField.setForeground(Color.BLACK);

                employeeBonusField.setText(String.valueOf(resultSet.getDouble("EmployeeAmountBonus")));
                employeeBonusField.setForeground(Color.BLACK);

                cppExemptCheckBox.setSelected(resultSet.getBoolean("CPPExempt"));
                eiExemptCheckBox.setSelected(resultSet.getBoolean("EIExempt"));

                boolean hasOvertime = resultSet.getBoolean("HasOvertime");
                hasOvertimeCheckBox.setSelected(hasOvertime);
                if (hasOvertime) {
                    overTimeHours.setText(String.valueOf(resultSet.getDouble("OvertimeHours")));
                    overTimeHours.setForeground(Color.BLACK);
                    overTimeHours.setEnabled(true);
                } else {
                    overTimeHours.setText("");
                    overTimeHours.setEnabled(false);
                }

                JOptionPane.showMessageDialog(this, "Employee information loaded successfully from the table" + tableName);
            } else {
                JOptionPane.showMessageDialog(this,"No data found in table" + tableName + ".", "No Data", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing employee table " + tableName + "." + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
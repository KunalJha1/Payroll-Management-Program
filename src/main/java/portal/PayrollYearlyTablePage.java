package portal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayrollYearlyTablePage extends JPanel {
    
    public JComboBox<String> yearComboBox;
    private JComboBox<String> companyComboBox;
    private JTextField companyField;
    private JTextField employeeField;

    private JComboBox<String> employeeComboBox;
    private JPanel tablePanel;

    private int row = 1;
    private String[] headers;
    private ArrayList<JCheckBox> paidCheckBoxes = new ArrayList<>();
    public ArrayList<String> lesscombinedRow = new ArrayList<>();

    public DatabaseInformation dbInformation = new DatabaseInformation();
    public GeneralFunctions generalMethods = new GeneralFunctions();
    public GeneralStylingMethods stylingMethods = new GeneralStylingMethods();

    public PayrollYearlyTablePage(JPanel parentPanel, CardLayout cardLayout) {
        // The method of setLayout works using the extension of the JPanel throuhgout the entire class.
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    
        JPanel topContainerPanel = new JPanel(new BorderLayout());
        topContainerPanel.setBackground(Color.WHITE);
    
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(Color.WHITE);
    
        JLabel titleLabel = new JLabel("Payroll Projections - Yearlong", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 122, 204));
        add(titleLabel, BorderLayout.NORTH);
    
        JLabel yearSelect = new JLabel("Payroll Year");
        yearSelect.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(yearSelect);

        // Used in order to figure out which year of exemptions and tax brackets to use for the payroll, this can be expanded to future and previous year based on need.
        yearComboBox = new JComboBox<>(new String[] {"2025", "2024"});
        yearComboBox.setBackground(Color.WHITE);
        topPanel.add(yearComboBox);

        JLabel companyLabel = new JLabel("Company Name:");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(companyLabel);
    
        companyComboBox = new JComboBox<>();
        companyComboBox.setEditable(true);
        populateComboBox(companyComboBox, "ALL_COMPANIES", "company_name");
        topPanel.add(companyComboBox);

        JLabel employeeLabel = new JLabel("Employee Name:");
        employeeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(employeeLabel);
    
        employeeField = stylingMethods.createStyledTextField(20);
        topPanel.add(employeeField);

        JPanel topPanelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        topPanelButtons.setBackground(Color.WHITE);
    
        JButton loadEmployeeNamesButton = stylingMethods.createStyledButton("Load Employee Dropdown");
        loadEmployeeNamesButton.addActionListener(e -> switchToEmployeeComboBox());
        topPanelButtons.add(loadEmployeeNamesButton);
    
        JButton autoFillButton = stylingMethods.createStyledButton("Fill Table");
        autoFillButton.addActionListener(e -> autoFillTable());
        topPanelButtons.add(autoFillButton);

        JButton resetUI = stylingMethods.createStyledButton("Reset Table");
        resetUI.addActionListener(e -> resetUI());
        topPanelButtons.add(resetUI);

        JButton resetButton = stylingMethods.createStyledButton("Reset Page");
        resetButton.addActionListener(e -> resetPage(parentPanel, cardLayout));
        topPanelButtons.add(resetButton);

        topContainerPanel.add(topPanel, BorderLayout.NORTH);
        topContainerPanel.add(topPanelButtons, BorderLayout.CENTER);

        add(topContainerPanel, BorderLayout.NORTH);

        tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setPreferredSize(new Dimension(1920, 800));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        
        // Making and adding action listeners to the JButton's which execute functions. 
        JButton writeToMySQLButton = stylingMethods.createStyledButton("Write to MYSQL Table");
        writeToMySQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportTableDataToSQL();
                JOptionPane.showMessageDialog(null, "Updated data in SQL table", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton recalculateButton = stylingMethods.createStyledButton("Recalculate");
        recalculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportTableDataToSQL();
                reCalculateValues();
                applyAdjustments(getEmployeeNameTable(), getCompanyNameTable(), convertYearToDbNameID(yearComboBox));
                resetUI();
                autoFillTable();
                JOptionPane.showMessageDialog(null, "Recalculation complete!");
            }
        });

        JButton paymentSchedule = stylingMethods.createStyledButton("Payment Schedule");
        paymentSchedule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportVisibleTableToCSV();
            }
        });
        
        JButton yearCSV = stylingMethods.createStyledButton("Generate Year CSV");
        yearCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateYearlyCSV();
            }
        });

        JButton generatePDFPaystub = stylingMethods.createStyledButton("Generate PDF Payslip");
        generatePDFPaystub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PDFGeneration pdfGeneration = new PDFGeneration(getEmployeeName(), getEmployeeNameTable(), getCompanyName(), getCompanyNameTable(), getYear(yearComboBox));

                String companyNameFolder = getCompanyName();
                File pdfFolder = new File(companyNameFolder);
            
                if (!pdfFolder.exists()) {
                    pdfFolder.mkdir();
                }
        
                String pdfFilePath = pdfFolder.getAbsolutePath() + File.separator + getEmployeeName() + " Payslip.pdf";

                pdfGeneration.generatePayslip(pdfFilePath);

                JOptionPane.showMessageDialog(null, "PDF Created");

            }
        });

        buttonPanel.add(writeToMySQLButton);
        buttonPanel.add(recalculateButton);
        buttonPanel.add(paymentSchedule);
        buttonPanel.add(yearCSV);
        buttonPanel.add(generatePDFPaystub);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private void resetUI() {
        tablePanel.removeAll();
        
        row = 1;
        
        paidCheckBoxes.clear();
        
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    public void resetPage(JPanel parentPanel, CardLayout cardLayout) {
        parentPanel.remove(this); // Remove current instance
        PayrollYearlyTablePage newPage = new PayrollYearlyTablePage(parentPanel, cardLayout); // Create a new instance
        parentPanel.add(newPage, "Payroll Yearly Table"); // Add the new instance
        cardLayout.show(parentPanel, "Payroll Yearly Table"); // Show the new page
        parentPanel.revalidate();
        parentPanel.repaint();
    }
    


    // This returns the name of the company from the top section, this is important to get the accurate information which is used in the payroll generation for the client. 
    private String getCompanyNameTable() {
        String employerName;

        if (companyComboBox != null && companyComboBox.isEditable()) {
            employerName = ((JTextField) companyComboBox.getEditor().getEditorComponent()).getText().trim();
        } else {
            employerName = companyField.getText().trim();
        }

        return (employerName.replaceAll("\\W+", "_")).toLowerCase();
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

    // This populates the combobox for all of the employees inside of the class, this aids in use for the user when accessing a certain employees yearly paystub.
    private void populateComboBox(JComboBox<String> comboBox, String tableName, String columnName) {
        String companyName = tableName;
        String companyNameRegular = getCompanyName();
        if(tableName.equalsIgnoreCase("ALLEMP")){
            companyName = tableName + "_" + getCompanyIdByName(companyNameRegular) + "";
        }

        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT " + columnName + " FROM " + companyName)) {
            comboBox.removeAllItems();
            while (resultSet.next()) {
                comboBox.addItem(resultSet.getString(columnName));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading " + columnName + " from " + tableName + ": " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This swithes the writing in option the an employee dropdown, this will lessen the amount of time it takes for the making of a paystub. 
    private void switchToEmployeeComboBox() {
        if (employeeComboBox == null) {
            employeeComboBox = new JComboBox<>();
            employeeComboBox.setEditable(true);
            populateComboBox(employeeComboBox, "ALLEMP", "employee_name");
        }
        employeeComboBox.setSelectedItem(employeeField.getText());
        replaceComponent(employeeField, employeeComboBox);
    }

    // This is used to change the text write in option to a employee dropdown option. 
    private void replaceComponent(JComponent oldComponent, JComponent newComponent) {
        Container parent = oldComponent.getParent();
        parent.remove(oldComponent);
        parent.add(newComponent);
        parent.revalidate();
        parent.repaint();
    }

    // This is used in order figure out the sums of each of the columns which is used for the yearly summary as well. This is important for the client as they require these summed options for the entire company to create the T4 filings which will be sent to the CRA.
    public void writeSumsToEmployeeSummaryTable(double[] columnSums) {
        String companyName = getCompanyNameTable();
        String employeeName = getEmployeeNameTable();
        String tableName = "emplSum_" + companyName; 
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "employee_name VARCHAR(255) PRIMARY KEY, "
                + "hours_worked DECIMAL(10, 2), "
                + "basic_amount DECIMAL(10, 2), "
                + "vacation_pay DECIMAL(10, 2), "
                + "total_earnings DECIMAL(10, 2), "
                + "income_tax DECIMAL(10, 2), "
                + "cpp DECIMAL(10, 2), "
                + "ei DECIMAL(10, 2), "
                + "total_deductions DECIMAL(10, 2), "
                + "net_pay DECIMAL(10, 2), "
                + "cra_remittance DECIMAL(10, 2))";
    
        String insertOrUpdateSQL = "INSERT INTO " + tableName + " (employee_name, hours_worked, basic_amount, vacation_pay, total_earnings, income_tax, cpp, ei, total_deductions, net_pay, cra_remittance) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "hours_worked = VALUES(hours_worked), "
                + "basic_amount = VALUES(basic_amount), "
                + "vacation_pay = VALUES(vacation_pay), "
                + "total_earnings = VALUES(total_earnings), "
                + "income_tax = VALUES(income_tax), "
                + "cpp = VALUES(cpp), "
                + "ei = VALUES(ei), "
                + "total_deductions = VALUES(total_deductions), "
                + "net_pay = VALUES(net_pay), "
                + "cra_remittance = VALUES(cra_remittance)";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement createStatement = connection.createStatement();
             PreparedStatement insertOrUpdateStatement = connection.prepareStatement(insertOrUpdateSQL)) {
    
            createStatement.execute(createTableSQL);

            insertOrUpdateStatement.setString(1, employeeName);
            insertOrUpdateStatement.setDouble(2, columnSums[2]);
            insertOrUpdateStatement.setDouble(3, columnSums[4]);
            insertOrUpdateStatement.setDouble(4, columnSums[5]);
            insertOrUpdateStatement.setDouble(5, columnSums[6]);
            insertOrUpdateStatement.setDouble(6, columnSums[7]);
            insertOrUpdateStatement.setDouble(7, columnSums[8]);
            insertOrUpdateStatement.setDouble(8, columnSums[9]);
            insertOrUpdateStatement.setDouble(9, columnSums[10]);
            insertOrUpdateStatement.setDouble(10, columnSums[11]);
            insertOrUpdateStatement.setDouble(11, columnSums[12]);

            insertOrUpdateStatement.executeUpdate();
    
            JOptionPane.showMessageDialog(this, "Employee summary data has been successfully written to the database.", "Success", JOptionPane.INFORMATION_MESSAGE);
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error writing summary data to the database: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This will export the table which is generated on the screen to a CSV. This is essential because of the fact the employer will find this easier when making payments versus going through every single paystub to make employee payments. 
    public void exportVisibleTableToCSV() {

        String companyNameRegular = getCompanyName();
        String companyNameFolder = getCompanyName();
        File csvFolder = new File(companyNameFolder);
    
        if (!csvFolder.exists()) {
            csvFolder.mkdir();
        }

        String csvFilePath = csvFolder.getAbsolutePath() + File.separator + getEmployeeName() + " Projected Payment Schedule.csv";
        String employeeName = getEmployeeNameTable();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        // SQL query to fetch all rows from the database
        String query = "SELECT * FROM " + tableName + " ORDER BY sl_no";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = statement.executeQuery();
             FileWriter writer = new FileWriter(csvFilePath)) {
    
            // Check if the table has data
            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No data found in table '" + tableName, "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
    
            resultSet.next();
            
    
            headers = new String[]{
                "sl_no", "pay_date", "hours_worked", "rate_per_hour", "overtime_hours", "overtime_rate", "amount_bonus", "basic_amount", "vacation_pay", "meal_Allowance",
                "total_earnings", "income_tax", "cpp", "cpp2", "ei",
                "total_deductions", "net_pay", "cra_remittance", "ctc"
            };

            boolean firstColumn = true;
            // Writes the name and then the titles of each of the Collumns this aids in the organization of the CSV to make sure it makes sense.
            writer.append(getEmployeeName() + "\n\n");

            for (String columnName : headers) {
                if (!firstColumn) writer.append(",");
                writer.append(columnName.replace("_", " ").toUpperCase());
                firstColumn = false;
            }

            writer.append("\n");
            resultSet.beforeFirst();
            // Makes a "sum" collumn for each of the collumns based on how many header collumns there are, some of these collumns do not need sum's which is why they are excluded using seperate functions. 
            double[] columnSums = new double[headers.length];
    
            while (resultSet.next()) {
                firstColumn = true;
                for (int i = 0; i < headers.length; i++) {
                    String columnName = headers[i];
    
                    if (!firstColumn) writer.append(",");
                    String value = resultSet.getString(columnName);
                    // If something is a summableColumn then it sums the collumn.
                    if (value != null && isSummableColumn(columnName)) {
                        try {
                            double numericValue = Double.parseDouble(value);
                            columnSums[i] += numericValue;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    // However, if the something is a summableColumn and it can have a dollar symbol in front that is what occurs. 
                    if (value != null) {
                        if (isNumericColumn(columnName) && i != 1) {
                            writer.append("$").append(value);
                        } else {
                            writer.append(value);
                        }
                    }
                    firstColumn = false;
                }
                writer.append("\n");
            }
            
    
            firstColumn = true;
            for (int i = 0; i < headers.length; i++) {
                if (!firstColumn) writer.append(",");
                if (isSummableColumn(headers[i]) && isNumericColumn(headers[i])) {
                    writer.append("$").append(String.format("%.2f", columnSums[i]));
                } else if (isSummableColumn(headers[i])) {
                    writer.append(String.format("%.2f", columnSums[i]));
                } else {
                    writer.append("");
                }
                //System.out.println(columnSums[i]);
                firstColumn = false;
            }

            writer.append("\n");
            // This is the method which is used to write the sum of all collumns to the database which will be used for the YearEndSummary. 
            //writeSumsToEmployeeSummaryTable(columnSums);

            writer.flush();
            JOptionPane.showMessageDialog(this, "CSV file saved at " + csvFilePath, "Success", JOptionPane.INFORMATION_MESSAGE);
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving data from database: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving CSV file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // This is a method which classifies all of the headers based on weather they are dollar numbers and need a dollar symbol or not.
    private boolean isNumericColumn(String columnName) {
        return 
               columnName.equalsIgnoreCase("basic_amount") ||
               columnName.equalsIgnoreCase("vacation_pay") ||
               columnName.equalsIgnoreCase("meal_Allowance") ||
               columnName.equalsIgnoreCase("total_earnings") ||
               columnName.equalsIgnoreCase("income_tax") ||
               columnName.equalsIgnoreCase("cpp") ||
               columnName.equalsIgnoreCase("cpp2") ||
               columnName.equalsIgnoreCase("ei") ||
               columnName.equalsIgnoreCase("total_deductions") ||
               columnName.equalsIgnoreCase("net_pay") ||
               columnName.equalsIgnoreCase("cra_remittance") ||
               columnName.equalsIgnoreCase("amount_bonus") ||
               columnName.equalsIgnoreCase("ctc") ||
               columnName.equalsIgnoreCase("net_pay");
    }
    
    // This is a method which classifies all of the headers and if they are numeric collumns which need sums but not dollar symbols.
    private boolean isSummableColumn(String columnName) {
        return columnName.equalsIgnoreCase("hours_worked") ||
               columnName.equalsIgnoreCase("basic_amount") ||
               columnName.equalsIgnoreCase("vacation_pay") ||
               columnName.equalsIgnoreCase("meal_Allowance") ||
               columnName.equalsIgnoreCase("total_earnings") ||
               columnName.equalsIgnoreCase("income_tax") ||
               columnName.equalsIgnoreCase("cpp") ||
               columnName.equalsIgnoreCase("cpp2") ||
               columnName.equalsIgnoreCase("ei") ||
               columnName.equalsIgnoreCase("total_deductions") ||
               columnName.equalsIgnoreCase("net_pay") ||
               columnName.equalsIgnoreCase("cra_remittance") ||
               columnName.equalsIgnoreCase("amount_bonus") ||
               columnName.equalsIgnoreCase("overtime_hours") ||
               columnName.equalsIgnoreCase("ctc") ||
               columnName.equalsIgnoreCase("net_pay");
    }
    
    public void printGrid() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();

        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();

        List<List<String>> dataGrid = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " ORDER BY sl_no")) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                headers.add(metaData.getColumnName(i));
            }
            dataGrid.add(headers);

            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                dataGrid.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /// This is what exports the visible datatable to something in the SQL system. This is essential in the useage of the program and this 
    /// method also gets information which is "hidden" anmd not avaialbe on the screen and still carries forward this information by accessing the SQL table 
    /// which contains the information.
    private void exportTableDataToSQL() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();

        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) +  "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement()) {
    
            String query = "SELECT sl_no, has_overtime, use_base_amount, base_amount, overtime_rate FROM " + tableName + " ORDER BY sl_no";
            ResultSet resultSet = statement.executeQuery(query);
            List<List<String>> hiddenData = new ArrayList<>();
                

            // Gets all of the information from the SQL table which would not be accessible from the information on the GUI or other parts of the program.
            while (resultSet.next()) {
                List<String> hiddenRow = new ArrayList<>();
                hiddenRow.add(resultSet.getString("sl_no"));
                hiddenRow.add(resultSet.getString("has_overtime"));
                hiddenRow.add(resultSet.getString("use_base_amount"));
                hiddenRow.add(resultSet.getString("base_amount"));
                hiddenRow.add(resultSet.getString("overtime_rate"));
                hiddenData.add(hiddenRow);
                //System.out.println(hiddenRow);
            }
            
            // Drops the table after getting all of the required information.
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
            statement.executeUpdate(dropTableSQL);
    
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "sl_no INT, pay_date DATE, hours_worked DECIMAL(10, 2), "
                    + "rate_per_hour DECIMAL(10, 2), basic_amount DECIMAL(10, 2), vacation_pay DECIMAL(10, 2), "
                    + "total_earnings DECIMAL(10, 2), federal_tax DECIMAL(10, 2), provincial_tax DECIMAL(10, 2), "
                    + "income_tax DECIMAL(10, 2), cpp DECIMAL(10, 2), cpp2 DECIMAL(10, 2), ei DECIMAL(10, 2), total_deductions DECIMAL(10, 2), "
                    + "net_pay DECIMAL(10, 2), cra_remittance DECIMAL(10, 2), "
                    + "has_overtime BOOLEAN, overtime_hours DECIMAL(10, 2), overtime_rate DECIMAL(10, 2), use_base_amount BOOLEAN, base_amount DECIMAL(10, 2), "
                    + "paid BOOLEAN DEFAULT FALSE, meal_Allowance DECIMAL(10,2), amount_bonus DECIMAL(10,2), ctc DECIMAL(10,2))";

            statement.executeUpdate(createTableSQL);
    
            List<List<String>> combinedDataArray = new ArrayList<>();
            List<List<String>> lessCombinedDataArray = new ArrayList<>();
    
            for (int rowIndex = 1; rowIndex < row; rowIndex++) {
                List<String> combinedRow = new ArrayList<>();
                for (Component comp : getComponentsInRow(rowIndex)) {
                    // String columnName = comp.getName();

                    // If a part of the textfields inside of the GUI are actually textfields and not checkmarkboxes then get the text value of that.
                    if (comp instanceof JTextField) {
                        String textValue = ((JTextField) comp).getText();
                        combinedRow.add(textValue);
                    } 
                    //System.out.println(combinedRow);
                }

                /// Add's all of the information from the GUI textfields into the list which is the ngoing to be written into the program, this includes the
                /// SL, Date, Hours, Rate, Basic, Vacation, Total Earnings, Income Taxes, CPP, EI, Total Deducions, Net Pay, CRA Remittance, and other information like the amount of overtime.
                
                combinedDataArray.add(combinedRow);
                //System.out.println(combinedDataArray);
    
                List<String> lessCombinedRow = new ArrayList<>();

                //System.out.println("Hidden Data Size (ROW 527 Payroll Yearly Table Page) Check for Mismatch Fix");
                if (rowIndex - 1 < hiddenData.size()) {
                    List<String> hiddenRow = hiddenData.get(rowIndex - 1);
                    String hasOvertime = hiddenRow.get(1);
                    String useBaseAmount = hiddenRow.get(2);
                    String baseAmountMySQL = hiddenRow.get(3);
                    String overtimeRateMySQL = hiddenRow.get(4);
                    // Logical operations for parts of the program and getting values or casting them as values depending of if they are edited.

                    if (hasOvertime.equals("1")) {
                        //System.out.println("YEAH OVERTIME");
                        lessCombinedRow.add(hasOvertime);
                        lessCombinedRow.add(getUIValue(rowIndex, "overtime_hours"));
                        lessCombinedRow.add(getUIValue(rowIndex, "overtime_rate"));
                    } else {
                        lessCombinedRow.add(hasOvertime);
                        lessCombinedRow.add("0");
                        lessCombinedRow.add(overtimeRateMySQL);
                    }
    
                    if (useBaseAmount.equals("1")) {
                        // System.out.println("YEAH BASE AMOUNT");
                        lessCombinedRow.add(useBaseAmount);
                        lessCombinedRow.add(getUIValue(rowIndex, "base_amount"));
                    } else {
                        lessCombinedRow.add("0");
                        lessCombinedRow.add(baseAmountMySQL);
                    }
    

                    boolean isPaid = false;
                    int shiftAmount = 0;
                    // This is a double-checking section to make sure if there is any mismatch between the isPaid section of the program that it realigns later on with the correct results.
                    while (true) {
                        boolean foundCheckbox = false;
                        for (Component comp : getComponentsInRow(rowIndex + shiftAmount)) {
                            if (comp instanceof JCheckBox) {
                                isPaid = ((JCheckBox) comp).isSelected();
                                foundCheckbox = true;
                                break;
                            }
                        }
                        if (foundCheckbox) {
                            rowIndex += shiftAmount;
                            break;
                        } else {
                            shiftAmount++;
                            if (rowIndex + shiftAmount >= 999) {
                                throw new IllegalStateException("No JCheckBox found in the rows.");
                            }
                        }
                    }
                    

                    lessCombinedRow.add(isPaid ? "1" : "0");
                }
                lessCombinedDataArray.add(lessCombinedRow);
                // System.out.println(lessCombinedRow);
            }
    
            String insertSQL = "INSERT INTO " + tableName + " (sl_no, pay_date, hours_worked, rate_per_hour, "
            + "basic_amount, vacation_pay, total_earnings, federal_tax, provincial_tax, income_tax, "
            + "cpp, cpp2, ei, total_deductions, net_pay, cra_remittance, "
            + "has_overtime, overtime_hours, overtime_rate, use_base_amount, base_amount, paid, meal_Allowance, amount_bonus, ctc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
            // debugPrintRowComponents(5);

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                for (int i = 0; i < combinedDataArray.size(); i++) {
                    List<String> temporaryList = new ArrayList<>(combinedDataArray.get(i));
                    temporaryList.addAll(lessCombinedDataArray.get(i));
                    //System.out.println(temporaryList.size());
                    //System.out.println(temporaryList);

                    preparedStatement.setInt(1, Integer.parseInt(temporaryList.get(0)));         // 0 - SL
                    preparedStatement.setDate(2, java.sql.Date.valueOf(temporaryList.get(1)));   // 1 - PAY DATE
                    preparedStatement.setDouble(3, Double.parseDouble(temporaryList.get(2)));    // 2 - HOURS WORKED
                    preparedStatement.setDouble(4, Double.parseDouble(temporaryList.get(3)));    // 3 - RATE
                    preparedStatement.setDouble(18, Double.parseDouble(temporaryList.get(4)));   // 4 - OVERTIME HOURS
                    preparedStatement.setDouble(19, Double.parseDouble(temporaryList.get(5)));   // 5 - OVERTIME RATE
                    preparedStatement.setDouble(5, Double.parseDouble(temporaryList.get(7)));    // 6 - BASIC AMOUNT
                    preparedStatement.setDouble(6, Double.parseDouble(temporaryList.get(8)));    // 7 - VACATION PAY
                    preparedStatement.setDouble(23, Double.parseDouble(temporaryList.get(9)));   // 8 - MEAL ALLOWANCE
                    preparedStatement.setDouble(7, Double.parseDouble(temporaryList.get(10)));    // 9 - TOTAL EARNINGS
                    preparedStatement.setDouble(8, Double.parseDouble(temporaryList.get(11)));   // 10 - FEDERAL TAX
                    preparedStatement.setDouble(9, Double.parseDouble(temporaryList.get(12)));   // 11 - PROVINCIAL TAX
                    preparedStatement.setDouble(10, Double.parseDouble(temporaryList.get(13)));  // 12 - INCOME TAX
                    preparedStatement.setDouble(11, Double.parseDouble(temporaryList.get(14)));  // 13 - CPP
                    preparedStatement.setDouble(12, Double.parseDouble(temporaryList.get(15)));  // 14 - CPP2
                    preparedStatement.setDouble(13, Double.parseDouble(temporaryList.get(16)));  // 15 - EI
                    preparedStatement.setDouble(14, Double.parseDouble(temporaryList.get(17)));  // 16 - TOTAL DEDUCTIONS
                    preparedStatement.setDouble(15, Double.parseDouble(temporaryList.get(18)));  // 17 - NET PAY
                    preparedStatement.setDouble(16, Double.parseDouble(temporaryList.get(19)));  // 18 - CRA REMITTANCE
                    preparedStatement.setBoolean(17, "1".equals(temporaryList.get(21))); // 19 - HAS OVERTIME
                    preparedStatement.setBoolean(20, "1".equals(temporaryList.get(24))); // 22 - USE BASE AMOUNT
                    preparedStatement.setDouble(21, Double.parseDouble(temporaryList.get(25)));  // 23 - BASE AMOUNT
                    preparedStatement.setBoolean(22, "1".equals(temporaryList.get(26)));         // 24 - PAID
                    preparedStatement.setDouble(24, Double.parseDouble(temporaryList.get(6)));  // 
                    preparedStatement.setDouble(25, Double.parseDouble(temporaryList.get(20))); // ctc
                    preparedStatement.executeUpdate();

            }
    
        }
            lessCombinedDataArray.clear();
            combinedDataArray.clear();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating data in SQL table: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // private void debugPrintRowComponents(int rowIndex) {
    //     System.out.println("🔍 Debugging components in row: " + rowIndex);
    //     List<Component> components = getComponentsInRow(rowIndex);
    
    //     for (int i = 0; i < components.size(); i++) {
    //         Component comp = components.get(i);
    //         System.out.print("[" + i + "] ");
    
    //         if (comp instanceof JTextField) {
    //             System.out.println("JTextField: \"" + ((JTextField) comp).getText() + "\"");
    //         } else if (comp instanceof JCheckBox) {
    //             System.out.println("JCheckBox: " + (((JCheckBox) comp).isSelected() ? "✓ Checked" : "✗ Unchecked"));
    //         } else if (comp instanceof JLabel) {
    //             System.out.println("JLabel: \"" + ((JLabel) comp).getText() + "\"");
    //         } else if (comp instanceof JButton) {
    //             System.out.println("JButton: \"" + ((JButton) comp).getText() + "\"");
    //         } else {
    //             System.out.println(comp.getClass().getSimpleName());
    //         }
    //     }
    //     System.out.println("✅ End of row " + rowIndex);
    // }
    


    private String getUIValue(int rowIndex, String columnName) {
        for (Component comp : getComponentsInRow(rowIndex)) {
            if (comp instanceof JTextField && columnName.equals(comp.getName())) {
                return ((JTextField) comp).getText();
            }
        }
        return null;
    }
    
    
    // This method is what allows the program to get the values which are stored inside of the table and convert them to real values which can be written to the SQL tables in the "backend."
    private List<Component> getComponentsInRow(int rowIndex) {
        List<Component> rowComponents = new ArrayList<>();
        
        // Iterate through all components in the tablePanel
        for (Component comp : tablePanel.getComponents()) {
            // Get the GridBagConstraints for the component
            GridBagConstraints gbc = ((GridBagLayout) tablePanel.getLayout()).getConstraints(comp);
            
            // Check if the component belongs to the specified row
            if (gbc.gridy == rowIndex) {
                rowComponents.add(comp);
            }
        }
    
        return rowComponents;
    }
    
    // This method is what allows the program to get the name of the company with no changes, it is the exact same and upper lower case as the original.
    private String getCompanyName() {
        if (companyComboBox.isVisible()) {
            if (companyComboBox.isEditable()) {
                return ((JTextField) companyComboBox.getEditor().getEditorComponent()).getText().trim();
            } else {
                return companyComboBox.getSelectedItem().toString().trim();
            }
        } else {
            try {
                return companyField.getText().trim();
            } catch (Exception e) {
                return companyField.getText().trim();
            }
        }
    }
    
    // This method is what allows the program to get the name of the employee with no changes, it is the exact name of the employee with the same upper and lower case as the original. 
    private String getEmployeeName() {
        if (employeeComboBox != null && employeeComboBox.isVisible()) {
            return employeeComboBox.getSelectedItem().toString().trim();
        } else {
            return employeeField.getText().trim();
        }
    }

    // This method is what replaces the spaces and other charactersitcs of the employee name to the format required for the SQL server. This is important as without this method none of the tables in the SQL server could have been saved. 
    private String getEmployeeNameTable() {
        String rawName;

        if (employeeComboBox != null && employeeComboBox.isVisible()) {
            rawName = ((JTextField) employeeComboBox.getEditor().getEditorComponent()).getText().trim();
        } else {
            rawName = employeeField.getText().trim();
        }

        return (rawName.replaceAll("\\W+", "_")).toLowerCase();
    }

    // This is what gets all of the employee information which is required to make a fully done paystub for the client. Moreover, this also has things like the exemptions and if they are exempt from specific deductions. 
    private ArrayList<String> openEmployeeSQLTable() {
        ArrayList<String> employeeData = new ArrayList<>();
        String companyNameRegular = getCompanyName();
        String tableName = getEmployeeNameTable() + "_" + getCompanyIdByName(companyNameRegular);
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
                employeeData.add(resultSet.getString("FullName"));
                employeeData.add(resultSet.getString("Address"));
                employeeData.add(resultSet.getString("PostalCode"));
                employeeData.add(resultSet.getString("Province"));
                employeeData.add(resultSet.getString("Frequency"));
                employeeData.add(resultSet.getString("AmountOfHours"));
                employeeData.add(resultSet.getString("EmployeeSIN"));
                employeeData.add(resultSet.getString("EmployeeCity"));
                employeeData.add(resultSet.getString("EmployeeNumber"));
                employeeData.add(resultSet.getString("DepositNo"));
                employeeData.add(resultSet.getString("RateOfPayment"));
                employeeData.add(String.valueOf(resultSet.getDouble("EmployeeAmountBonus")));
                employeeData.add(String.valueOf(resultSet.getBoolean("CPPExempt")));
                employeeData.add(String.valueOf(resultSet.getBoolean("EIExempt")));
                employeeData.add(String.valueOf(resultSet.getBoolean("HasOvertime")));
                employeeData.add(String.valueOf(resultSet.getDouble("OvertimeHours")));
                employeeData.add(String.valueOf(resultSet.getBoolean("UseBaseAmount")));
                employeeData.add(String.valueOf(resultSet.getDouble("BaseAmount")));
                employeeData.add(String.valueOf(resultSet.getDouble("meal_Allowance")));
    
                JOptionPane.showMessageDialog(this, "Employee information loaded successfully from the table '" + tableName + "' in database '" + "'.");
            } else {
                JOptionPane.showMessageDialog(this, "No data found in table '" + tableName + "'.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing employee table '" + tableName + "': " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        return employeeData;
    }
    
    // Opens the accurate table which includes all of the employer information, this is information which is used when writing the employer information for the CSV. This is returned as an ArrayList to make it easier to construct the tables which is like row after row of arraylist. 
    private ArrayList<String> openMySQLTable() {
        String dbName = getCompanyNameTable();
        String companyNameRegular = getCompanyName();

        String tableName = "EmplI_" + getCompanyIdByName(companyNameRegular);
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        String selectDataSQL = "SELECT EmployerCompanyName, EmployerCity, EmployerProvince, EmployerAddress, EmployerPostalCode FROM " + tableName;
    
        ArrayList<String> employerInfoList = new ArrayList<>();
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectDataSQL)) {
    
            while (resultSet.next()) {
                employerInfoList.add(resultSet.getString("EmployerCompanyName"));
                employerInfoList.add(resultSet.getString("EmployerCity"));
                employerInfoList.add(resultSet.getString("EmployerProvince"));
                employerInfoList.add(resultSet.getString("EmployerAddress"));
                employerInfoList.add(resultSet.getString("EmployerPostalCode"));
                
            }

            if (employerInfoList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No data found in table '" + tableName + "' in the database '" + dbName + "'.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            }
    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing table '" + tableName + "' in database '" + dbName + "': " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        return employerInfoList;
    }
    
    // This generated the CSV which has the paystubs and other stuff with the updated information from the table which is on the GUI.
    private void generateYearlyCSV() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();

        String selectSQL = "SELECT * FROM " + tableName;

        ArrayList<ArrayList<String>> data = new ArrayList<>();

        String fileName = getCompanyName();
        File csvFolder = new File(fileName);
        if (!csvFolder.exists()) {
            csvFolder.mkdir();
        }
    
        String csvFilePath = csvFolder.getAbsolutePath() + File.separator + getEmployeeName() + " Yearly Paystub.csv";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            Statement statement = connection.createStatement();
            FileWriter writer = new FileWriter(csvFilePath)) {
            ResultSet resultSet = statement.executeQuery(selectSQL);

            
            while (resultSet.next()) {
                ArrayList<String> row = new ArrayList<>();
                row.add(String.valueOf(resultSet.getDouble("sl_no")));
                row.add(resultSet.getString("pay_date"));
                row.add(String.valueOf(resultSet.getDouble("hours_worked")));
                row.add(String.valueOf(resultSet.getDouble("rate_per_hour")));
                row.add(String.valueOf(resultSet.getDouble("basic_amount")));
                row.add(String.valueOf(resultSet.getDouble("vacation_pay")));
                row.add(String.valueOf(resultSet.getDouble("total_earnings")));
                row.add(String.valueOf(resultSet.getDouble("federal_tax")));
                row.add(String.valueOf(resultSet.getDouble("provincial_tax")));
                row.add(String.valueOf(resultSet.getDouble("income_tax")));
                row.add(String.valueOf(resultSet.getDouble("cpp")));
                row.add(String.valueOf(resultSet.getDouble("cpp2")));
                row.add(String.valueOf(resultSet.getDouble("ei")));
                row.add(String.valueOf(resultSet.getDouble("total_deductions")));
                row.add(String.valueOf(resultSet.getDouble("net_pay")));
                row.add(String.valueOf(resultSet.getDouble("cra_remittance")));
                row.add(String.valueOf(resultSet.getBoolean("has_overtime")));
                row.add(String.valueOf(resultSet.getDouble("overtime_hours")));
                row.add(String.valueOf(resultSet.getDouble("overtime_rate")));
                row.add(String.valueOf(resultSet.getBoolean("use_base_amount")));
                row.add(String.valueOf(resultSet.getDouble("base_amount")));
                row.add(String.valueOf(resultSet.getBoolean("paid")));
                row.add(String.valueOf(resultSet.getDouble("meal_Allowance")));
                row.add(String.valueOf(resultSet.getDouble("ctc")));

                data.add(row);
            }

            double maxSLNo = 1.0;
            if (!data.isEmpty()) {
                ArrayList<String> lastRow = data.get(data.size() - 1);
                String maxSLNoString = lastRow.get(0);
                maxSLNo = Double.parseDouble(maxSLNoString);
            }

                ArrayList<String> employerInformation = new ArrayList<String>();
                employerInformation = openMySQLTable();

                ArrayList<String> employeeData = openEmployeeSQLTable();
                String employeeNameCSV = employeeData.get(0);
                String employeeAddress = employeeData.get(1);
                String employeePostalCode = employeeData.get(2);
                String employeeProvince = generalMethods.getProvinceCode(employeeData.get(3));
                String frequency = employeeData.get(4);
                // String amountOfHours = employeeData.get(5);
                String employeeFullSin = employeeData.get(6);
                String employeeCity = employeeData.get(7);
                String employeeNumber = employeeData.get(8);
                String depositNumberString = employeeData.get(9);
                // String rateOfPayment = employeeData.get(10);
                String employeeAmountBonus = employeeData.get(11);
                // String cppExempt = employeeData.get(12);
                // String eiExempt = employeeData.get(13);
                String hasOvertime = employeeData.get(14);
                // String overtimeHoursBoolean = employeeData.get(15);
                // String useBaseAmount = employeeData.get(16);
                // String baseAmount = employeeData.get(17);
                int depositNumberDouble;

                try {
                    depositNumberDouble = Integer.parseInt(depositNumberString);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid deposit number format: " + depositNumberString);
                    depositNumberDouble = 1;
                }


                String companyNameCSV = employerInformation.get(0);
                String companyCity = employerInformation.get(1);
                String companyProvince = generalMethods.getProvinceCode(employerInformation.get(2));
                String companyAddress = employerInformation.get(3);
                String postalCode = employerInformation.get(4);
                String lastThreeSIN = employeeFullSin.length() >= 3 ? employeeFullSin.substring(employeeFullSin.length() - 3) : employeeFullSin;

                ArrayList<Double> ytdEarningsList = new ArrayList<>();
                ArrayList<Double> ytdPayrollTaxList = new ArrayList<>();
                ArrayList<Double> ytdCPPList = new ArrayList<>();
                ArrayList<Double> ytdEIList = new ArrayList<>();
                ArrayList<Double> ytdVacationList = new ArrayList<>();
                ArrayList<Double> ytdNetPayList = new ArrayList<>();
                ArrayList<Double> ytdBonusList = new ArrayList<>();
                ArrayList<Double> ytdMealAllowanceList = new ArrayList<>();



                for (int i = 1; i <= maxSLNo; i++) {
                    depositNumberString = depositNumberDouble + "";
                
                    String payPeriodEnd = data.get(i - 1).get(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate endDate = LocalDate.parse(payPeriodEnd, formatter);

                    LocalDate startDate = null;

                    switch (frequency) {
                        case "Weekly (52)":
                        case "Weekly (53)":
                            startDate = endDate.minusDays(6);
                            break;
                    
                        case "Bi-Weekly (26)":
                        case "Bi-Weekly (27)":
                            startDate = endDate.minusDays(13);
                            break;
                    
                        case "Semi-Monthly (24)":
                            if (endDate.getDayOfMonth() <= 15) {
                                // First half of the month: Start from the 1st of the current month
                                startDate = endDate.withDayOfMonth(1);
                            } else {
                                // Second half of the month: Start from the 16th of the current month
                                startDate = endDate.withDayOfMonth(16);
                            }
                            break;
                    
                        case "Monthly (12)":
                            startDate = endDate.withDayOfMonth(1);
                            break;
                    
                        case "Quarterly (4)":
                            startDate = endDate.minusMonths(3);
                            break;
                    
                        case "Semi-Annually (2)":
                            startDate = endDate.minusMonths(6);
                            break;
                    
                        case "Yearly (1)":
                            startDate = endDate.minusYears(1);
                            break;
                    
                        default:
                            throw new IllegalArgumentException("Unknown payment frequency");
                    }
                    
                    String startDateString = startDate.format(formatter);
                    System.out.println(data.get(i-1));

                    String rate = data.get(i - 1).get(3); 
                    String hoursWorked = data.get(i - 1).get(2);
                    String basicAmount = data.get(i - 1).get(4);
                    String vacationPay = data.get(i - 1).get(5); 
                    String totalEarnings = data.get(i - 1).get(6); 
                    // String federalTax = data.get(i - 1).get(7);
                    // String provincialTax = data.get(i - 1).get(8); 
                    String totalTax = data.get(i - 1).get(9); 
                    String cppDeduction = data.get(i - 1).get(10);
                    String eiDeduction = data.get(i - 1).get(12);
                    String cppDeductions2 = data.get(i - 1).get(11);
                    String netPay = data.get(i - 1).get(14); 

                    String mealAllowance = data.get(i - 1).get(22); // index 22 = mealAllowance column
                    double mealAllowanceDouble = 0.00;
                    try {
                        mealAllowanceDouble = Double.parseDouble(mealAllowance);
                    } catch (NumberFormatException e) {
                        mealAllowanceDouble = 0.00;
                    }
                    ytdMealAllowanceList.add(mealAllowanceDouble);
                    
                    String overtimeHours = data.get(i - 1).get(17); 
                    String overtimeRate = data.get(i - 1).get(18); 
                    
                    double overtimeHoursDouble = Double.parseDouble(overtimeHours);
                    double overtimeRateDouble = Double.parseDouble(overtimeRate);
                    double overtimeMoneyDouble = overtimeHoursDouble * overtimeRateDouble;
                    
                    String overtimeMoney = String.valueOf(overtimeMoneyDouble);

                    ytdEarningsList.add(Double.parseDouble(basicAmount));
                    ytdPayrollTaxList.add(Double.parseDouble(totalTax));
                    ytdCPPList.add(Double.parseDouble(cppDeduction));
                    ytdEIList.add(Double.parseDouble(eiDeduction));
                    ytdVacationList.add(Double.parseDouble(vacationPay));
                    ytdNetPayList.add(Double.parseDouble(netPay));
                
                    double ytdEarnings = 0;
                    for (Double earnings : ytdEarningsList) {
                        ytdEarnings += earnings;
                    }
                
                    double ytdPayrollTax = 0;
                    for (Double tax : ytdPayrollTaxList) {
                        ytdPayrollTax += tax;
                    }
                
                    double ytdCPP = 0;
                    for (Double cpp : ytdCPPList) {
                        ytdCPP += cpp;
                    }
                
                    double ytdEI = 0;
                    for (Double ei : ytdEIList) {
                        ytdEI += ei;
                    }
                    double ytdVacationPay = 0;
                    for (Double vacationPayIteration : ytdVacationList) {
                        ytdVacationPay += vacationPayIteration;
                    }
                
                    double ytdNetPay = 0;
                    for (Double net : ytdNetPayList) {
                        ytdNetPay += net;
                    }
                
                    double ytdBonus = 0;
                    for (Double bonus : ytdBonusList) {
                        ytdBonus += bonus;
                    }

                    double ytdMealAllowance = 0;
                    for (Double meal : ytdMealAllowanceList) {
                        ytdMealAllowance += meal;
                    }
                    


                    double rateCSV = Double.parseDouble(rate);
                    double hoursWorkedCSV = Double.parseDouble(hoursWorked);
                    double vacationPayCSV = Double.parseDouble(vacationPay);
                    double totalEarningsCSV = Double.parseDouble(totalEarnings);
                    double totalTaxCSV = Double.parseDouble(totalTax);
                    double cppDeductionCSV = Double.parseDouble(cppDeduction);
                    double eiDeductionCSV = Double.parseDouble(eiDeduction);
                    double basicAmountCSV = Double.parseDouble(basicAmount);
                    double totalDeductions = eiDeductionCSV + cppDeductionCSV + totalTaxCSV;

                    double netPayCSV = Double.parseDouble(netPay);
                    double employeeAmountBonusCSV = Double.parseDouble(employeeAmountBonus);

                    writer.append(employeeNameCSV).append(",,,,,,").append(companyNameCSV).append("\n");
                    System.out.println(employeeAddress);
                    System.out.println(employeePostalCode);
                    writer.append(employeeAddress).append(",,,,,,").append(companyAddress).append("\n");
                    writer.append(employeeCity).append(" ").append(employeeProvince).append(" ").append(employeePostalCode).append(",,,,,,").append(companyCity).append(" ").append(companyProvince).append(" ").append(postalCode).append("\n\n\n");
                
                    writer.append("EMPLOYEE NO.,").append(employeeNumber).append(",,,,SIN,XXX-XXX-").append(lastThreeSIN).append("\n");
                    writer.append("PAY PERIOD,").append(startDateString).append(",TO,").append(payPeriodEnd).append(",,DEPOSIT NO.,").append(depositNumberString).append("\n");
                    writer.append("DATE OF PAYMENT\n\n");
                    depositNumberDouble++;
                
                    writer.append(",,,EARNINGS,,,DEDUCTION\n");
                    writer.append("DESCRIPTION,RATE,HOURS,CURRENT,YTD,DESCRIPTION,CURRENT,YTD\n");
                
                    writer.append("REGULAR EARNING,").append(String.format("$%.2f", rateCSV)).append(",").append(String.format("%.2f", hoursWorkedCSV)).append(",").append(String.format("$%.2f", basicAmountCSV)).append(",").append(String.format("$%.2f", ytdEarnings)).append(",").append("INCOME TAX,").append(String.format("$%.2f", totalTaxCSV)).append(",").append(String.format("$%.2f", ytdPayrollTax)).append("\n");
                
                    if (hasOvertime.equals("true")) {
                        String overtimeRateString = overtimeRate + "";
                        String overtimeHoursString = overtimeHours + "";
                        String overtimeMoneyString = overtimeMoney + "";
                        writer.append("OVERTIME EARNINGS,$").append(overtimeRateString).append("$").append(overtimeHoursString).append(",$").append(overtimeMoneyString).append("$").append(overtimeMoneyString).append(",C.P.P,").append(String.format("$%.2f", cppDeductionCSV)).append(",").append(String.format("$%.2f", ytdCPP)).append("\n");
                        
                        writer.append("VACATION PAY,,").append(",").append(String.format("$%.2f", vacationPayCSV)).append(",").append(String.format("$%.2f", ytdVacationPay)).append(",E.I,").append(String.format("$%.2f", eiDeductionCSV)).append(",").append(String.format("$%.2f", ytdEI)).append("\n");
                       
                        if (ytdMealAllowance > 0.00) {
                            writer.append("MEAL ALLOWANCE,,").append(",")
                                .append(String.format("$%.2f", mealAllowanceDouble)).append(",")
                                .append(String.format("$%.2f", ytdMealAllowance))
                                .append("\n");
                        }

                        writer.append("BONUS,,").append(",").append(String.format("$%.2f", employeeAmountBonusCSV)).append(",").append(String.format("$%.2f", ytdBonus));
                

                        
                        writer.append("TOTAL,,").append(",").append(String.format("$%.2f", totalEarnings)).append(",").append(String.format("$%.2f", ytdEarnings + ytdVacationPay + ytdBonus + ytdMealAllowance)).append(",,").append(String.format("$%.2f", totalDeductions)).append(",").append(String.format("$%.2f", ytdCPP + ytdEI + ytdPayrollTax));
                
                        writer.append("\nNET PAY,,").append(",,,").append(String.format("$%.2f", netPay)).append(",").append(String.format("$%.2f", ytdNetPay)).append("\n\n\n");
                    }
                    else {
                        writer.append("VACATION PAY,,").append(",").append(String.format("$%.2f", vacationPayCSV)).append(",").append(String.format("$%.2f", ytdVacationPay)).append(",C.P.P,").append(String.format("$%.2f", cppDeductionCSV)).append(",").append(String.format("$%.2f", ytdCPP)).append("\n");
                
                            writer.append("BONUS,,").append(",").append(String.format("$%.2f", employeeAmountBonusCSV)).append(",").append(String.format("$%.2f", ytdBonus)).append(",E.I,").append(String.format("$%.2f", eiDeductionCSV)).append(",").append(String.format("$%.2f", ytdEI)).append("\n");
                
                            if (ytdMealAllowance > 0.00) {
                                writer.append("MEAL ALLOWANCE,,").append(",")
                                    .append(String.format("$%.2f", mealAllowanceDouble)).append(",")
                                    .append(String.format("$%.2f", ytdMealAllowance))
                                    .append("\n");
                            }
                            
                            writer.append("TOTAL,,").append(",").append(String.format("$%.2f", totalEarningsCSV)).append(",").append(String.format("$%.2f", ytdEarnings + ytdVacationPay + ytdBonus + ytdMealAllowance)).append(",,").append(String.format("$%.2f", totalDeductions)).append(",").append(String.format("$%.2f", ytdCPP + ytdEI + ytdPayrollTax));
                
                            writer.append("\nNET PAY,,").append(",,,,").append(String.format("$%.2f", netPayCSV)).append(",").append(String.format("$%.2f", ytdNetPay)).append("\n\n\n");
                    }
                
                    writer.append(companyNameCSV).append(",,,BANK\n");
                    writer.append(companyAddress).append(",,,BRANCH\n");
                    writer.append(companyCity).append(" ").append(postalCode).append(",,,CITY\n\n,,,FINANCIAL INST NO.\n,,,TRANSIT NO.\n");
                
                    writer.append(employeeNameCSV).append(",,,ACCOUNT NO.\n");
                    writer.append(employeeAddress).append(",,,AMOUNT,,$").append(String.format("%.2f", netPayCSV)).append("\n");
                    writer.append(employeeCity).append(" ").append(employeePostalCode).append(",,,DATE\n\n\n\n\n");
                    writer.append("PAYROLL ADVICE ONLY - NON NEGOTIABLE\n\n\n");
    
                startDate = endDate.plusDays(1);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // This autofills the table with all of the information 
    private void autoFillTable() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        //System.out.println("Using Table: " + tableName);
    
        String query = "SELECT * FROM " + tableName + " ORDER BY sl_no";
    
        List<List<Double>> columnData = new ArrayList<>();
        List<Integer> sumColumns = new ArrayList<>();
        List<String> fieldTypes = new ArrayList<>();
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
    
            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No data found in table '" + tableName + "' in the database '" + "'.", "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
    
            tablePanel.removeAll();
            paidCheckBoxes.clear();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(1, 1, 1, 1);
    
            // Initialize headers and sumColumns based on condition
            while (resultSet.next()) {
    
                headers = new String[]{
                    "sl_no", "pay_date", "hours_worked", "rate_per_hour", "overtime_hours", "overtime_rate", "amount_bonus", "basic_amount", "vacation_pay", "meal_Allowance",
                    "total_earnings", "federal_tax", "provincial_tax","income_tax", "cpp", "cpp2", "ei",
                    "total_deductions", "net_pay", "cra_remittance", "ctc", "paid" 
                };
                sumColumns = List.of(2, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

                // Initialize columnData to store column values
                for (int i = 0; i < headers.length; i++) {
                    columnData.add(new ArrayList<>());
                }
    
                gbc.gridy = 0;
                
                for (int col = 0; col < headers.length; col++) {
                    // Force two-line headers: split every header string in the middle (roughly)
                    String original = headers[col].replace("_", " ").toUpperCase();
                    String[] words = original.split(" ");
                    
                    StringBuilder firstLine = new StringBuilder();
                    StringBuilder secondLine = new StringBuilder();
                    
                    // Distribute words to lines
                    for (int w = 0; w < words.length; w++) {
                        if (w < words.length / 2) {
                            firstLine.append(words[w]).append(" ");
                        } else {
                            secondLine.append(words[w]).append(" ");
                        }
                    }
                
                    // Combine into HTML format
                    String formattedHeader = "<html><center>" + firstLine.toString().trim() + "<br>" + secondLine.toString().trim() + "</center></html>";
                
                    JLabel headerLabel = new JLabel(formattedHeader, JLabel.CENTER);
                    headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    headerLabel.setOpaque(true);
                    headerLabel.setBackground(new Color(230, 230, 250));
                    headerLabel.setBorder(BorderFactory.createMatteBorder(1, col == 0 ? 1 : 0, 1, 1, Color.GRAY));
                
                    // Set minimum width for each header
                    headerLabel.setPreferredSize(new Dimension(80, 40)); // Minimum width: 100px, height: 40px
                
                    gbc.gridx = col;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    tablePanel.add(headerLabel, gbc);
                }
                
    
                gbc.gridy = row;
                List<Component> currentRowComponents = new ArrayList<>();
    
                for (int col = 0; col < headers.length; col++) {
                    String columnName = headers[col];
                    String columnValue = resultSet.getString(columnName);
                    //System.out.println("Row: " + row + ", Column: " + columnName + ", Value: " + columnValue);
                    if (columnName.equals("paid")) {
                        JCheckBox paidCheckBox = new JCheckBox();
                        paidCheckBox.setSelected(resultSet.getBoolean("paid"));
                        paidCheckBox.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                        gbc.gridx = col;
    
                        int currentRowIndex = paidCheckBoxes.size();
                        paidCheckBox.addActionListener(e -> {
                            handlePaidCheckboxToggle(paidCheckBoxes, currentRowIndex);
                            updateRowState(paidCheckBox.isSelected(), currentRowComponents);
                        });
    
                        paidCheckBoxes.add(paidCheckBox);
                        tablePanel.add(paidCheckBox, gbc);
                        currentRowComponents.add(paidCheckBox);
                        fieldTypes.add("checkbox");
                    } else {
                        JTextField textField = new JTextField(columnValue);
                        textField.setEditable(false);
                        textField.setOpaque(true);
                        textField.setHorizontalAlignment(JTextField.CENTER);
                        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        //textField.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                        //System.out.println(columnName);
                        textField.setBackground(new Color(255, 204, 204)); // pastel red
                        if (columnName.equals("overtime_hours") || columnName.equals("overtime_rate")) {
                            textField.setBackground(new Color(255, 204, 204)); // pastel red
                        } else {
                            textField.setBackground(new Color(255, 204, 204)); // pastel red
                        }
                        gbc.gridx = col;
                        tablePanel.add(textField, gbc);
                        currentRowComponents.add(textField);

                        // Add column value to columnData for numeric columns
                        if (sumColumns.contains(col)) {
                            try {
                                columnData.get(col).add(Double.parseDouble(columnValue));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        fieldTypes.add("text");
                    }


                }
    
                updateRowState(resultSet.getBoolean("paid"), currentRowComponents);
                colorSpecificColumnsInRow(currentRowComponents, row, resultSet.getBoolean("paid"));
                row++;
            }
    
            // Calculate sums for columns
            double[] columnSums = new double[headers.length];
            for (int col : sumColumns) {
                columnSums[col] = columnData.get(col).stream().mapToDouble(Double::doubleValue).sum();
            }
    
            // Add sum row
            gbc.gridy = row;
            for (int col = 0; col < headers.length; col++) {
                if (sumColumns.contains(col)) {
                    JLabel sumLabel = new JLabel(String.format("%.2f", columnSums[col]), JLabel.CENTER);
                    sumLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    sumLabel.setOpaque(true);
                    sumLabel.setBackground(new Color(220, 220, 240));
                    gbc.gridx = col;
                    tablePanel.add(sumLabel, gbc);
                } else {
                    JLabel emptyLabel = new JLabel("", JLabel.CENTER);
                    emptyLabel.setOpaque(true);
                    emptyLabel.setBackground(new Color(220, 220, 240));
                    gbc.gridx = col;
                    tablePanel.add(emptyLabel, gbc);
                }
            }


            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.gridy = row;
            gbc2.gridx = 0;
            gbc.insets = new Insets(1, 1, 1, 1);
            
            if (canAddRowWithinSameYear()) {
                JButton addButton = new JButton("+");
                addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
                addButton.setFocusPainted(false);
                addButton.setBackground(Color.decode("#213f9e"));
                addButton.setForeground(Color.WHITE);
                addButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                addButton.addActionListener(e -> addNewRowToDatabase());
                tablePanel.add(addButton, gbc2);
                row++;
            }
            
            tablePanel.revalidate();
            tablePanel.repaint();
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data from database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    // This is the method used to give function to the "+" button when a time frame can be extended, this allows for a row to be inserting instead of dropping the entire table and just adds to the lowest SL.NO which already exists. 
    private void addNewRowToDatabase() {
        String employeeName = getEmployeeNameTable();
        String companyName = getCompanyNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement()) {
    
            String query = "SELECT * FROM " + tableName + " ORDER BY sl_no DESC LIMIT 1";
            ResultSet resultSet = statement.executeQuery(query);
    
            if (resultSet.next()) {
                int lastSLNo = resultSet.getInt("sl_no");
                LocalDate lastPayDate = resultSet.getDate("pay_date").toLocalDate();
    
            LocalDate newPayDate;


            switch (getPaymentFrequencyFromDatabase(companyName, employeeName)) {
                case "Weekly (52)":
                case "Weekly (53)":
                    newPayDate = lastPayDate.plusWeeks(1);
                    break;
            
                case "Bi-Weekly (26)":
                case "Bi-Weekly (27)":
                    newPayDate = lastPayDate.plusWeeks(2);
                    break;
            
                case "Monthly (12)":
                    newPayDate = lastPayDate.plusMonths(1);
                    break;
            
                case "Semi-Monthly (24)":
                    if (lastPayDate.getDayOfMonth() <= 15) {
                        newPayDate = lastPayDate.withDayOfMonth(lastPayDate.getMonth().length(lastPayDate.isLeapYear()));
                    } else {
                        newPayDate = lastPayDate.plusMonths(1).withDayOfMonth(15);
                    }
                    break;
            
                default:
                    throw new IllegalArgumentException("Unsupported payment frequency");
            }
            

            if (newPayDate.getYear() != lastPayDate.getYear()) {
                JOptionPane.showMessageDialog(null, "New pay date must be within the same year as the last pay date: " + lastPayDate.getYear(), "Invalid Pay Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
                String insertQuery = "INSERT INTO " + tableName +
                        " (sl_no, pay_date, hours_worked, rate_per_hour, basic_amount, vacation_pay, total_earnings, federal_tax, provincial_tax, " +
                        "income_tax, cpp, cpp2, ei, total_deductions, net_pay, cra_remittance, has_overtime, overtime_hours, overtime_rate, " +
                        "use_base_amount, base_amount, paid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, lastSLNo + 1);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(newPayDate));
                    preparedStatement.setDouble(3, resultSet.getDouble("hours_worked"));
                    preparedStatement.setDouble(4, resultSet.getDouble("rate_per_hour"));
                    preparedStatement.setDouble(5, resultSet.getDouble("basic_amount"));
                    preparedStatement.setDouble(6, resultSet.getDouble("vacation_pay"));
                    preparedStatement.setDouble(7, resultSet.getDouble("total_earnings"));
                    preparedStatement.setDouble(8, resultSet.getDouble("federal_tax"));
                    preparedStatement.setDouble(9, resultSet.getDouble("provincial_tax"));
                    preparedStatement.setDouble(10, resultSet.getDouble("income_tax"));
                    preparedStatement.setDouble(11, resultSet.getDouble("cpp"));
                    preparedStatement.setDouble(12, resultSet.getDouble("cpp2"));
                    preparedStatement.setDouble(13, resultSet.getDouble("ei"));
                    preparedStatement.setDouble(14, resultSet.getDouble("total_deductions"));
                    preparedStatement.setDouble(15, resultSet.getDouble("net_pay"));
                    preparedStatement.setDouble(16, resultSet.getDouble("cra_remittance"));
                    preparedStatement.setBoolean(17, resultSet.getBoolean("has_overtime"));
                    preparedStatement.setDouble(18, resultSet.getDouble("overtime_hours"));
                    preparedStatement.setDouble(19, resultSet.getDouble("overtime_rate"));
                    preparedStatement.setBoolean(20, resultSet.getBoolean("use_base_amount"));
                    preparedStatement.setDouble(21, resultSet.getDouble("base_amount"));
                    preparedStatement.setBoolean(22, false);
                    preparedStatement.executeUpdate();
                }
                autoFillTable();
            } else {
                JOptionPane.showMessageDialog(null, "No data found in table: " + tableName, "Error", JOptionPane.ERROR_MESSAGE);
            }
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding new row: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // This allows the program to retrive the province which the employee is in from the database allowing for the accurate exemptions and other info from the SQL.
    private String fetchProvinceCodeFromDatabase() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular);
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String provinceFullName = "";
        String provinceCode = "";
    
        String query = "SELECT province FROM " + tableName + " LIMIT 1"; 
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
    
            if (resultSet.next()) {
                provinceFullName = resultSet.getString("province");
                provinceCode = generalMethods.getProvinceCode(provinceFullName);
            }
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching province: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        return provinceCode;
    }
        
    // Returns the status of CPP and if the employee is CPP exempt
    private boolean getCPPExemptionStatus() {
        String employeeName = getEmployeeNameTable();
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String companyNameRegular = getCompanyName();
        String exemptionTableName = employeeName + "_" + getCompanyIdByName(companyNameRegular);
        boolean cppExempt = false;

        String query = "SELECT CPPExempt FROM " + exemptionTableName + " LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            if (resultSet.next()) {
                cppExempt = resultSet.getBoolean("CPPExempt");
           }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching CPP exemption status: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return cppExempt;
    }

    // Gets the payment freq of the user from the database which says 
    private String getPaymentFrequencyFromDatabase(String companyName, String employeeTableName) {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String paymentFrequency = null;
        String companyNameRegular = getCompanyName();
        String tableName = employeeTableName + "_" + getCompanyIdByName(companyNameRegular);

        String query = "SELECT frequency FROM " + tableName + " LIMIT 1";
        
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {
        
            if (resultSet.next()) {
                paymentFrequency = resultSet.getString("frequency");
            }
        
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching payment frequency: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return paymentFrequency;
    }
    
    // Returns the status of EI and if the employee is EI exempt
    private boolean getEIExemptionStatus() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();            
        String exemptionTableName = employeeName + "_" + getCompanyIdByName(companyNameRegular);
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        boolean eiExempt = false;

        String query = "SELECT EIExempt FROM " + exemptionTableName + " LIMIT 1";

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {
                
            if (resultSet.next()) {
                eiExempt = resultSet.getBoolean("EIExempt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching EI exemption status: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return eiExempt;
    }

    // Converts the year to the name of the table for the database, this allows it to correspondence to the years accurate exemptions, tax rate etc. 
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


    public static String getYear(JComboBox<String> yearComboBox) {
        String selectedYear = (String) yearComboBox.getSelectedItem();
    
        return selectedYear;
    }

    // Gets the max contribution of the CPP or EI to make sure that you dont cross over the max "threshold."
    public double getMaxContribution(String contributionType) {
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT max_amount FROM maxcontributions" + "_" + convertYearToDbName(yearComboBox).toLowerCase() + " WHERE contribution_type = ?";
        
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, contributionType);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("max_amount");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    // Gets the amount of payment periods that occur inside of one year.
    private double getPaymentPeriodsPerYear() {
        String employeeName = getEmployeeNameTable();
        String companyNameRegular = getCompanyName();

        String exemptionTableName = employeeName + "_" + getCompanyIdByName(companyNameRegular);
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        String query = "SELECT Frequency FROM " + exemptionTableName + " LIMIT 1";
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
    
            if (resultSet.next()) {
                String frequency = resultSet.getString("Frequency").trim();
    
                // Map the frequency value to periods per year
                switch (frequency) {
                    case "Weekly (52)":
                        return 52.0;
                    case "Weekly (53)":
                        return 53.0;
                    case "Bi-Weekly (26)":
                        return 26.0;
                    case "Bi-Weekly (27)":
                        return 27.0;
                    case "Semi-Monthly (24)":
                        return 24.0;
                    case "Monthly (12)":
                        return 12.0;
                    case "Quarterly (4)":
                        return 4.0;
                    case "Semi-Annually (2)":
                        return 2.0;
                    case "Yearly (1)":
                        return 1.0;
                    default:
                        throw new IllegalArgumentException("Unknown payment frequency: " + frequency);
                }
                
            } else {
                throw new IllegalStateException("No rows found in table: " + exemptionTableName);
            }
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching payment frequency: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return 0.0; // Return 0 if an error occurs
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return 0.0; // Return 0 if invalid frequency or table is empty
        }
    }

    // Calculate the adjustment of the tax period to make sure that the amount of tax is accurate, this adjusts for the taxes which have already been paid.
private double calculateTaxAdjustmentPerPeriod() {
    String employeeName = getEmployeeNameTable();
    String companyName = getCompanyNameTable();
    String companyNameRegular = getCompanyName();
    String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
    String url = dbInformation.getUrl();
    String username = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();
    
    String query = "SELECT * FROM " + tableName;
    
    try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query)) {
    
        double idealTotalTaxSum = 0.0;
        double paidTotalTaxSum = 0.0;
        int nonPaidRowCount = 0;
    
        // Precompute static data
        TaxCalculations taxCalculator = new TaxCalculations(convertYearToDbName(yearComboBox));
        String provinceCode = fetchProvinceCodeFromDatabase();
        double totalProvinceExemption = taxCalculator.calculateTotalProvinceExemption(provinceCode, 1, convertYearToDbName(yearComboBox));
        double totalFederalExemption = taxCalculator.calculateTotalFederalExemptions(provinceCode, 1, convertYearToDbName(yearComboBox));
        double exemptionDivisor = getPaymentPeriodsPerYear();

        // Iterate over rows
        while (resultSet.next()) {
            // Use a Map to store the row data temporarily
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("paid", resultSet.getBoolean("paid"));
            rowData.put("totalTax", resultSet.getBigDecimal("income_tax").doubleValue());
            rowData.put("basicAmount", resultSet.getBigDecimal("basic_amount").doubleValue());
            rowData.put("vacationPay", resultSet.getBigDecimal("vacation_pay").doubleValue());
            
            // Now that the entire row is fetched, proceed to calculation
            boolean paid = (boolean) rowData.get("paid");
            double totalTax = (double) rowData.get("totalTax");
            double basicAmount = (double) rowData.get("basicAmount");
            double vacationPay = (double) rowData.get("vacationPay");

            if (paid) {
                paidTotalTaxSum += totalTax;
            } else {
                nonPaidRowCount++;
            }

            double totalEarnings = basicAmount + vacationPay;
            double federalTax = (taxCalculator.calculateFederalPayrollTax(totalEarnings, "can", 1, convertYearToDbName(yearComboBox)) - (totalFederalExemption/exemptionDivisor));
            double provincialTax = (taxCalculator.calculateProvincialPayrollTax(totalEarnings, provinceCode, 1, convertYearToDbName(yearComboBox)) - (totalProvinceExemption/exemptionDivisor));

            if (provincialTax < 0) {
                provincialTax = 0.0;
            }

            if (federalTax < 0) {
                federalTax = 0.0;
            }

            double idealTotalTax = federalTax + provincialTax;
            System.out.println("idealTotalTax " + String.format("%.3f", idealTotalTax));

            // Round to two decimals
            idealTotalTax = Math.round(idealTotalTax * 100.0) / 100.0;

            idealTotalTaxSum += idealTotalTax;
        }

        System.out.println("paid total" + paidTotalTaxSum);
        System.out.println("idealtotaltaxsum" + idealTotalTaxSum);
        double outstandingTax = idealTotalTaxSum - paidTotalTaxSum;
        if (nonPaidRowCount > 0) {
            return outstandingTax / nonPaidRowCount;
        } else {
            return 0.0;
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error during tax adjustment calculation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return 0.0; // Return 0.0 if an error occurs
    }
}

        

    private double calculateCPPAdjustmentPerPeriod(double periodsPerYear, double cppExemption) {
        String employeeName = getEmployeeNameTable();
        String companyName = getCompanyNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        
        String query = "SELECT * FROM " + tableName;
        
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
        
            double idealCPPSum = 0.0;
            double paidCPPSum = 0.0;
            int nonPaidRowCount = 0;
        
            int i = 1;
            while (resultSet.next()) {
                i++;
                if (i == 6) {
                boolean paid = resultSet.getBoolean("paid");
                double cpp = resultSet.getBigDecimal("cpp").doubleValue();

                if (paid) {
                    paidCPPSum += cpp;
                } else {
                    nonPaidRowCount++;
                }
        
                double basicAmount = resultSet.getBigDecimal("basic_amount").doubleValue();
                double vacationPay = resultSet.getBigDecimal("vacation_pay").doubleValue();
                double totalEarnings = basicAmount + vacationPay;
        
                TaxCalculations taxCalculator = new TaxCalculations(convertYearToDbName(yearComboBox));
                double idealCPP = (taxCalculator.calculateCPP(totalEarnings*periodsPerYear - cppExemption, getCPPExemptionStatus(), convertYearToDbName(yearComboBox)))/periodsPerYear;

                idealCPPSum += idealCPP;
                i = 1;    
                }
            }
        
                double outstandingCPP = idealCPPSum - paidCPPSum;
                return nonPaidRowCount > 0 ? outstandingCPP / nonPaidRowCount : 0.0;
        
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error during CPP adjustment calculation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return 0.0;
            }
    }
        
    // Calculates if the amount of EI paid inside of a year is paid
    private double calculateEIAdjustmentPerPeriod() {
        String employeeName = getEmployeeNameTable();
        String companyName = getCompanyNameTable();
        String companyNameRegular = getCompanyName();
        String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        
        String query = "SELECT * FROM " + tableName;
        
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {
        
            double idealEISum = 0.0;
            double paidEISum = 0.0;
            int nonPaidRowCount = 0;
                
            int i = 1;
            while (resultSet.next()) {
                i++;
                if (i == 6) {
                boolean paid = resultSet.getBoolean("paid");
                double ei = resultSet.getBigDecimal("ei").doubleValue();
        
                    if (paid) {
                        paidEISum += ei;
                    } else {
                        nonPaidRowCount++;
                    }
        
                    double basicAmount = resultSet.getBigDecimal("basic_amount").doubleValue();
                    double vacationPay = resultSet.getBigDecimal("vacation_pay").doubleValue();
                    double totalEarnings = basicAmount + vacationPay;
        
                    TaxCalculations taxCalculator = new TaxCalculations(convertYearToDbName(yearComboBox));
                    double idealEI = taxCalculator.calculateEI(totalEarnings, getEIExemptionStatus());
                    idealEISum += idealEI;
                    i = 0;
                }
            }
        
            double outstandingEI = idealEISum - paidEISum;

            return nonPaidRowCount > 0 ? outstandingEI / nonPaidRowCount : 0.0;
        
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during EI adjustment calculation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return 0.0;
        }
    }
        
    // See's if you can one additional row inside of the program, this allows for you to adjust the amount of paystubs which you generate and that you can add more which are typically autofilled. 
        private boolean canAddRowWithinSameYear() {
            String employeeName = getEmployeeNameTable();
            String companyName = getCompanyNameTable();
            String companyNameRegular = getCompanyName();
            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
            String url = dbInformation.getUrl();
            String username = dbInformation.getUsername();
            String dbPassword = dbInformation.getPassword();
        
            try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                 Statement statement = connection.createStatement()) {
        
                String query = "SELECT pay_date FROM " + tableName + " ORDER BY sl_no DESC LIMIT 1";
                ResultSet resultSet = statement.executeQuery(query);
        
                if (resultSet.next()) {
                    LocalDate lastPayDate = resultSet.getDate("pay_date").toLocalDate();
                    int lastYear = lastPayDate.getYear();
        
                    String frequency = getPaymentFrequencyFromDatabase(companyName, employeeName);
                    LocalDate newPayDate;
        
                    switch (frequency) {
                        case "Weekly (52)":
                        case "Weekly (53)":
                            newPayDate = lastPayDate.plusWeeks(1);
                            newPayDate = adjustToFriday(newPayDate);
                            break;
                    
                        case "Bi-Weekly (26)":
                        case "Bi-Weekly (27)":
                            newPayDate = lastPayDate.plusWeeks(2);
                            newPayDate = adjustToFriday(newPayDate);
                            break;
                    
                        case "Semi-Monthly (24)":
                            if (lastPayDate.getDayOfMonth() <= 15) {
                                newPayDate = lastPayDate.withDayOfMonth(lastPayDate.lengthOfMonth());
                            } else {
                                newPayDate = lastPayDate.plusMonths(1).withDayOfMonth(15);
                            }
                            break;
                    
                        case "Monthly (12)":
                            newPayDate = lastPayDate.plusMonths(1).withDayOfMonth(lastPayDate.plusMonths(1).lengthOfMonth());
                            break;
                    
                        case "Quarterly (4)":
                            newPayDate = lastPayDate.plusMonths(3).withDayOfMonth(1);
                            break;
                    
                        case "Semi-Annually (2)":
                            newPayDate = lastPayDate.plusMonths(6).withDayOfMonth(1);
                            break;
                    
                        case "Yearly (1)":
                            newPayDate = lastPayDate.plusYears(1).withDayOfMonth(1);
                            break;
                    
                        default:
                            throw new IllegalArgumentException("Unsupported payment frequency: " + frequency);
                    }
                    
        
                    return newPayDate.getYear() == lastYear;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error checking row addition: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        
        // Adjusts the date of payment to friday to make sure that the payment occurs on an acurate date.
        private LocalDate adjustToFriday(LocalDate date) {
            while (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                date = date.plusDays(1);
            }
            return date;
        }

        public static int getPeriodsPerYear(String paymentFrequency) {
            switch (paymentFrequency) {
                case "Weekly (52)":
                    return 52;
                case "Weekly (53)":
                    return 53;
                case "Bi-Weekly (26)":
                    return 26;
                case "Bi-Weekly (27)":
                    return 27;
                case "Semi-Monthly (24)":
                    return 24;
                case "Monthly (12)":
                    return 12;
                case "Quarterly (4)":
                    return 4;
                case "Semi-Annually (2)":
                    return 2;
                case "Yearly (1)":
                    return 1;
                default:
                    throw new IllegalArgumentException("Unknown payment frequency: " + paymentFrequency);
            }
        }
        

        private void reCalculateValues() {
            System.out.println("Goes into recalculate");
            String employeeName = getEmployeeNameTable();
            String companyName = getCompanyNameTable();
            String companyNameRegular = getCompanyName();
            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";
            String url = dbInformation.getUrl();
            String username = dbInformation.getUsername();
            String dbPassword = dbInformation.getPassword();
            Boolean lastround = true;
            String query = "SELECT * FROM " + tableName;

                    Map<Integer, Double> cppAdjustments = getCPPAdjustments(employeeName, companyNameRegular, convertYearToDbName(yearComboBox));
                    Map<Integer, Double> eiAdjustments = getEIAdjustments(employeeName, companyNameRegular, convertYearToDbName(yearComboBox));
                    Map<Integer, Double> taxAdjustments = getIncomeTaxAdjustments(employeeName, companyNameRegular, convertYearToDbName(yearComboBox));

            try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {

                // Retrieve max contributions and exemptions
                double maxCPPContribution = getMaxContribution("CPP");
                double cppContributionsOG = maxCPPContribution;
                double maxEIContribution = getMaxContribution("EI");
                double cppExemption = new TaxCalculations(convertYearToDbName(yearComboBox)).cppExemptionsGet();

                double ytdCPP = 0.0;
                double ytdEI = 0.0;
                double ytdCPP2 = 0.0;

                // Get CPP and EI exemption status from separate table
                boolean cppExempt = getCPPExemptionStatus();
                boolean eiExempt = getEIExemptionStatus();
                double undoingCPP = 0.0;
                String provinceCode = fetchProvinceCodeFromDatabase();

                String paymentFrequency = getPaymentFrequencyFromDatabase(companyName, employeeName);
                
                int periodsPerYear = getPeriodsPerYear(paymentFrequency);

                List<List<Object>> rows = new ArrayList<>();

                while (resultSet.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getObject(i));
                    }
                    rows.add(row);
                }

                // for (List<Object> row : rows) {
                //     System.out.println(row);
                // }
                

                for (List<Object> row : rows) {
                    if ((Boolean) row.get(row.size() - 4)) continue;

                    System.out.println("========== Row Contents ==========");
                    for (int i = 0; i < row.size(); i++) {
                        Object value = row.get(i);
                        System.out.println("Index " + i + ": " + value + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                    }
                    System.out.println("==================================");

                    int slNo = (int) row.get(0);                                        // 1 - SL
                    java.sql.Date payDate = (java.sql.Date) row.get(1);                 // 2 - PAY DATE
                    double hoursWorked = ((BigDecimal) row.get(2)).doubleValue();       // 3 - HOURS WORKED
                    double ratePerHour = ((BigDecimal) row.get(3)).doubleValue();       // 4 - RATE
                    double basicAmount = ((BigDecimal) row.get(4)).doubleValue();       // 5 - BASIC AMOUNT
                    double vacationPay = ((BigDecimal) row.get(5)).doubleValue();       // 6 - VACATION PAY
                    double totalEarnings = ((BigDecimal) row.get(6)).doubleValue();     // 7 - TOTAL EARNINGS
                    double federalTax = ((BigDecimal) row.get(7)).doubleValue();        // 8 - FEDERAL TAX
                    double provincialTax = ((BigDecimal) row.get(8)).doubleValue();     // 9 - PROVINCIAL TAX
                    double incomeTax = ((BigDecimal) row.get(9)).doubleValue();         // 10 - INCOME TAX
                    double cpp = ((BigDecimal) row.get(10)).doubleValue();              // 11 - CPP
                    double cpp2 = ((BigDecimal) row.get(11)).doubleValue();             // 12 - CPP2
                    double ei = ((BigDecimal) row.get(12)).doubleValue();               // 13 - EI
                    double totalDeductions = ((BigDecimal) row.get(13)).doubleValue();  // 14 - TOTAL DEDUCTIONS
                    double netPay = ((BigDecimal) row.get(14)).doubleValue();           // 15 - NET PAY
                    double craRemittance = ((BigDecimal) row.get(15)).doubleValue();    // 16 - CRA REMITTANCE
                    boolean hasOvertime = (Boolean) row.get(16);                        // 17 - HAS OVERTIME
                    double overtimeHours = ((BigDecimal) row.get(17)).doubleValue();    // 18 - OVERTIME HOURS
                    double overtimeRate = ((BigDecimal) row.get(18)).doubleValue();     // 19 - OVERTIME RATE
                    boolean useBaseAmount = (Boolean) row.get(19);               
                    
                    double baseAmount = ((BigDecimal) row.get(20)).doubleValue();       // 21 - BASE AMOUNT
                    boolean paid = (Boolean) row.get(21);                               // 22 - PAID
                    double mealAllowance = ((BigDecimal) row.get(22)).doubleValue();    // 23 - MEAL ALLOWANCE
                    double amountBonus = ((BigDecimal) row.get(23)).doubleValue();
                    double ctc = ((BigDecimal) row.get(24)).doubleValue();

                    overtimeRate = ratePerHour * 1.5;
                    overtimeRate = roundToTwoDecimals(overtimeRate);

                    if (useBaseAmount) {
                        basicAmount = baseAmount;
                    } else {
                        basicAmount = ratePerHour * hoursWorked;
                    }

                    basicAmount = BigDecimal.valueOf(basicAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double overtimeEarnings = 0.0;

                    //System.out.println("has overtime? " + hasOvertime);

                    overtimeEarnings = overtimeHours * overtimeRate;

                    //System.out.println("OT HOURS " + overtimeHours + " | OT RATE " + overtimeRate);

                    vacationPay = (basicAmount + overtimeEarnings + mealAllowance + amountBonus) * 0.04;

                    vacationPay = roundToTwoDecimals(vacationPay);


                    totalEarnings = basicAmount + vacationPay + overtimeEarnings + mealAllowance + amountBonus;
                    totalEarnings = roundToTwoDecimals(totalEarnings);

                    double projectedAnnualEarnings = totalEarnings * periodsPerYear;

                    TaxCalculations taxCalculator = new TaxCalculations(convertYearToDbName(yearComboBox));
                    double totalProvinceExemption = taxCalculator.calculateTotalProvinceExemption(provinceCode, 1, convertYearToDbName(yearComboBox));
                    double totalFederalExemption = taxCalculator.calculateTotalFederalExemptions(provinceCode, 1, convertYearToDbName(yearComboBox));

                    federalTax = taxCalculator.calculateFederalPayrollTax(projectedAnnualEarnings, "can", 1, convertYearToDbName(yearComboBox)) / periodsPerYear;
                    provincialTax = taxCalculator.calculateProvincialPayrollTax(projectedAnnualEarnings, provinceCode, 1, convertYearToDbName(yearComboBox)) /periodsPerYear;
                    
                    federalTax = roundToTwoDecimals(federalTax);
                    provincialTax = roundToTwoDecimals(provincialTax);

                    //System.out.println(projectedAnnualEarnings + " projected annual");

                    double cppDeduction = (taxCalculator.calculateCPP(projectedAnnualEarnings - cppExemption, getCPPExemptionStatus(), convertYearToDbName(yearComboBox))) /periodsPerYear;
                    
                    double eiDeduction = taxCalculator.calculateEI(projectedAnnualEarnings, getEIExemptionStatus()) / periodsPerYear;
                    cppDeduction = roundToTwoDecimals(cppDeduction);

                    eiDeduction = roundToTwoDecimals(eiDeduction);



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

                    if (ytdEI + eiDeduction > maxEIContribution) {
                        eiDeduction = maxEIContribution - ytdEI;
                        eiDeduction = roundToTwoDecimals(eiDeduction);
                    
                        // Double-check and adjust if necessary
                        if (eiDeduction + ytdEI != maxEIContribution) {
                            double difference = maxEIContribution - (eiDeduction + ytdEI);
                            eiDeduction += difference;
                        }
                    }


                    federalTax = federalTax - (totalFederalExemption / periodsPerYear);
                    //System.out.println("FEDERAL TAX WITH DEDUCTION " + federalTax + "\n");
                    provincialTax = provincialTax - (totalProvinceExemption / periodsPerYear);

                    incomeTax = BigDecimal.valueOf(provincialTax + federalTax).setScale(2, RoundingMode.HALF_UP).doubleValue();

                    if (incomeTax < 0){
                        incomeTax = 0.0;
                    }

                    if (provincialTax < 0){
                        provincialTax = 0.0;
                    }

                    if (federalTax < 0){
                        federalTax = 0.0;
                    }

                    if (cppDeduction < 0){
                        cppDeduction = 0.0;
                    }

                    if (cppExempt) {
                        cppDeduction = 0.0;
                    }

                    if (eiDeduction < 0){
                        eiDeduction = 0.0;
                    }

                    if (eiExempt) {
                        eiDeduction = 0.0;
                    }

                    ytdCPP += cppDeduction;

                    ytdEI += eiDeduction;
                    

                    totalDeductions = incomeTax + cppDeduction + eiDeduction;
                    netPay = totalEarnings - totalDeductions;

                    System.out.println("Elements of CRA Remittance | " + "INCOME TAX " + incomeTax  + " | CPPDeduction * 2" + (cppDeduction*2) + "| CPP2 * 2 " + (cpp2Deduction*2) + "| EI REG " + eiDeduction + "| EI 1.4" + (eiDeduction * 1.4));

                    craRemittance = incomeTax + (cppDeduction * 2) + (cpp2Deduction * 2)+ eiDeduction + (eiDeduction * 1.4);
                    ctc = craRemittance + totalEarnings;




                    String updateQuery = "UPDATE " + tableName + " SET pay_date = ?, hours_worked = ?, rate_per_hour = ?, basic_amount = ?, "
                    + "vacation_pay = ?, total_earnings = ?, federal_tax = ?, provincial_tax = ?, income_tax = ?, cpp = ?, ei = ?, "
                    + "total_deductions = ?, net_pay = ?, cra_remittance = ?, has_overtime = ?, overtime_hours = ?, overtime_rate = ?, "
                    + "use_base_amount = ?, base_amount = ?, paid = ?, meal_Allowance = ?, ctc = ? WHERE sl_no = ?";
            
                    try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                        pstmt.setDate(1, payDate);
                        pstmt.setDouble(2, hoursWorked);
                        pstmt.setDouble(3, ratePerHour);
                        pstmt.setDouble(4, basicAmount);
                        pstmt.setDouble(5, vacationPay);
                        pstmt.setDouble(6, totalEarnings);
                        pstmt.setDouble(7, federalTax);
                        pstmt.setDouble(8, provincialTax);
                        pstmt.setDouble(9, incomeTax);
                        pstmt.setDouble(10, cppDeduction);
                        pstmt.setDouble(11, eiDeduction);
                        pstmt.setDouble(12, totalDeductions);
                        pstmt.setDouble(13, netPay);
                        pstmt.setDouble(14, craRemittance);
                        pstmt.setBoolean(15, hasOvertime);
                        pstmt.setDouble(16, overtimeHours);
                        pstmt.setDouble(17, overtimeRate);
                        pstmt.setBoolean(18, useBaseAmount);
                        pstmt.setDouble(19, baseAmount);
                        pstmt.setBoolean(20, paid);
                        pstmt.setDouble(21, mealAllowance);
                        pstmt.setDouble(22, ctc);
                        pstmt.setInt(23, slNo);                     
                        pstmt.executeUpdate();
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error during recalculation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public double getTotalEarnings(int slNo, String tableName) {
            double totalEarnings = 0.0;

            String query = "SELECT total_earnings FROM " + tableName + " WHERE sl_no = ?";

            try (Connection connection = DriverManager.getConnection(dbInformation.getUrl(), dbInformation.getUsername(), dbInformation.getPassword());
                PreparedStatement stmt = connection.prepareStatement(query)) {

                stmt.setInt(1, slNo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalEarnings = rs.getBigDecimal("total_earnings").doubleValue();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return totalEarnings;
        }


        public void applyAdjustments(String employeeName, String companyName, String yearDbName) {
            String companyNameRegular = getCompanyName();
            
            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + yearDbName + "_pap";
            System.out.println(tableName);
            Map<Integer, Double> cppMap = getCPPAdjustments(employeeName, companyName, convertYearToDbName(yearComboBox));
            Map<Integer, Double> eiMap = getEIAdjustments(employeeName, companyName, convertYearToDbName(yearComboBox));
            Map<Integer, Double> taxMap = getIncomeTaxAdjustments(employeeName, companyName, convertYearToDbName(yearComboBox));

            try (Connection connection = DriverManager.getConnection(dbInformation.getUrl(), dbInformation.getUsername(), dbInformation.getPassword())) {

                for (Integer slNo : cppMap.keySet()) {
                    // 1. Fetch original payroll values
                    String selectQuery = "SELECT cpp, ei, income_tax, total_earnings FROM " + tableName + " WHERE sl_no = ?";
                    double cpp = 0, ei = 0, incomeTax = 0, totalEarnings = 0;

                    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                        selectStmt.setInt(1, slNo);
                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                cpp = rs.getBigDecimal("cpp").doubleValue();
                                ei = rs.getBigDecimal("ei").doubleValue();
                                incomeTax = rs.getBigDecimal("income_tax").doubleValue();
                                totalEarnings = rs.getBigDecimal("total_earnings").doubleValue();
                            }
                        }
                    }

                    // 2. Apply deltas (if present)
                    if (cppMap.containsKey(slNo)) cpp += cppMap.get(slNo);
                    if (eiMap.containsKey(slNo)) ei += eiMap.get(slNo);
                    if (taxMap.containsKey(slNo)) incomeTax += taxMap.get(slNo);

                    // Round all values
                    cpp = roundToTwoDecimals(cpp);
                    ei = roundToTwoDecimals(ei);
                    incomeTax = roundToTwoDecimals(incomeTax);

                    // 3. Recalculate derived values
                    double totalDeductions = roundToTwoDecimals(incomeTax + cpp + ei);
                    double netPay = roundToTwoDecimals(totalEarnings - totalDeductions);
                    double cra = roundToTwoDecimals(incomeTax + (cpp * 2) + (ei * 2.4));
                    double ctc = roundToTwoDecimals(cra + totalEarnings);

                    // 4. Update database row
                    String updateQuery = "UPDATE " + tableName + " SET cpp = ?, ei = ?, income_tax = ?, total_deductions = ?, net_pay = ?, cra_remittance = ?, ctc = ? WHERE sl_no = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setDouble(1, cpp);
                        updateStmt.setDouble(2, ei);
                        updateStmt.setDouble(3, incomeTax);
                        updateStmt.setDouble(4, totalDeductions);
                        updateStmt.setDouble(5, netPay);
                        updateStmt.setDouble(6, cra);
                        updateStmt.setDouble(7, ctc);
                        updateStmt.setInt(8, slNo);
                        updateStmt.executeUpdate();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }




        public static double roundToTwoDecimals(double value) {
            return BigDecimal.valueOf(value)
                             .setScale(2, RoundingMode.HALF_UP)
                             .doubleValue();
        }
        
        public double getCppRate(String taxYearDbName) {
            if (taxYearDbName.equalsIgnoreCase("twenty_twenty_five")) return 0.0595;
            if (taxYearDbName.equalsIgnoreCase("twenty_twenty_four")) return 0.0595;
            return 0.0595; // default fallback
        }

        // Handles the logic about the paid column and that you can only pay in sequential order and cant pay someone with the payments which happen further down the year occur first.
        private void handlePaidCheckboxToggle(List<JCheckBox> checkboxes, int index) {
            JCheckBox currentCheckbox = checkboxes.get(index);
        
            if (currentCheckbox.isSelected()) {
                for (int i = 0; i < index; i++) {
                    if (!checkboxes.get(i).isSelected()) {
                        currentCheckbox.setSelected(false);
                        JOptionPane.showMessageDialog(this, "You must check previous entries first.");
                        return;
                    }
                }
        
                List<Component> rowComponents = getComponentsInRow(index + 1);
                
                double incomeTax = Double.parseDouble(((JTextField) rowComponents.get(13)).getText());
                double cppDeduction = Double.parseDouble(((JTextField) rowComponents.get(14)).getText());
                double eiDeduction = Double.parseDouble(((JTextField) rowComponents.get(16)).getText());
                double totalEarnings = Double.parseDouble(((JTextField) rowComponents.get(10)).getText());

                double hours = Double.parseDouble(((JTextField) rowComponents.get(2)).getText());
                double rate = Double.parseDouble(((JTextField) rowComponents.get(3)).getText());
                double overtimeHours = Double.parseDouble(((JTextField) rowComponents.get(4)).getText());
                double overtimeRate = Double.parseDouble(((JTextField) rowComponents.get(5)).getText());
                double mealAllowance = Double.parseDouble(((JTextField) rowComponents.get(9)).getText());
                double forVacationPay = (hours * rate) + (overtimeHours * overtimeRate) + mealAllowance;

                double vacationPayEstimator = forVacationPay * 0.04;

                double vacationPay = Double.parseDouble(((JTextField) rowComponents.get(8)).getText());

                // double tempTotalEarnings = (hours * rate) + (overtimeHours * overtimeRate) + mealAllowance + vacationPay;
                // tempTotalEarnings = roundToTwoDecimals(tempTotalEarnings);

                try {
                    mealAllowance = Double.parseDouble(((JTextField) rowComponents.get(9)).getText());
                } catch (Exception e) {
                    mealAllowance = 0.0; // fallback
                }
        
                // If this is a zeroed row
                if (((JTextField) rowComponents.get(2)).getText().equals("0") || ((JTextField) rowComponents.get(2)).getText().equals("0.00")) {
                    for (int i = 2; i < rowComponents.size() - 1; i++) {
                        ((JTextField) rowComponents.get(i)).setText("0.00");
                    }
        
                } else {
                    double totalDeductions = incomeTax + cppDeduction + eiDeduction;
                    double netPay = totalEarnings - totalDeductions + mealAllowance; // ✅ Add mealAllowance to netPay
                    double craRemittance = incomeTax + (cppDeduction * 2) + eiDeduction + (eiDeduction * 1.4);
                    double ctc = craRemittance + totalEarnings;

                    ((JTextField) rowComponents.get(17)).setText(String.format("%.2f", totalDeductions));
                    ((JTextField) rowComponents.get(18)).setText(String.format("%.2f", netPay));
                    ((JTextField) rowComponents.get(19)).setText(String.format("%.2f", craRemittance));
                    ((JTextField) rowComponents.get(20)).setText(String.format("%.2f", ctc));
                    ((JTextField) rowComponents.get(9)).setText(String.format("%.2f", mealAllowance));
                    ((JTextField) rowComponents.get(8)).setText(String.format("%.2f", vacationPay));
                }
        
                updateRowState(true, rowComponents);
        
            } else {
                for (int i = index + 1; i < checkboxes.size(); i++) {
                    if (checkboxes.get(i).isSelected()) {
                        currentCheckbox.setSelected(true);
                        JOptionPane.showMessageDialog(this, "You must uncheck later entries first.");
                        return;
                    }
                }
        
                List<Component> rowComponents = getComponentsInRow(index + 1);
                updateRowState(false, rowComponents);
                colorSpecificColumnsInRow(rowComponents, tablePanel.getComponentCount() / headers.length - 2, false);
            }
        
            paidCheckBoxes.set(index, currentCheckbox);
        }        
        
        public Map<Integer, Double> getCPPAdjustments(String employeeName, String companyName, String yearDbName) {
            Map<Integer, Double> cppAdjustments = new LinkedHashMap<>();
            double ytdActual = 0.0, ytdIdeal = 0.0;
            List<Integer> unpaidSLNos = new ArrayList<>();
            List<Double> idealValues = new ArrayList<>();
            String companyNameRegular = getCompanyName();

            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";

            try (Connection connection = DriverManager.getConnection(dbInformation.getUrl(), dbInformation.getUsername(), dbInformation.getPassword());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {

                TaxCalculations taxCalculator = new TaxCalculations(yearDbName);
                boolean cppExempt = getCPPExemptionStatus();
                int periodsPerYear = getPeriodsPerYear(getPaymentFrequencyFromDatabase(companyName, employeeName));

                while (resultSet.next()) {
                    int slNo = resultSet.getInt("sl_no");
                    double totalEarnings = resultSet.getBigDecimal("total_earnings").doubleValue();
                    double actualCPP = resultSet.getBigDecimal("cpp").doubleValue();
                    boolean paid = resultSet.getBoolean("paid");

                    double projectedAnnual = totalEarnings * periodsPerYear;
                    double idealCPP = cppExempt ? 0.0 : taxCalculator.calculateCPP(projectedAnnual - taxCalculator.cppExemptionsGet(), cppExempt, yearDbName) / periodsPerYear;

                    ytdActual += actualCPP;
                    ytdIdeal += idealCPP;

                    if (!paid) {
                        unpaidSLNos.add(slNo);
                        idealValues.add(idealCPP);
                    }
                }

                double correction = ytdIdeal - ytdActual;
                int unpaidPeriods = unpaidSLNos.size();

                if (unpaidPeriods == 0) return cppAdjustments;

                double distributed = correction / unpaidPeriods;
                distributed = BigDecimal.valueOf(distributed).setScale(2, RoundingMode.HALF_UP).doubleValue();

                double totalDistributed = 0.0;

                for (int i = 0; i < unpaidPeriods; i++) {
                    double adj = (i == unpaidPeriods - 1)
                        ? correction - totalDistributed // Final correction to ensure exact match
                        : distributed;

                    adj = BigDecimal.valueOf(adj).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    cppAdjustments.put(unpaidSLNos.get(i), adj);
                    totalDistributed += adj;
                }

                // ✅ Validation
                double finalTotal = ytdActual + totalDistributed;
                if (Math.abs(finalTotal - ytdIdeal) > 0.01) {
                    throw new IllegalStateException("CPP adjustment validation failed. Total after adjustment is not matching ideal.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return cppAdjustments;
        }

        public Map<Integer, Double> getEIAdjustments(String employeeName, String companyName, String yearDbName) {
            Map<Integer, Double> eiAdjustments = new LinkedHashMap<>();
            double ytdActual = 0.0, ytdIdeal = 0.0;
            List<Integer> unpaidSLNos = new ArrayList<>();
            List<Double> idealValues = new ArrayList<>();
            String companyNameRegular = getCompanyName();

            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";

            try (Connection connection = DriverManager.getConnection(dbInformation.getUrl(), dbInformation.getUsername(), dbInformation.getPassword());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {

                TaxCalculations taxCalculator = new TaxCalculations(yearDbName);
                boolean eiExempt = getEIExemptionStatus();
                int periodsPerYear = getPeriodsPerYear(getPaymentFrequencyFromDatabase(companyName, employeeName));

                while (resultSet.next()) {
                    int slNo = resultSet.getInt("sl_no");
                    double totalEarnings = resultSet.getBigDecimal("total_earnings").doubleValue();
                    double actualEI = resultSet.getBigDecimal("ei").doubleValue();
                    boolean paid = resultSet.getBoolean("paid");

                    double projectedAnnual = totalEarnings * periodsPerYear;
                    double idealEI = eiExempt ? 0.0 : taxCalculator.calculateEI(projectedAnnual, eiExempt) / periodsPerYear;

                    ytdActual += actualEI;
                    ytdIdeal += idealEI;

                    if (!paid) {
                        unpaidSLNos.add(slNo);
                        idealValues.add(idealEI);
                    }
                }

                double correction = ytdIdeal - ytdActual;
                int unpaidPeriods = unpaidSLNos.size();

                if (unpaidPeriods == 0) return eiAdjustments;

                double distributed = BigDecimal.valueOf(correction / unpaidPeriods).setScale(2, RoundingMode.HALF_UP).doubleValue();
                double totalDistributed = 0.0;

                for (int i = 0; i < unpaidPeriods; i++) {
                    double adj = (i == unpaidPeriods - 1)
                        ? correction - totalDistributed
                        : distributed;

                    adj = BigDecimal.valueOf(adj).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    eiAdjustments.put(unpaidSLNos.get(i), adj);
                    totalDistributed += adj;
                }

                double finalTotal = ytdActual + totalDistributed;
                if (Math.abs(finalTotal - ytdIdeal) > 0.01) {
                    throw new IllegalStateException("EI adjustment validation failed. Total after adjustment is not matching ideal.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return eiAdjustments;
        }        

        public Map<Integer, Double> getIncomeTaxAdjustments(String employeeName, String companyName, String yearDbName) {
            Map<Integer, Double> taxAdjustments = new LinkedHashMap<>();
            double ytdActual = 0.0, ytdIdeal = 0.0;
            List<Integer> unpaidSLNos = new ArrayList<>();
            List<Double> idealValues = new ArrayList<>();
            String companyNameRegular = getCompanyName();

            String tableName = employeeName + "_" + getCompanyIdByName(companyNameRegular) + convertYearToDbNameID(yearComboBox) + "_pap";

            try (Connection connection = DriverManager.getConnection(dbInformation.getUrl(), dbInformation.getUsername(), dbInformation.getPassword());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {

                TaxCalculations taxCalculator = new TaxCalculations(yearDbName);
                String provinceCode = fetchProvinceCodeFromDatabase();
                int periodsPerYear = getPeriodsPerYear(getPaymentFrequencyFromDatabase(companyName, employeeName));

                double totalFederalExemption = taxCalculator.calculateTotalFederalExemptions(provinceCode, 1, yearDbName);
                double totalProvinceExemption = taxCalculator.calculateTotalProvinceExemption(provinceCode, 1, yearDbName);

                while (resultSet.next()) {
                    int slNo = resultSet.getInt("sl_no");
                    double totalEarnings = resultSet.getBigDecimal("total_earnings").doubleValue();
                    double actualIncomeTax = resultSet.getBigDecimal("income_tax").doubleValue();
                    boolean paid = resultSet.getBoolean("paid");

                    double projectedAnnual = totalEarnings * periodsPerYear;

                    double federal = taxCalculator.calculateFederalPayrollTax(projectedAnnual, "can", 1, yearDbName) / periodsPerYear;
                    double provincial = taxCalculator.calculateProvincialPayrollTax(projectedAnnual, provinceCode, 1, yearDbName) / periodsPerYear;

                    federal -= totalFederalExemption / periodsPerYear;
                    provincial -= totalProvinceExemption / periodsPerYear;

                    double idealTax = Math.max(0, federal + provincial);

                    ytdActual += actualIncomeTax;
                    ytdIdeal += idealTax;

                    if (!paid) {
                        unpaidSLNos.add(slNo);
                        idealValues.add(idealTax);
                    }
                }

                double correction = ytdIdeal - ytdActual;
                int unpaidPeriods = unpaidSLNos.size();

                if (unpaidPeriods == 0) return taxAdjustments;

                double distributed = BigDecimal.valueOf(correction / unpaidPeriods).setScale(2, RoundingMode.HALF_UP).doubleValue();
                double totalDistributed = 0.0;

                for (int i = 0; i < unpaidPeriods; i++) {
                    double adj = (i == unpaidPeriods - 1)
                        ? correction - totalDistributed
                        : distributed;

                    adj = BigDecimal.valueOf(adj).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    taxAdjustments.put(unpaidSLNos.get(i), adj);
                    totalDistributed += adj;
                }

                double finalTotal = ytdActual + totalDistributed;
                if (Math.abs(finalTotal - ytdIdeal) > 0.01) {
                    throw new IllegalStateException("Income tax adjustment validation failed. Total after adjustment is not matching ideal.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return taxAdjustments;
        }


        private void updateRowState(boolean isPaid, List<Component> rowComponents) {
            // Call this for all rows regardless of isPaid
            colorSpecificColumnsInRow(rowComponents, tablePanel.getComponentCount() / headers.length - 2, isPaid);
        
            for (int i = 0; i < rowComponents.size(); i++) {
                Component comp = rowComponents.get(i);
        
                if (comp instanceof JTextField) {
                    JTextField textField = (JTextField) comp;
        
                    // Only these fields should be editable when unpaid
                    boolean shouldBeEditable = !isPaid && (
                        i == 1 || i == 2 || i == 3 || i == 4 || i == 6 || i == 9 || i == 10 || i == 13 || i == 14 || i == 15 || i == 16
                    );
        
                    textField.setEditable(shouldBeEditable);
                }
            }
        }
        

    private void colorSpecificColumnsInRow(List<Component> rowComponents, int row, boolean isPaid) {
        for (int i = 0; i < rowComponents.size(); i++) {
            Component comp = rowComponents.get(i);
    
            if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                textField.setOpaque(true);
    
                if (isPaid) {
                    textField.setBackground(Color.GREEN);
                } else {
                    // 🎯 Highlight specific columns only if unpaid
                    if (i == 0 || i == 5 || i == 7 || i == 8 || i == 11 || i == 12 || i == 17 || i == 18 || i == 19 || i == 20) {
                        textField.setBackground(new Color(255, 204, 204)); // pastel red
                    } else {
                        textField.setBackground(Color.WHITE);
                    }
                }
            }

            if (comp instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) comp;
                checkBox.setOpaque(true); // required for background color to show
            
                if (isPaid) {
                    checkBox.setBackground(Color.GREEN);
                } else {
                    checkBox.setBackground(Color.WHITE); // or match the row's default
                }
            }

        }
    }
    

    public static void main(String[] args) {
        JFrame frame = new JFrame("Payroll Projections Yearlong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        CardLayout cardLayout = new CardLayout();
        JPanel parentPanel = new JPanel(cardLayout);


        parentPanel.add(new PayrollYearlyTablePage(parentPanel, cardLayout), "PayrollProjectionsYearlong");
        frame.add(parentPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

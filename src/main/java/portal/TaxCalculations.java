package portal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;



public class TaxCalculations {
    public DatabaseInformation dbInformation = new DatabaseInformation();
    private double vacationPayRate;
    private double EIRate;
    private double CPPExemption;
    private double federalExemption;

    public TaxCalculations(String dbCurrent) {
        loadExemptionValues(dbCurrent);
    }

    // Load the exmeptions and other important tax values from the exemptions table, this will allow the TaxCalculations class to have the updated exemption values as set out by the CRA.
    private void loadExemptionValues(String dbCurrent) {
        String query = "SELECT * FROM exemptions_" + dbCurrent + " LIMIT 1";
        String url = dbInformation.getUrl();;
        String dbUsername = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                vacationPayRate = resultSet.getDouble("vacationPayRate");
                EIRate = resultSet.getDouble("EIRate");
                CPPExemption = resultSet.getDouble("CPPExemption");
                federalExemption = resultSet.getDouble("federalExemption");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

private List<TaxBracket> fetchTaxBrackets(String provinceCode, String dbCurrent) {
    List<TaxBracket> taxBrackets = new ArrayList<>();
    String url = dbInformation.getUrl();;
    String dbUsername = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();
    String query = "SELECT bracket_limit, tax_bracket_percentage FROM " + provinceCode.toLowerCase() + "_" + dbCurrent + " ORDER BY bracket_limit DESC";
    try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
         PreparedStatement statement = connection.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
            double limit = resultSet.getDouble("bracket_limit");
            double rate = resultSet.getDouble("tax_bracket_percentage") / 100.0; // Convert to decimal

            // Add a TaxBracket object with `limit` and `rate`
            taxBrackets.add(new TaxBracket(limit, rate));
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return taxBrackets;
}


public boolean TD1Status(String employeeSIN, String employeeName, String companyName, int CompanyID) {
    boolean status = false;
    String url = dbInformation.getUrl();;
    String dbUsername = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();

    String tableName = employeeName.replaceAll("\\W+", "_");

    String query = "SELECT UseTD1 FROM " + tableName + "_" + CompanyID + " WHERE EmployeeSIN = ?";

    try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
         PreparedStatement preparedStatement = connection.prepareStatement(query)) {

        // Set the Employee SIN for the query
        preparedStatement.setString(1, employeeSIN);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                // Fetch the UseTD1 value from the table
                status = resultSet.getBoolean("UseTD1");
            } else {
                System.out.println("No record found for EmployeeSIN: " + employeeSIN);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error retrieving TD1 status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    return status;
}


public double getProvincialExemption(String provinceCode, String dbCurrent) {
    double exemption = 0.0;
    String url = dbInformation.getUrl();;
    String dbUsername = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();
    String query = "SELECT basic_personal_exemption FROM provincialexemptions_" + dbCurrent.toLowerCase() + " WHERE province_code = ? LIMIT 1";
    try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
         PreparedStatement statement = connection.prepareStatement(query)) {

        statement.setString(1, provinceCode);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            exemption = resultSet.getDouble("basic_personal_exemption");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    return exemption;
}

// Fetch the lowest tax rate for a given province or federal tax
private double getLowestTaxRate(String tableName, String dbCurrent) {
    double lowestRate = 0.0;
    String url = dbInformation.getUrl();;
    String dbUsername = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();
    String query = "SELECT tax_bracket_percentage FROM " + tableName.toLowerCase() + "_" + dbCurrent + " ORDER BY bracket_limit ASC LIMIT 1";
    try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
         PreparedStatement statement = connection.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
            lowestRate = resultSet.getDouble("tax_bracket_percentage") / 100.0; // Convert to decimal
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    return lowestRate;
}

public double calculateTotalProvinceExemption(String provinceCode, int paymentPeriodsAnnual, String dbCurrent) {

    double provincialExemption = getProvincialExemption(provinceCode, dbCurrent) / paymentPeriodsAnnual;
    double lowestProvincialRate = getLowestTaxRate(provinceCode, dbCurrent);
    double adjustedProvincialExemption = provincialExemption * lowestProvincialRate;

    return adjustedProvincialExemption;
}

public double calculateTotalFederalExemptions(String provinceCode, int paymentPeriodsAnnual, String dbCurrent) {
    double federalExemptionPerPeriod = federalExemption / paymentPeriodsAnnual;
    double lowestFederalRate = getLowestTaxRate("can", dbCurrent);
    double adjustedFederalExemption = federalExemptionPerPeriod * lowestFederalRate;
    return adjustedFederalExemption;
}

// Calculate provincial payroll tax based on income and province
public double calculateProvincialPayrollTax(double income, String provinceCode, int paymentPeriodsAnnual, String dbCurrent) {
    List<TaxBracket> taxBrackets = fetchTaxBrackets(provinceCode, dbCurrent);
    double totalPayrollTaxProvince = 0.0;
    double amountLeftToTax = income;

    for (int i = 0; i < taxBrackets.size(); i++) {
        TaxBracket bracket = taxBrackets.get(i);
        double bracketLimit = bracket.getLimit();
        double bracketRate = bracket.getRate();

        if (amountLeftToTax > bracketLimit) {
            double taxableAmount = amountLeftToTax - bracketLimit;
            totalPayrollTaxProvince += (taxableAmount / paymentPeriodsAnnual) * bracketRate;
            amountLeftToTax = bracketLimit;
        } 
        
        if (i == taxBrackets.size() - 1) {
            totalPayrollTaxProvince += (amountLeftToTax / paymentPeriodsAnnual) * bracketRate;
        }
    }

    return totalPayrollTaxProvince;
}

public double calculateFederalPayrollTax(double income, String provinceCode, int paymentPeriodsAnnual, String dbCurrent) {
    List<TaxBracket> taxBrackets = fetchTaxBrackets(provinceCode, dbCurrent);
    double totalFederalPayrollTax = 0.0;
    double amountLeftToTax = income;

    for (int i = 0; i < taxBrackets.size(); i++) {
        TaxBracket bracket = taxBrackets.get(i);
        double bracketRate = bracket.getRate();
        double nextBracketLimit = (i < taxBrackets.size() - 1) ? taxBrackets.get(i + 1).getLimit() : 0;


        if (amountLeftToTax > nextBracketLimit) {
            double taxableAmount = amountLeftToTax - nextBracketLimit;
            totalFederalPayrollTax += (taxableAmount / paymentPeriodsAnnual) * bracketRate;
            amountLeftToTax = nextBracketLimit; 
        }

        if (i == taxBrackets.size() - 1) {
            totalFederalPayrollTax += (amountLeftToTax / paymentPeriodsAnnual) * bracketRate;
        }
    }

    return totalFederalPayrollTax;
}


    public double cppExemptionsGet(){
        return CPPExemption;
    }

    public double getMaxContribution(String contributionType, String dbCurrent) {
    String url = dbInformation.getUrl();;
    String dbUsername = dbInformation.getUsername();
    String dbPassword = dbInformation.getPassword();
        String query = "SELECT max_amount FROM maxcontributions" + "_" + dbCurrent.toLowerCase() + " WHERE contribution_type = ?";
        
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
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

    public double calculateCPP(double totalEarnings, boolean isCPPExempt, String taxYearString) {

        double CPPRate = 0.0595;
    

        if (isCPPExempt) return 0.0; 
    
        double CPP1 = totalEarnings * CPPRate;
        return Math.max(0, CPP1);
    }
    
    public double calculatePartialCPP2(double cpp2EligibleIncome, String taxYearString) {
        int taxYear = 0;
        if (taxYearString.equalsIgnoreCase("twenty_twenty_four")) {
            taxYear = 2024;
        } else if (taxYearString.equalsIgnoreCase("twenty_twenty_five")) {
            taxYear = 2025;
        }
    
        double CPP2Rate = 0.04;
        double ympe = (taxYear == 2025) ? 71300.0 : 68500.0;
        double aympe = (taxYear == 2025) ? 81200.0 : 73200.0;
    
        double maxCPP2Income = aympe - ympe;
    
        // Ensure we do not exceed max eligible income for CPP2
        double incomeUsed = Math.min(cpp2EligibleIncome, maxCPP2Income);
    
        return incomeUsed * CPP2Rate;
    }
    

    public double calculateCPP2(double totalEarnings) {
        return (totalEarnings * 0.04);
    }
    


    public double calculateEI(double totalEarnings, boolean isEIExempt) {
        if (isEIExempt) return 0.0;
        return EIRate * totalEarnings;
    }

    public double calculateTotalDeductions(double totalEarnings, boolean isCPPExempt, boolean isEIExempt, String provinceCode, int paymentPeriodsAnnual, String dbCurrent, String yearComboBox) {
        double totalPayrollTaxProvince = calculateProvincialPayrollTax(totalEarnings, provinceCode, paymentPeriodsAnnual, dbCurrent);
        double totalFederalTax = calculateFederalPayrollTax(totalEarnings, "can", paymentPeriodsAnnual, dbCurrent);
        double CPPPaid = calculateCPP(totalEarnings, isCPPExempt, yearComboBox);
        double EIPaid = calculateEI(totalEarnings, isEIExempt);
        return totalFederalTax + totalPayrollTaxProvince + CPPPaid + EIPaid;
    }

    public double calculateVacationPay(double totalEarnings) {
        return totalEarnings * vacationPayRate;
    }

    private static class TaxBracket {
        private final double limit;
        private final double rate;

        public TaxBracket(double limit, double rate) {
            this.limit = limit;
            this.rate = rate;
        }

        public double getLimit() {
            return limit;
        }

        public double getRate() {
            return rate;
        }
    }
}

package portal;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.sql.PreparedStatement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class PDFGeneration {

    private String employeeName;
    private String employeeNameDatabase;
    private String companyName;
    private String companyNameRegular;
    private int companyID;
    private String yearComboBoxValue;
    private String tableNamePAP;

    private String url;
    private String username;
    private String dbPassword;

    private DatabaseInformation dbInformation;

    // Constructor to initialize and setup PDFGeneration
    public PDFGeneration(String employeeName, String employeeNameDatabase, String companyNameRegular, String companyName, String yearComboBoxValue) {
        this.employeeName = employeeName;
        this.employeeNameDatabase = employeeNameDatabase;
        this.companyNameRegular = companyNameRegular;
        this.companyName = companyName;
        this.yearComboBoxValue = yearComboBoxValue;

        this.dbInformation = new DatabaseInformation();

        this.url = dbInformation.getUrl();
        this.username = dbInformation.getUsername();
        this.dbPassword = dbInformation.getPassword();

        this.companyID = getCompanyIdByName(companyNameRegular);
        this.tableNamePAP = buildTableNamePAP();
    }

    private String buildTableNamePAP() {
        String tableName = employeeNameDatabase + "_" + companyID + convertYearToDbNameID(yearComboBoxValue) + "_pap";
        System.out.println("Generated table name: " + tableName);
        return tableName;
    }

    // Converts year dropdown (e.g., "2025") to identifier like "twenty_twenty_five"
    public static String convertYearToDbNameID(String year) {
        switch (year) {
            case "2024":
                return "1";
            case "2025":
                return "2";
            default:
                throw new IllegalArgumentException("Invalid year selected: " + year);
        }
    }


    public String getFormattedPayPeriod(String paymentDateStr, String frequency) {
        try {
            // Parse end date
            LocalDate endDate = LocalDate.parse(paymentDateStr);  // e.g., 2025-04-15
            LocalDate startDate;
    
            // Use your provided frequency switch logic
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
                        startDate = endDate.withDayOfMonth(1);
                    } else {
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
                    throw new IllegalArgumentException("Unknown payment frequency: " + frequency);
            }
    
            // Format as dd/MMM/yy (e.g., 1/Apr/25)
            DateTimeFormatter rawFormatter = DateTimeFormatter.ofPattern("dd/MMM/yy", Locale.ENGLISH);

            String formattedStart = startDate.format(rawFormatter).replace(".", "");
            String formattedEnd = endDate.format(rawFormatter).replace(".", "");

    
            return formattedStart + " TO " + formattedEnd;
    
        } catch (Exception e) {
            return "Invalid Pay Period";
        }
    }


    private int getCompanyIdByName(String companyName) {
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
            return -1;
        }
    }

    public void generatePayslip(String outputPath) {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            Font calibriRegular = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font calibriBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bottomFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);


            ArrayList<ArrayList<String>> payrollInformationList = getAllPayrollRecords();

            // YTD Accumulators
            double ytdRegular = 0.0;
            double ytdVacation = 0.0;
            double ytdBonus = 0.0;
            double ytdIncomeTax = 0.0;
            double ytdCPP = 0.0;
            double ytdEI = 0.0;
            double ytdTotalEarnings = 0.0;
            double ytdTotalDeductions = 0.0;
            double ytdMealAllowance = 0.0;
            double ytdOvertimeEarnings = 0.0;
            double ytdCPP2 = 0.0;

            if (payrollInformationList.isEmpty()) {
                document.add(new Paragraph("No payroll records available.", calibriRegular));
                JOptionPane.showMessageDialog(null, "No payroll records found. Payslip not generated.");
                return; // exit early
            }

            for (int i = 0; i < payrollInformationList.size(); i++) {
                System.out.println(payrollInformationList.get(i));

                double hoursWorked     = Double.parseDouble(payrollInformationList.get(i).get(2));
                double ratePerHour     = Double.parseDouble(payrollInformationList.get(i).get(3));
                double regularEarning  = Double.parseDouble(payrollInformationList.get(i).get(4));
                double vacationPay     = Double.parseDouble(payrollInformationList.get(i).get(5));
                double incomeTax       = Double.parseDouble(payrollInformationList.get(i).get(8));
                double cpp             = Double.parseDouble(payrollInformationList.get(i).get(10));
                double ei              = Double.parseDouble(payrollInformationList.get(i).get(12));
                double bonus           = Double.parseDouble(payrollInformationList.get(i).get(23)); // or parse from payrollInformationList if dynamic
                //double 17 18
                double overtimeHours = Double.parseDouble(payrollInformationList.get(i).get(17));
                double overtimeRate = Double.parseDouble(payrollInformationList.get(i).get(18));
                double amountOvertime = BigDecimal.valueOf(overtimeHours * overtimeRate).setScale(2, RoundingMode.HALF_UP).doubleValue();;
                ytdOvertimeEarnings += amountOvertime;

                double mealAllowance = Double.parseDouble(payrollInformationList.get(i).get(22));
                ytdMealAllowance += mealAllowance;
                
                double cpp2 = Double.parseDouble(payrollInformationList.get(i).get(11));
                ytdCPP2 += cpp2;



                ytdRegular   += regularEarning;
                ytdVacation  += vacationPay;
                ytdIncomeTax += incomeTax;
                ytdCPP       += cpp;
                ytdEI        += ei;
                ytdBonus     += bonus;
                

                // Current Totals
                double totalEarnings   = regularEarning + vacationPay + bonus;
                double totalDeductions = incomeTax + cpp + ei;
                double netPay          = totalEarnings - totalDeductions;

                // YTD Totals
                double ytdEarnings     = ytdRegular + ytdVacation + ytdBonus + ytdMealAllowance + ytdOvertimeEarnings;
                double ytdDeductions   = ytdIncomeTax + ytdCPP + ytdEI + ytdCPP2;
                double ytdNetPay       = ytdEarnings - ytdDeductions;


                // === Header ===
                PdfPTable header = new PdfPTable(3);
                header.setWidthPercentage(100);
                header.setWidths(new float[]{1f, 3f, 1f});

                Image logo = Image.getInstance("images/insta_tax_services_logo_website_home_accountant_blue.png");
                logo.scaleAbsolute(120, 30);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setRowspan(2);
                logoCell.setVerticalAlignment(Element.ALIGN_TOP);
                header.addCell(logoCell);

                PdfPCell titleCell = new PdfPCell(new Phrase(employeeName + " Paystub", titleFont));

                titleCell.setBorder(Rectangle.NO_BORDER);
                titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                header.addCell(titleCell);

                header.addCell(new PdfPCell(new Phrase("")) {{ setBorder(Rectangle.NO_BORDER); }});

                document.add(header);
                document.add(Chunk.NEWLINE);

                // === Info Table ===
                PdfPTable infoTable = new PdfPTable(2);
                infoTable.setWidthPercentage(100);
                infoTable.setWidths(new float[]{1f, 1f});

                PdfPCell left = new PdfPCell();
                ArrayList<String> employeeData = getEmployeeInfo(employeeNameDatabase, companyID);
                ArrayList<String> employerData = getEmployerInfo(companyID);
            
                // LEFT CELL - Employee Information
                left.setBorder(Rectangle.BOX);
                left.addElement(new Paragraph(employeeData.get(0), calibriBold)); // Full Name
                left.addElement(new Paragraph(employeeData.get(1), calibriRegular)); // Address
                left.addElement(new Paragraph(employeeData.get(7) + " " + employeeData.get(3) + " " + employeeData.get(2), calibriRegular)); // City, Province, Postal
                left.addElement(Chunk.NEWLINE);
                left.addElement(new Phrase("EMPLOYEE NO.: ", calibriBold)); // index 8
                left.addElement(new Phrase(employeeData.get(8), calibriRegular));
                left.addElement(Chunk.NEWLINE);
                left.addElement(new Phrase("PAY PERIOD: ", calibriBold));
                // Add logic here to insert actual date range if available

                String paymentDateStr = payrollInformationList.get(i).get(1);  // pay_date (yyyy-MM-dd)
                String frequency = employeeData.get(4);  // e.g., "Bi-Weekly (26)"
                
                String payPeriod = getFormattedPayPeriod(paymentDateStr, frequency);
                left.addElement(new Phrase(payPeriod, calibriRegular));
                left.addElement(Chunk.NEWLINE);
                left.addElement(new Phrase("DATE OF PAYMENT: ", calibriBold));
                left.addElement(Chunk.NEWLINE);
                
                // RIGHT CELL - Employer Information
                PdfPCell right = new PdfPCell();
                right.setBorder(Rectangle.BOX);
                right.addElement(new Paragraph(employerData.get(0), calibriBold)); // Employer Name
                right.addElement(new Paragraph(employerData.get(3), calibriRegular)); // Address
                right.addElement(new Paragraph(employerData.get(1) + " " + employerData.get(2) + " " + employerData.get(4), calibriRegular)); // City, Province, Postal
                right.addElement(Chunk.NEWLINE);
                right.addElement(new Phrase("SIN: ", calibriBold));
                right.addElement(new Phrase("XXX-XXX-" + employeeData.get(6).substring(employeeData.get(6).length() - 3), calibriRegular)); // SIN
                right.addElement(Chunk.NEWLINE);
                right.addElement(new Phrase("DEPOSIT NO.: ", calibriBold));
                int slNo = (int) Double.parseDouble(payrollInformationList.get(i).get(0));
                right.addElement(new Phrase(String.valueOf(slNo), calibriRegular));
                
            

                infoTable.addCell(left);
                infoTable.addCell(right);
                document.add(infoTable);
                document.add(Chunk.NEWLINE);

                // === Earnings + Deductions ===
                PdfPTable table = new PdfPTable(8);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2f, 1f, 1f, 1.5f, 1.5f, 2f, 1.5f, 1.5f});


                // Headers
                addHeaderCell(table, "DESCRIPTION");
                addHeaderCell(table, "RATE");
                addHeaderCell(table, "HOURS");
                addHeaderCell(table, "CURRENT");
                addHeaderCell(table, "YTD");
                addHeaderCell(table, "DESCRIPTION");
                addHeaderCell(table, "CURRENT");
                addHeaderCell(table, "YTD");

                // REGULAR
                addDataCell(table, "REGULAR EARNINGS");
                addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(3)));
                addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(2)));
                addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(4)));
                addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdRegular)));  // ← assuming you're tracking YTD
                

                // USE CASE OVERTIME | NO MEAL ALLOWANCE, NO CPP2
                if ((amountOvertime != 0.0) && (mealAllowance == 0) && (cpp2 == 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    addDataCell(table, "OVERTIME EARNINGS");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(17)));
                    addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(18)));
                    addDataCell(table, formatCurrencyOrDash(amountOvertime + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdOvertimeEarnings)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");

                }


                if ((amountOvertime != 0.0) && (mealAllowance != 0) && (cpp2 == 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    addDataCell(table, "OVERTIME EARNINGS");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(17)));
                    addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(18)));
                    addDataCell(table, formatCurrencyOrDash(amountOvertime + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdOvertimeEarnings)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");

                    addDataCell(table, "MEAL ALLOWANCE");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdMealAllowance)));
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");

                }              

                if ((amountOvertime != 0.0) && (mealAllowance != 0) && (cpp2 != 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    addDataCell(table, "OVERTIME EARNINGS");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(17)));
                    addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(18)));
                    addDataCell(table, formatCurrencyOrDash(amountOvertime + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdOvertimeEarnings)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P 2");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(11)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP2)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));



                    addDataCell(table, "MEAL ALLOWANCE");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdMealAllowance)));
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");

                }

                if ((amountOvertime != 0.0) && (mealAllowance == 0) && (cpp2 != 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    addDataCell(table, "OVERTIME EARNINGS");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(17)));
                    addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(18)));
                    addDataCell(table, formatCurrencyOrDash(amountOvertime + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdOvertimeEarnings)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P 2");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(11)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP2)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));

                }                

                if ((amountOvertime == 0.0) && (mealAllowance == 0) && (cpp2 == 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));


                }      

                if ((amountOvertime == 0.0) && (mealAllowance != 0) && (cpp2 == 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));

                    addDataCell(table, "MEAL ALLOWANCE");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdMealAllowance)));
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");


                }    

                if ((amountOvertime == 0.0) && (mealAllowance == 0) && (cpp2 != 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    // C.P.P
                    addDataCell(table, "C.P.P 2");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(11)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP2)));
                    
                    // E.I
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));



                }   

                if ((amountOvertime != 0.0) && (mealAllowance == 0) && (cpp2 != 0)) {

                    addDataCell(table, "INCOME TAX");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(8)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdIncomeTax)));       

                    
                    addDataCell(table, "OVERTIME EARNINGS");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(17)));
                    addDataCell(table, formatNumberOrDash(payrollInformationList.get(i).get(18)));
                    addDataCell(table, formatCurrencyOrDash(amountOvertime + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdOvertimeEarnings)));
                    
                    // C.P.P
                    addDataCell(table, "C.P.P");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(10)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP)));


                    // VACATION
                    addDataCell(table, "VACATION PAY");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(5)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdVacation)));

                    // C.P.P
                    addDataCell(table, "C.P.P 2");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(11)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdCPP2)));                    

                    // BONUS
                    addDataCell(table, "BONUS");
                    addDataCell(table, "");
                    addDataCell(table, "");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(23) + ""));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdBonus)));

                    // E.I
                    addDataCell(table, "E.I");
                    addDataCell(table, formatCurrencyOrDash(payrollInformationList.get(i).get(12)));
                    addDataCell(table, formatCurrencyOrDash(String.valueOf(ytdEI)));


                }                   

                // TOTAL
                addDataCell(table, "TOTAL", true);
                addDataCell(table, "", true);
                addDataCell(table, "", true);
                addDataCell(table, formatCurrencyOrDash(totalEarnings+ ""), true);       // REGULAR + VACATION + BONUS
                addDataCell(table, formatCurrencyOrDash(ytdEarnings + ""), true);         // YTD
                addDataCell(table, "", true);
                addDataCell(table, formatCurrencyOrDash(totalDeductions + ""), true);     // INCOME TAX + CPP + EI
                addDataCell(table, formatCurrencyOrDash(ytdDeductions + ""), true);       // YTD

                // NET PAY
                addDataCell(table, "NET PAY", true);
                addDataCell(table, "", true);
                addDataCell(table, "", true);
                addDataCell(table, "", true);
                addDataCell(table, "", true);
                addDataCell(table, "", true);
                addDataCell(table, formatCurrencyOrDash(netPay + ""), true);              // TOTAL - DEDUCTIONS
                addDataCell(table, formatCurrencyOrDash(ytdNetPay + ""), true);           // YTD TOTAL - YTD DEDUCTIONS



                document.add(table);
                document.add(Chunk.NEWLINE);

                // === Footer ===
                PdfPTable footer = new PdfPTable(2);
                footer.setWidthPercentage(100);
                footer.setWidths(new float[]{1f, 1f});

                PdfPCell empInfo = new PdfPCell();
                empInfo.setBorder(Rectangle.BOX);
                empInfo.addElement(new Paragraph(employerData.get(0), calibriBold));
                empInfo.addElement(new Paragraph(employerData.get(3), calibriRegular));
                empInfo.addElement(new Paragraph(employerData.get(1), calibriRegular));
                empInfo.addElement(Chunk.NEWLINE);
                empInfo.addElement(new Paragraph(employeeData.get(0), calibriBold));
                empInfo.addElement(new Paragraph(employeeData.get(3), calibriRegular));
                empInfo.addElement(new Paragraph(employeeData.get(7) + " " + employeeData.get(3) + " " + employeeData.get(2), calibriRegular));

                PdfPCell bankInfo = new PdfPCell();
                bankInfo.setBorder(Rectangle.BOX);
                bankInfo.addElement(new Paragraph("BANK", small));
                bankInfo.addElement(new Paragraph("BRANCH", small));
                bankInfo.addElement(new Paragraph("CITY", small));
                bankInfo.addElement(Chunk.NEWLINE);
                bankInfo.addElement(new Paragraph("FINANCIAL INST NO.", small));
                bankInfo.addElement(new Paragraph("TRANSIT NO.", small));
                bankInfo.addElement(new Paragraph("ACCOUNT NO.", small));
                bankInfo.addElement(new Paragraph("AMOUNT " + formatCurrencyOrDash(netPay + ""), small));
                bankInfo.addElement(new Paragraph("DATE", small));

                footer.addCell(empInfo);
                footer.addCell(bankInfo);
                document.add(footer);

                // === Bottom Bar ===
                PdfPTable bottomBar = new PdfPTable(1);
                bottomBar.setWidthPercentage(100);

                bottomFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12); // Larger text

                PdfPCell bottomNotice = new PdfPCell(new Phrase("PAYROLL ADVICE ONLY - NON NEGOTIABLE", bottomFont));
                bottomNotice.setBackgroundColor(new BaseColor(230, 230, 230));
                bottomNotice.setHorizontalAlignment(Element.ALIGN_CENTER);
                bottomNotice.setVerticalAlignment(Element.ALIGN_MIDDLE);
                bottomNotice.setBorder(Rectangle.BOX);
                bottomNotice.setMinimumHeight(30); // Makes the cell visually taller

                bottomBar.addCell(bottomNotice);
                document.add(bottomBar);

                if (i != (payrollInformationList.size() - 1)) {
                    document.newPage();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private String formatCurrencyOrDash(String value) {
        double amount = Double.parseDouble(value);
        return amount == 0.0 ? "-" : "$ " + String.format("%.2f", amount);
    }
    
    private String formatNumberOrDash(String value) {
        double amount = Double.parseDouble(value);
        return amount == 0.0 ? "-" : String.format("%.2f", amount);
    }
    

    private void addHeaderCell(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text) {
        addDataCell(table, text, false);
    }

    private void addDataCell(PdfPTable table, String text, boolean highlight) {
        Font font = highlight
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)
                : FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        if (highlight) {
            cell.setBackgroundColor(new BaseColor(230, 230, 230));
        }
        table.addCell(cell);
    }

    public ArrayList<ArrayList<String>> getAllPayrollRecords() {
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        String tableName = buildTableNamePAP();
        String selectSQL = "SELECT * FROM " + tableName;

        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL)) {

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
                row.add(String.valueOf(resultSet.getDouble("amount_bonus")));
                data.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(data + "\n ALL INFORMATION DATA");
        return data;
    }

    public ArrayList<String> getEmployerInfo(int companyID) {
        ArrayList<String> employerInfoList = new ArrayList<>();
        
        String tableName = "EmplI_" + companyID;
    
        String selectDataSQL = "SELECT EmployerCompanyName, EmployerCity, EmployerProvince, EmployerAddress, EmployerPostalCode FROM " + tableName;
    
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
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return employerInfoList;
    }
    
    public ArrayList<String> getEmployeeInfo(String employeeTableName, int companyID) {
        ArrayList<String> employeeData = new ArrayList<>();
        String tableName = employeeTableName + "_" + companyID;
    
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
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return employeeData;
    }
    
    public ArrayList<Double> getNumericalValuesFromPayroll() {
        ArrayList<Double> numericalData = new ArrayList<>();
        
        int companyID = getCompanyIdByName(companyName);       // e.g., 7
        String yearID = convertYearToDbNameID(yearComboBoxValue);   // e.g., "25"
        String tableName = employeeNameDatabase + "_" + companyID + yearID + "_pap";
    
        String url = dbInformation.getUrl();
        String username = dbInformation.getUsername();
        String dbPassword = dbInformation.getPassword();
    
        String selectSQL = "SELECT * FROM " + tableName;
    
        try (Connection connection = DriverManager.getConnection(url, username, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {
    
            while (resultSet.next()) {
                numericalData.add(resultSet.getDouble("sl_no"));
                numericalData.add(resultSet.getDouble("hours_worked"));
                numericalData.add(resultSet.getDouble("rate_per_hour"));
                numericalData.add(resultSet.getDouble("basic_amount"));
                numericalData.add(resultSet.getDouble("vacation_pay"));
                numericalData.add(resultSet.getDouble("total_earnings"));
                numericalData.add(resultSet.getDouble("federal_tax"));
                numericalData.add(resultSet.getDouble("provincial_tax"));
                numericalData.add(resultSet.getDouble("income_tax"));
                numericalData.add(resultSet.getDouble("cpp"));
                numericalData.add(resultSet.getDouble("cpp2"));
                numericalData.add(resultSet.getDouble("ei"));
                numericalData.add(resultSet.getDouble("total_deductions"));
                numericalData.add(resultSet.getDouble("net_pay"));
                numericalData.add(resultSet.getDouble("cra_remittance"));
                numericalData.add(resultSet.getDouble("overtime_hours"));
                numericalData.add(resultSet.getDouble("overtime_rate"));
                numericalData.add(resultSet.getDouble("base_amount"));
                numericalData.add(resultSet.getDouble("meal_Allowance"));
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return numericalData;
    }
    

}

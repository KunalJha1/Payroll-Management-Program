package portal;

import javax.swing.*;
import java.awt.*;

public class PayrollDashboardPage extends JPanel {
    public GeneralStylingMethods stylingMethods = new GeneralStylingMethods();
    
    public PayrollDashboardPage(JPanel mainPanel, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Creates the yearly projection button which can be used to view the yearly layout of a clients payroll processing.
        JButton yearlyProjectionsButton = stylingMethods.createOptionButton("Yearly Projections", new ImageIcon("images\\yearly_projections_icon.png"));
        yearlyProjectionsButton.addActionListener(e -> cardLayout.show(mainPanel, "PayrollProjectionsYearLong"));
        mainPanel.add(new PayrollYearlyTablePage(mainPanel, cardLayout), "PayrollProjectionsYearLong");

        // Creates the employee information entry button which can be used to enter the information about the employee.
        JButton employeeInfoButton = stylingMethods.createOptionButton("Information Entry", new ImageIcon("images\\employee_info_icon.png"));
        employeeInfoButton.addActionListener(e -> cardLayout.show(mainPanel, "PayrollCalculationsPage"));
        mainPanel.add(new PayrollInformationPage(mainPanel, cardLayout), "PayrollCalculationsPage");

        // Company year-end-summary which was scrapped by the client, inclusion of code provides a chance to revist and further expandability inside of the future.

        // JButton yearlySummaryButton = createOptionButton("Yearly Summary", new ImageIcon("images\\yearly_summary_icon.png"));
        // yearlySummaryButton.setEnabled(false);  // Placeholder button

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(yearlyProjectionsButton, gbc);

        gbc.gridx = 0;
        add(employeeInfoButton, gbc);
    }
}

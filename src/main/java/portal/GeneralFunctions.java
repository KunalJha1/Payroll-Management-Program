package portal;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

public class GeneralFunctions {
    public String getProvinceCode(String provinceName) {
        switch (provinceName) {
            case "Alberta":
                return "AB";
            case "British Columbia":
                return "BC";
            case "Manitoba":
                return "MB";
            case "New Brunswick":
                return "NB";
            case "Newfoundland and Labrador":
                return "NL";
            case "Nova Scotia":
                return "NS";
            case "Ontario":
                return "ONT";
            case "Prince Edward Island":
                return "PE";
            case "Quebec":
                return "QC";
            case "Saskatchewan":
                return "SK";
            case "Northwest Territories":
                return "NT";
            case "Nunavut":
                return "NU";
            case "Yukon":
                return "YT";
            default:
                return "";
        }
    }

    // This is the method which is required to have the placeholdrs inside of the textfields for the GUI.
    public JTextField createPlaceHolderTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });

        return textField;
    }

}

# 🧾 Payroll Management Platform

A full-featured, CRA-compliant payroll processing system built in **Java (Swing)** with **MySQL** for storage and **iText** for PDF generation.  
Designed for accountants and small businesses, this platform automates earnings, deductions, tax calculations, pay periods, and year-end summaries — replacing manual spreadsheets and reducing payroll turnaround time significantly.

---

## 🚀 Key Features

###  Login System
![Login Page](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Login%20Page%20Payroll.png)

- User authentication with account creation  
- Clean UI for rapid onboarding  

---

### Employee & Employer Data Management
![Entry Section](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Entry%20Section%20Payroll.png)

- Manage employer details, employee information, SIN, and payroll preferences  
- Supports Weekly, Bi-Weekly, Semi-Monthly, Monthly pay frequencies  
- SQL-validated inputs stored securely  

---

### 📊 Dynamic Payroll Table & Auto-Calculations  
![Payroll Table](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Table%20Section%20Adjustment%20Payroll.png)

- Excel-style table for each pay period  
- Automatic calculation of:
  - **CPP, EI, Federal Tax, Alberta Provincial Tax**
  - Vacation pay, overtime, meal allowances  
  - Net pay, total deductions, CRA remittance  
- On-table recalculation & MySQL write-back  
- Batch-PDF generation and CSV export  

---

### 📁 Dashboard Navigation
![Dashboard Page](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Dashboard%20Page%20Payroll.png)

- Simple two-module workflow:  
  **Information Entry** → **Yearly Payroll Projections**

![Applications Page](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Applications%20Page%20Dashboard.png)

---

### 🧮 CRA-Compliant Tax Engine  
Accurate 2024-2025 calculations for all Canadian provinces:

- Federal & provincial withholding using proper **semi-monthly/bi-weekly/etc. exemptions**  
- Handles bonus, overtime, retroactive pay, allowances, TD1, lumpsum payments. 

---

### 📄 Professional PDF Payslips  
![Payslip](https://github.com/KunalJha1/Payroll-Management-Program/blob/main/images/Github_Example_Payslip.png)

- Auto-generated using **iText**  
- Company branding with logo  
- Clean, modern design  
- Includes YTD summary for all deductions (CPP, EI, Tax)  

---

## 🏢 Real-World Usage

This platform is **actively used by 30+ businesses**, processing payroll for **100+ employees** across multiple industries.  
It has replaced error-prone manual spreadsheets and now supports:

- Year-end reconciliation  
- CRA remittance tracking  
- Batch processing  
- Audit-ready reports  

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|----------|
| **Java (Swing)** | Core application + GUI |
| **MySQL** | Employee & payroll data storage |
| **JDBC** | Database connectivity |
| **iText PDF** | Generate payslips & reports |
| **Maven** | Dependency management |

---

## 📬 Contact

If you'd like to discuss this project, collaborate, or see the full system in action, feel free to reach out:

📧 **kunal.jha@uwaterloo.ca**

---

## ⚠️ Disclaimer
This system performs payroll calculations based on CRA guidelines, but final verification should always be done by a qualified accountant.  
This project is for educational and business use; the author assumes no liability for payroll decisions.


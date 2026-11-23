package model;

import util.InputValidator;

public class Employee extends Entity implements Contactable {
    private String department;
    private String role;
    private String gst;
    private String phone;
    private String bankAccount;

    public Employee(
            String id,
            String name,
            int riskScore,
            String department,
            String role,
            String gst,
            String phone,
            String bankAccount) {
        super(
                InputValidator.validateId(id, "EMP"),
                InputValidator.validateName(name),
                riskScore,
                EntityType.EMPLOYEE);
        this.department = InputValidator.validateName(department);
        this.role = InputValidator.validateName(role);
        this.gst = InputValidator.validateGst(gst, false);
        this.phone = InputValidator.validatePhone(phone);
        this.bankAccount = InputValidator.validateBankAccount(bankAccount);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = InputValidator.validateName(department);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = InputValidator.validateName(role);
    }

    @Override
    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = InputValidator.validateGst(gst, false);
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = InputValidator.validatePhone(phone);
    }

    @Override
    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = InputValidator.validateBankAccount(bankAccount);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("[EMPLOYEE] ").append(id).append(" - ").append(name).append("\n");
        sb.append("Dept: ").append(department).append(" | Role: ").append(role).append("\n");
        sb.append("Phone: ").append(phone).append(" | Bank: ").append(bankAccount).append("\n");
        sb.append("GST: ").append(gst != null ? gst : "N/A").append("\n");
        sb.append("Risk: ").append(riskScore).append(" (").append(getRiskTier()).append(")");
        return sb.toString();
    }
}
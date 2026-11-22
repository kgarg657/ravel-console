package model;

import exception.InvalidEntityException;
import util.InputValidator;

public class Vendor extends Entity implements Contactable {
    private String category;
    private String gst;
    private String phone;
    private String bankAccount;

    public Vendor(
            String id,
            String name,
            int riskScore,
            String category,
            String gst,
            String phone,
            String bankAccount) {
        super(
                InputValidator.validateId(id, "VEN"),
                InputValidator.validateName(name),
                riskScore,
                EntityType.VENDOR);
        this.category = InputValidator.validateName(category);
        this.gst = InputValidator.validateGst(gst, true);
        this.phone = InputValidator.validatePhone(phone);
        this.bankAccount = InputValidator.validateBankAccount(bankAccount);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = InputValidator.validateName(category);
    }

    @Override
    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = InputValidator.validateGst(gst, true);
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
        sb.append("[VENDOR] ").append(id).append(" - ").append(name).append("\n");
        sb.append("Category: ").append(category).append("\n");
        sb.append("GST: ").append(gst).append("\n");
        sb.append("Phone: ").append(phone).append(" | Bank: ").append(bankAccount).append("\n");
        sb.append("Risk: ").append(riskScore).append(" (").append(getRiskTier()).append(")");
        return sb.toString();
    }
}
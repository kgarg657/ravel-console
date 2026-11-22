package model;

import exception.InvalidEntityException;
import java.time.LocalDate;
import util.InputValidator;

public class Invoice extends Entity {
    private double amount;
    private String approverId;
    private String vendorId;
    private LocalDate invoiceDate;

    public Invoice(
            String id,
            String name,
            int riskScore,
            double amount,
            String approverId,
            String vendorId,
            LocalDate invoiceDate) {
        super(
                InputValidator.validateId(id, "INV"),
                InputValidator.validateName(name),
                riskScore,
                EntityType.INVOICE);
        this.amount = InputValidator.validateAmount(amount);
        this.approverId = InputValidator.validateId(approverId, "EMP");
        this.vendorId = InputValidator.validateId(vendorId, "VEN");
        if (invoiceDate == null) {
            throw new InvalidEntityException("Invoice date cannot be null.");
        }
        this.invoiceDate = invoiceDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = InputValidator.validateAmount(amount);
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = InputValidator.validateId(approverId, "EMP");
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = InputValidator.validateId(vendorId, "VEN");
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        if (invoiceDate == null) {
            throw new InvalidEntityException("Invoice date cannot be null.");
        }
        this.invoiceDate = invoiceDate;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("[INVOICE] ").append(id).append(" - ").append(name).append("\n");
        sb.append("Amount: ").append(String.format("%.2f", amount)).append(" | Date: ").append(invoiceDate).append("\n");
        sb.append("Approver: ").append(approverId).append(" | Vendor: ").append(vendorId).append("\n");
        sb.append("Risk: ").append(riskScore).append(" (").append(getRiskTier()).append(")");
        return sb.toString();
    }
}

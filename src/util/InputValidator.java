package util;

import exception.InvalidEntityException;

public class InputValidator {
    private static final String ID_PATTERN = "^(EMP|VEN|INV)-\\d{3,}$";
    private static final String GST_PATTERN = "^\\d{2}[A-Z]{5}\\d{4}[A-Z][1-9A-Z]Z[0-9A-Z]$";

    private InputValidator() {
    }

    public static String validateId(String id, String expectedPrefix) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidEntityException("ID cannot be blank.");
        }
        String cleaned = id.trim();
        if (!cleaned.matches(ID_PATTERN)) {
            throw new InvalidEntityException("Invalid ID format '" + cleaned + "'. Expected EMP-xxx, VEN-xxx, or INV-xxx.");
        }
        if (!cleaned.startsWith(expectedPrefix + "-")) {
            throw new InvalidEntityException("ID '" + cleaned + "' must start with '" + expectedPrefix + "-'.");
        }
        return cleaned;
    }

    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEntityException("Name cannot be blank.");
        }
        return name.trim();
    }

    public static String validateGst(String gst, boolean required) {
        if (gst == null || gst.trim().isEmpty()) {
            if (required) {
                throw new InvalidEntityException("GST number is required.");
            }
            return null;
        }
        String cleaned = gst.trim().toUpperCase();
        if (!cleaned.matches(GST_PATTERN)) {
            throw new InvalidEntityException("Invalid GST format: " + cleaned);
        }
        return cleaned;
    }

    public static String validatePhone(String phone) {
        if (phone == null) {
            throw new InvalidEntityException("Phone number cannot be blank.");
        }
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() != 10) {
            throw new InvalidEntityException("Phone number must be 10 digits after removing non-digits.");
        }
        return digitsOnly;
    }

    public static String validateBankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            throw new InvalidEntityException("Bank account cannot be blank.");
        }
        return bankAccount.trim();
    }

    public static double validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new InvalidEntityException("Amount must be greater than 0.");
        }
        return amount;
    }
}
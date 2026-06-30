package com.example.library.pattern.chain;

public class ValidationResult {
    private final boolean valid;
    private final String failureReason;
    private final String failedHandler;

    private ValidationResult(boolean valid, String failureReason, String failedHandler) {
        this.valid = valid;
        this.failureReason = failureReason;
        this.failedHandler = failedHandler;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult invalid(String reason, String handlerName) {
        return new ValidationResult(false, reason, handlerName);
    }

    public boolean isValid() { return valid; }
    public String getFailureReason() { return failureReason; }
    public String getFailedHandler() { return failedHandler; }

    @Override
    public String toString() {
        if (valid) return "ValidationResult{valid=true}";
        return "ValidationResult{valid=false, reason='" + failureReason + "', handler='" + failedHandler + "'}";
    }
}

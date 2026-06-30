package com.example.library.pattern.command;

/**
 * Encapsulates the outcome of a {@link LibraryCommand} execution or undo operation.
 *
 * <p>Use the static factory methods {@code success()} and {@code failure()} for
 * convenient construction rather than calling the constructor directly.</p>
 */
public class CommandResult {

    private final boolean success;
    private final String message;
    private final Long affectedEntityId;

    public CommandResult(boolean success, String message, Long affectedEntityId) {
        this.success = success;
        this.message = message;
        this.affectedEntityId = affectedEntityId;
    }

    public static CommandResult success(String message, Long affectedEntityId) {
        return new CommandResult(true, message, affectedEntityId);
    }

    public static CommandResult success(String message) {
        return new CommandResult(true, message, null);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, null);
    }

    public static CommandResult failure(String message, Long affectedEntityId) {
        return new CommandResult(false, message, affectedEntityId);
    }

    public boolean isSuccess()             { return success; }
    public String getMessage()             { return message; }
    public Long getAffectedEntityId()      { return affectedEntityId; }

    @Override
    public String toString() {
        return "CommandResult{success=" + success +
                ", message='" + message + '\'' +
                ", affectedEntityId=" + affectedEntityId + '}';
    }
}

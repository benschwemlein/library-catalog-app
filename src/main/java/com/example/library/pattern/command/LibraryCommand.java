package com.example.library.pattern.command;

/**
 * Command interface for all reversible library operations.
 *
 * <p>Implementations encapsulate a single business action (checkout, renewal, hold placement,
 * fine payment, copy transfer) along with the state needed to reverse it. Commands are managed
 * by {@link CommandHistory} and executed via {@link LibraryCommandService}.</p>
 */
public interface LibraryCommand {

    /**
     * Execute the command, performing the business operation.
     *
     * @return a {@link CommandResult} describing success or failure
     */
    CommandResult execute();

    /**
     * Undo the command, reversing the business operation.
     * Must restore the system to the state it was in before {@code execute()} was called.
     *
     * @return a {@link CommandResult} describing success or failure of the reversal
     */
    CommandResult undo();

    /**
     * Human-readable description of this command, used in audit logs and history views.
     *
     * @return short description string
     */
    String getDescription();
}

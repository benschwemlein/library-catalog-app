package com.example.library.pattern.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tracks the history of executed {@link LibraryCommand} instances, supporting undo/redo.
 *
 * <p>Two stacks are maintained:</p>
 * <ul>
 *   <li>{@code executeStack} — commands that have been executed and can be undone</li>
 *   <li>{@code undoStack} — commands that have been undone and can be re-executed</li>
 * </ul>
 *
 * <p>Executing a new command clears the undo stack (branching history is not supported).</p>
 *
 * <p>This component is request-scoped in spirit; applications that need HTTP-session-level
 * history should change the scope to "session" and add {@code @SessionScope}. The default
 * singleton scope is fine for service-layer integration tests.</p>
 */
@Component
@Slf4j
public class CommandHistory {

    private final Deque<LibraryCommand> executeStack = new ArrayDeque<>();
    private final Deque<LibraryCommand> undoStack    = new ArrayDeque<>();

    /**
     * Push a successfully executed command onto the execute stack and clear the undo stack.
     * (Any pending redo history is invalidated when a new command is recorded.)
     *
     * @param command the command that was just executed
     */
    public void push(LibraryCommand command) {
        executeStack.push(command);
        undoStack.clear();
        log.debug("CommandHistory: pushed '{}', executeStack size={}", command.getDescription(), executeStack.size());
    }

    /**
     * Return {@code true} if there is at least one command that can be undone.
     */
    public boolean canUndo() {
        return !executeStack.isEmpty();
    }

    /**
     * Return {@code true} if there is at least one undone command that can be re-executed.
     */
    public boolean canRedo() {
        return !undoStack.isEmpty();
    }

    /**
     * Peek at the next command that would be undone without removing it from the stack.
     *
     * @return the command, or {@code null} if the execute stack is empty
     */
    public LibraryCommand peekUndo() {
        return executeStack.peek();
    }

    /**
     * Peek at the next command that would be re-executed without removing it from the stack.
     *
     * @return the command, or {@code null} if the undo stack is empty
     */
    public LibraryCommand peekRedo() {
        return undoStack.peek();
    }

    /**
     * Pop the most recently executed command from the execute stack and push it onto
     * the undo stack. The caller is responsible for calling {@code command.undo()}.
     *
     * @return the command to undo
     * @throws IllegalStateException if there is nothing to undo
     */
    public LibraryCommand popForUndo() {
        if (executeStack.isEmpty()) {
            throw new IllegalStateException("Nothing to undo");
        }
        LibraryCommand command = executeStack.pop();
        undoStack.push(command);
        log.debug("CommandHistory: popped '{}' for undo, executeStack size={}", command.getDescription(), executeStack.size());
        return command;
    }

    /**
     * Pop the most recently undone command from the undo stack and push it back onto
     * the execute stack. The caller is responsible for calling {@code command.execute()}.
     *
     * @return the command to re-execute
     * @throws IllegalStateException if there is nothing to redo
     */
    public LibraryCommand popForRedo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("Nothing to redo");
        }
        LibraryCommand command = undoStack.pop();
        executeStack.push(command);
        log.debug("CommandHistory: popped '{}' for redo, undoStack size={}", command.getDescription(), undoStack.size());
        return command;
    }

    /**
     * Return descriptions of all commands in the execute stack, most-recent first.
     *
     * @return list of description strings; empty if no commands have been executed
     */
    public List<String> getHistory() {
        return executeStack.stream()
                .map(LibraryCommand::getDescription)
                .collect(Collectors.toList());
    }

    /**
     * Return the number of commands that can be undone.
     */
    public int undoDepth() {
        return executeStack.size();
    }

    /**
     * Return the number of commands that can be redone.
     */
    public int redoDepth() {
        return undoStack.size();
    }

    /**
     * Clear both stacks. Useful when starting a new session or after a hard reset.
     */
    public void clear() {
        executeStack.clear();
        undoStack.clear();
        log.info("CommandHistory cleared");
    }
}

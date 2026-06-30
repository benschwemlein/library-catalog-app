package com.example.library.pattern.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application-layer service for executing, undoing, and redoing {@link LibraryCommand} instances.
 *
 * <p>This service is the sole point of interaction for callers that want command-pattern
 * semantics. It delegates execution to the command itself and uses {@link CommandHistory}
 * to track what has happened so operations can be reversed or re-applied.</p>
 *
 * <p>Prototype-scoped command beans should be retrieved from the {@link ApplicationContext}
 * by callers (or via factory methods) so each operation gets a fresh instance. This service
 * does not construct commands itself -- it receives them ready to execute.</p>
 */
@Service
@Slf4j
public class LibraryCommandService {

    private final CommandHistory commandHistory;
    private final ApplicationContext applicationContext;

    @Autowired
    public LibraryCommandService(CommandHistory commandHistory, ApplicationContext applicationContext) {
        this.commandHistory = commandHistory;
        this.applicationContext = applicationContext;
    }

    /**
     * Execute the given command. If execution succeeds, the command is pushed onto the history
     * stack so it can be undone later.
     *
     * @param command the command to execute; must not be null
     * @return the result of the execution
     */
    public CommandResult executeCommand(LibraryCommand command) {
        log.info("Executing command: {}", command.getDescription());
        try {
            CommandResult result = command.execute();
            if (result.isSuccess()) {
                commandHistory.push(command);
                log.info("Command succeeded and pushed to history: {}", command.getDescription());
            } else {
                log.warn("Command failed (not pushed to history): {} -- {}",
                        command.getDescription(), result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Unexpected error executing command '{}': {}", command.getDescription(), e.getMessage(), e);
            return CommandResult.failure("Command execution error: " + e.getMessage());
        }
    }

    /**
     * Undo the most recently executed command.
     *
     * @return the result of the undo operation
     */
    public CommandResult undoLast() {
        if (!commandHistory.canUndo()) {
            log.warn("Undo requested but nothing to undo");
            return CommandResult.failure("Nothing to undo");
        }
        LibraryCommand command = commandHistory.popForUndo();
        log.info("Undoing command: {}", command.getDescription());
        try {
            CommandResult result = command.undo();
            if (result.isSuccess()) {
                log.info("Undo succeeded for command: {}", command.getDescription());
            } else {
                // Undo failed -- put the command back so the caller can retry or investigate
                commandHistory.push(command);
                log.error("Undo failed for command '{}': {}", command.getDescription(), result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Unexpected error undoing command '{}': {}", command.getDescription(), e.getMessage(), e);
            return CommandResult.failure("Undo error: " + e.getMessage());
        }
    }

    /**
     * Re-execute the most recently undone command.
     *
     * @return the result of the redo operation
     */
    public CommandResult redoLast() {
        if (!commandHistory.canRedo()) {
            log.warn("Redo requested but nothing to redo");
            return CommandResult.failure("Nothing to redo");
        }
        LibraryCommand command = commandHistory.popForRedo();
        log.info("Redoing command: {}", command.getDescription());
        try {
            CommandResult result = command.execute();
            if (result.isSuccess()) {
                log.info("Redo succeeded for command: {}", command.getDescription());
            } else {
                log.error("Redo failed for command '{}': {}", command.getDescription(), result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Unexpected error redoing command '{}': {}", command.getDescription(), e.getMessage(), e);
            return CommandResult.failure("Redo error: " + e.getMessage());
        }
    }

    /**
     * Return descriptions of all commands in the execute history, most-recent first.
     *
     * @return list of description strings; empty if no commands have been executed
     */
    public List<String> getHistory() {
        return commandHistory.getHistory();
    }

    /**
     * Return whether there is an operation available to undo.
     */
    public boolean canUndo() {
        return commandHistory.canUndo();
    }

    /**
     * Return whether there is an undone operation available to redo.
     */
    public boolean canRedo() {
        return commandHistory.canRedo();
    }

    /**
     * Retrieve a prototype-scoped command bean from the Spring context.
     * Convenience method for callers that want to build commands through the context
     * rather than wiring them directly.
     *
     * @param commandClass the command class to retrieve
     * @param <T>          the command type
     * @return a fresh prototype instance
     */
    public <T extends LibraryCommand> T createCommand(Class<T> commandClass) {
        return applicationContext.getBean(commandClass);
    }
}

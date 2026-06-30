package com.example.library.pattern;

import com.example.library.pattern.command.CommandHistory;
import com.example.library.pattern.command.CommandResult;
import com.example.library.pattern.command.LibraryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CommandHistoryTest {

    private CommandHistory commandHistory;

    @Mock
    private LibraryCommand command1;

    @Mock
    private LibraryCommand command2;

    @Mock
    private LibraryCommand command3;

    @BeforeEach
    void setUp() {
        commandHistory = new CommandHistory();
        when(command1.getDescription()).thenReturn("Checkout Book #1");
        when(command2.getDescription()).thenReturn("Place Hold #2");
        when(command3.getDescription()).thenReturn("Renew Loan #3");
    }

    @Test
    void push_newCommand_canUndo() {
        commandHistory.push(command1);

        assertThat(commandHistory.canUndo()).isTrue();
    }

    @Test
    void push_newCommand_cannotRedo() {
        // Before any push
        commandHistory.push(command1);
        commandHistory.popForUndo(); // now can redo
        commandHistory.push(command2); // new push clears redo

        assertThat(commandHistory.canRedo()).isFalse();
    }

    @Test
    void canUndo_emptyHistory_returnsFalse() {
        assertThat(commandHistory.canUndo()).isFalse();
    }

    @Test
    void canRedo_emptyUndoStack_returnsFalse() {
        assertThat(commandHistory.canRedo()).isFalse();
    }

    @Test
    void popForUndo_returnsCommand_movesToUndoStack() {
        commandHistory.push(command1);

        LibraryCommand popped = commandHistory.popForUndo();

        assertThat(popped).isEqualTo(command1);
        assertThat(commandHistory.canUndo()).isFalse();
        assertThat(commandHistory.canRedo()).isTrue();
    }

    @Test
    void popForRedo_returnsCommand_movesBackToExecuteStack() {
        commandHistory.push(command1);
        commandHistory.popForUndo();

        LibraryCommand redone = commandHistory.popForRedo();

        assertThat(redone).isEqualTo(command1);
        assertThat(commandHistory.canRedo()).isFalse();
        assertThat(commandHistory.canUndo()).isTrue();
    }

    @Test
    void popForUndo_emptyStack_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> commandHistory.popForUndo());
    }

    @Test
    void popForRedo_emptyStack_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> commandHistory.popForRedo());
    }

    @Test
    void clear_emptiesBothStacks() {
        commandHistory.push(command1);
        commandHistory.push(command2);
        commandHistory.popForUndo(); // move one to undo stack

        commandHistory.clear();

        assertThat(commandHistory.canUndo()).isFalse();
        assertThat(commandHistory.canRedo()).isFalse();
        assertThat(commandHistory.undoDepth()).isEqualTo(0);
    }

    @Test
    void getHistory_returnsDescriptionsInMostRecentFirst() {
        commandHistory.push(command1);
        commandHistory.push(command2);

        List<String> history = commandHistory.getHistory();

        assertThat(history).hasSize(2);
        // Stack is LIFO so most recent (command2) is first
        assertThat(history.get(0)).isEqualTo("Place Hold #2");
        assertThat(history.get(1)).isEqualTo("Checkout Book #1");
    }

    @Test
    void undoDepth_returnsCorrectCount() {
        commandHistory.push(command1);
        commandHistory.push(command2);
        commandHistory.push(command3);

        assertThat(commandHistory.undoDepth()).isEqualTo(3);

        commandHistory.popForUndo();
        assertThat(commandHistory.undoDepth()).isEqualTo(2);
    }

    @Test
    void push_afterUndo_clearsRedoStack() {
        commandHistory.push(command1);
        commandHistory.popForUndo(); // command1 goes to redo
        assertThat(commandHistory.canRedo()).isTrue();

        commandHistory.push(command2); // new command clears redo

        assertThat(commandHistory.canRedo()).isFalse();
    }

    @Test
    void canUndo_afterClear_returnsFalse() {
        commandHistory.push(command1);
        commandHistory.push(command2);

        commandHistory.clear();

        assertThat(commandHistory.canUndo()).isFalse();
    }

    @Test
    void redoDepth_afterMultipleUndos_returnsCorrectCount() {
        commandHistory.push(command1);
        commandHistory.push(command2);
        commandHistory.push(command3);

        commandHistory.popForUndo();
        commandHistory.popForUndo();

        assertThat(commandHistory.redoDepth()).isEqualTo(2);
        assertThat(commandHistory.undoDepth()).isEqualTo(1);
    }

    @Test
    void multipleUndoThenRedo_restoresCorrectOrder() {
        commandHistory.push(command1);
        commandHistory.push(command2);

        LibraryCommand firstUndo = commandHistory.popForUndo(); // returns command2
        assertThat(firstUndo).isEqualTo(command2);

        LibraryCommand redo = commandHistory.popForRedo(); // returns command2
        assertThat(redo).isEqualTo(command2);

        assertThat(commandHistory.undoDepth()).isEqualTo(2);
        assertThat(commandHistory.canRedo()).isFalse();
    }

    @Test
    void getHistory_emptyHistory_returnsEmptyList() {
        List<String> history = commandHistory.getHistory();

        assertThat(history).isEmpty();
    }
}

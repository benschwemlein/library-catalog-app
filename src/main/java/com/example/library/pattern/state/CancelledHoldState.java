package com.example.library.pattern.state;

import com.example.library.entity.HoldStatus;
import org.springframework.stereotype.Component;

@Component
public class CancelledHoldState implements HoldState {

    private static final String TERMINAL_MESSAGE = "Hold is already cancelled - terminal state";

    @Override public void activate(HoldContext context) { throw new IllegalStateException(TERMINAL_MESSAGE); }
    @Override public void notify(HoldContext context) { throw new IllegalStateException(TERMINAL_MESSAGE); }
    @Override public void fulfill(HoldContext context) { throw new IllegalStateException(TERMINAL_MESSAGE); }
    @Override public void cancel(HoldContext context) { throw new IllegalStateException(TERMINAL_MESSAGE); }
    @Override public void expire(HoldContext context) { throw new IllegalStateException(TERMINAL_MESSAGE); }
    @Override public HoldStatus getStatus() { return HoldStatus.CANCELLED; }
}

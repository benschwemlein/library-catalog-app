package com.example.library.pattern.state;

import com.example.library.entity.Hold;

public class HoldContext {

    private final Hold hold;
    private HoldState currentState;

    public HoldContext(Hold hold, HoldState initialState) {
        this.hold = hold;
        this.currentState = initialState;
        hold.setStatus(initialState.getStatus());
    }

    public void setState(HoldState state) {
        this.currentState = state;
        hold.setStatus(state.getStatus());
    }

    public HoldState getState() { return currentState; }
    public Hold getHold() { return hold; }

    public void activate() { currentState.activate(this); }
    public void notifyMember() { currentState.notify(this); }
    public void fulfill() { currentState.fulfill(this); }
    public void cancel() { currentState.cancel(this); }
    public void expire() { currentState.expire(this); }
}

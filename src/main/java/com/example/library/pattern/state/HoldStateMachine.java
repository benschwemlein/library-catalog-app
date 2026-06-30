package com.example.library.pattern.state;

import com.example.library.entity.Hold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HoldStateMachine {

    @Autowired
    private PendingHoldState pendingHoldState;

    @Autowired
    private ReadyHoldState readyHoldState;

    @Autowired
    private FulfilledHoldState fulfilledHoldState;

    @Autowired
    private CancelledHoldState cancelledHoldState;

    @Autowired
    private ExpiredHoldState expiredHoldState;

    public HoldContext createContext(Hold hold) {
        if (hold.getStatus() == null) {
            throw new IllegalArgumentException("Unknown hold status: null");
        }
        switch (hold.getStatus()) {
            case PENDING:
                return new HoldContext(hold, pendingHoldState);
            case READY:
                return new HoldContext(hold, readyHoldState);
            case FULFILLED:
                return new HoldContext(hold, fulfilledHoldState);
            case CANCELLED:
                return new HoldContext(hold, cancelledHoldState);
            case EXPIRED:
                return new HoldContext(hold, expiredHoldState);
            default:
                throw new IllegalArgumentException("Unknown hold status: " + hold.getStatus());
        }
    }
}

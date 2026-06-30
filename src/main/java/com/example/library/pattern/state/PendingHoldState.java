package com.example.library.pattern.state;

import com.example.library.entity.HoldStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class PendingHoldState implements HoldState {

    @Autowired
    @Lazy
    private ReadyHoldState readyHoldState;

    @Autowired
    @Lazy
    private CancelledHoldState cancelledHoldState;

    @Override
    public void activate(HoldContext context) {
        context.getHold().setNotifiedDate(LocalDateTime.now());
        context.setState(readyHoldState);
        log.info("Hold {} activated and is now ready for pickup", context.getHold().getId());
    }

    @Override
    public void notify(HoldContext context) {
        throw new IllegalStateException("Cannot notify - hold must be activated/ready first");
    }

    @Override
    public void fulfill(HoldContext context) {
        throw new IllegalStateException("Cannot fulfill pending hold - hold must be ready first");
    }

    @Override
    public void cancel(HoldContext context) {
        context.setState(cancelledHoldState);
        log.info("Hold {} cancelled", context.getHold().getId());
    }

    @Override
    public void expire(HoldContext context) {
        throw new IllegalStateException("Cannot expire a pending hold directly - activate first");
    }

    @Override
    public HoldStatus getStatus() {
        return HoldStatus.PENDING;
    }
}

package com.example.library.pattern.state;

import com.example.library.entity.HoldStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ReadyHoldState implements HoldState {

    @Autowired
    @Lazy
    private FulfilledHoldState fulfilledHoldState;

    @Autowired
    @Lazy
    private CancelledHoldState cancelledHoldState;

    @Autowired
    @Lazy
    private ExpiredHoldState expiredHoldState;

    @Override
    public void activate(HoldContext context) {
        log.warn("Hold {} is already in READY state", context.getHold().getId());
    }

    @Override
    public void notify(HoldContext context) {
        context.getHold().setNotifiedDate(LocalDateTime.now());
        log.info("Member notified for hold {}", context.getHold().getId());
    }

    @Override
    public void fulfill(HoldContext context) {
        context.setState(fulfilledHoldState);
        log.info("Hold {} fulfilled", context.getHold().getId());
    }

    @Override
    public void cancel(HoldContext context) {
        context.setState(cancelledHoldState);
        log.info("Hold {} cancelled", context.getHold().getId());
    }

    @Override
    public void expire(HoldContext context) {
        context.setState(expiredHoldState);
        log.info("Hold {} expired", context.getHold().getId());
    }

    @Override
    public HoldStatus getStatus() {
        return HoldStatus.READY;
    }
}

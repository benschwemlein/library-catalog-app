package com.example.library.pattern.state;

import com.example.library.entity.HoldStatus;

public interface HoldState {
    void activate(HoldContext context);
    void notify(HoldContext context);
    void fulfill(HoldContext context);
    void cancel(HoldContext context);
    void expire(HoldContext context);
    HoldStatus getStatus();
}

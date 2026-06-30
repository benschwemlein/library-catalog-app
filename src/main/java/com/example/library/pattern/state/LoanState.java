package com.example.library.pattern.state;

import com.example.library.entity.LoanStatus;

public interface LoanState {
    void checkout(LoanContext context);
    void returnBook(LoanContext context);
    void renew(LoanContext context);
    void markOverdue(LoanContext context);
    void markLost(LoanContext context);
    LoanStatus getStatus();
}

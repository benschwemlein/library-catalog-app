package com.example.library.circulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EligibilityResult {

    private final boolean eligible;
    private final List<String> reasons;

    private EligibilityResult(boolean eligible, List<String> reasons) {
        this.eligible = eligible;
        this.reasons = Collections.unmodifiableList(reasons);
    }

    public static EligibilityResult eligible() {
        return new EligibilityResult(true, Collections.emptyList());
    }

    public static EligibilityResult ineligible(String... reasons) {
        return new EligibilityResult(false, Arrays.asList(reasons));
    }

    public static EligibilityResult ineligible(List<String> reasons) {
        return new EligibilityResult(false, List.copyOf(reasons));
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    @Override
    public String toString() {
        return "EligibilityResult{eligible=" + eligible + ", reasons=" + reasons + "}";
    }
}

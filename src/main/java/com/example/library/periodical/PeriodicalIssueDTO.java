package com.example.library.periodical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicalIssueDTO {

    private Long id;
    private Long periodicalId;
    private String periodicalTitle;
    private int volume;
    private int issueNumber;
    private LocalDate publicationDate;
    private PeriodicalIssueStatus status;
    private String condition;
    private String location;
    private LocalDate receivedDate;
    private String notes;
}

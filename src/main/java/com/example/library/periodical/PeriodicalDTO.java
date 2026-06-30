package com.example.library.periodical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicalDTO {

    private Long id;
    private String title;
    private String issn;
    private String publisher;
    private PeriodicalFrequency frequency;
    private String category;
    private String description;
    private boolean active;
    private boolean digitalAccess;
    private String digitalUrl;
    private Long branchId;
    private String branchName;
    private int issueCount;
}

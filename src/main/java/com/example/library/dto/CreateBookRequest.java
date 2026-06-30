package com.example.library.dto;

import com.example.library.annotation.ValidIsbn;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @NotBlank
    @ValidIsbn
    private String isbn;

    @NotBlank
    private String title;

    private String subtitle;
    private String description;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String coverImageUrl;
    private List<Long> authorIds;
    private Long publisherId;
    private List<Long> genreIds;
    private List<Long> subjectIds;
}

package org.is.bandmanager.repository.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Getter
@Setter
@Builder
public class BestBandAwardFilter implements EntityFilter {

    private MusicGenre genre;

    private String bandName;

    private Long bandId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAtAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAtBefore;

}
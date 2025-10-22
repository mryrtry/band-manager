package org.is.bandmanager.repository.filter;

import lombok.Data;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class BestBandAwardFilter implements EntityFilter {

    private MusicGenre genre;

    private String bandName;

    private Long bandId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAtAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAtBefore;

    private String bandNameContains;

}
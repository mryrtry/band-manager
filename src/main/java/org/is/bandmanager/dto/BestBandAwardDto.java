package org.is.bandmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.MusicGenre;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestBandAwardDto {

    private Long id;

    private MusicBand band;

    private MusicGenre genre;

    private LocalDateTime createdAt;

}

package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.is.bandmanager.model.MusicGenre;


@Builder
@Data
public class BestBandAwardRequest {

    @NotNull(message = "BestBandAward.MusicBandId не может быть пустым")
    private Integer musicBandId;

    @NotNull(message = "BestBandAward.MusicGenre не может быть пустым")
    private MusicGenre genre;

}

package org.is.bandmanager.dto.importRequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.model.Person;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicBandImportRequest {

    @NotBlank(message = "MusicBand.Name не может быть пустым")
    private String name;

    @NotNull(message = "MusicBand.Coordinates не может быть пустым")
    private CoordinatesImportRequest coordinates;

    @NotNull(message = "MusicBand.MusicGenre не может быть пустым")
    private MusicGenre genre;

    @NotNull(message = "MusicBand.NumberOfParticipants не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
    private Long numberOfParticipants;

    @NotNull(message = "MusicBand.SinglesCount не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
    private Long singlesCount;

    @NotBlank(message = "MusicBand.Description не может быть пустым")
    private String description;

    @NotNull(message = "MusicBand.BestAlbum не может быть пустым")
    private AlbumImportRequest bestAlbum;

    @NotNull(message = "MusicBand.AlbumsCount не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
    private Long albumsCount;

    @NotNull(message = "MusicBand.EstablishmentDate не может быть пустым")
    private Date establishmentDate;

    @NotNull(message = "MusicBand.FrontMan не может быть пустым")
    private PersonImportRequest frontMan;

}

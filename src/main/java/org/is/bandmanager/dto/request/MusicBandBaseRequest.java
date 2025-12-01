package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.is.bandmanager.model.MusicGenre;

import java.util.Date;


@Data
@SuperBuilder
@Jacksonized
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MusicBandBaseRequest {

	@NotNull(message = "MusicBand.CoordinatesId не может быть пустым")
	private Long coordinatesId;

	@NotNull(message = "MusicBand.MusicGenre не может быть пустым")
	private MusicGenre genre;

	@NotNull(message = "MusicBand.NumberOfParticipants не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
	private Long numberOfParticipants;

	@NotNull(message = "MusicBand.SinglesCount не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.SinglesCount должно быть > 0")
	private Long singlesCount;

	@NotBlank(message = "MusicBand.Description не может быть пустым")
	private String description;

	@NotNull(message = "MusicBand.BestAlbumId не может быть пустым")
	private Long bestAlbumId;

	@NotNull(message = "MusicBand.AlbumsCount не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
	private Long albumsCount;

	@NotNull(message = "MusicBand.EstablishmentDate не может быть пустым")
	private Date establishmentDate;

	@NotNull(message = "MusicBand.FrontManId не может быть пустым")
	private Long frontManId;

}

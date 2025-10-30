package org.is.bandmanager.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.is.bandmanager.model.MusicGenre;

import java.util.Date;


@Builder
@Data
public class MusicBandRequest {

	@NotBlank(message = "MusicBand.Name не может быть пустым")
	@Column(nullable = false)
	private String name;

	@Valid
	@NotNull(message = "MusicBand.CoordinatesId не может быть пустым")
	private Long coordinatesId;

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

	@Valid
	@NotNull(message = "MusicBand.BestAlbumId не может быть пустым")
	private Long bestAlbumId;

	@NotNull(message = "MusicBand.AlbumsCount не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
	private Long albumsCount;

	@NotNull(message = "MusicBand.EstablishmentDate не может быть пустым")
	private Date establishmentDate;

	@Valid
	@NotNull(message = "MusicBand.FrontManId не может быть пустым")
	private Long frontManId;

}

package org.is.bandmanager.repository.filter;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class MusicBandFilter implements EntityFilter {

	private String name;

	private String description;

	private MusicGenre genre;

	private String frontManName;

	private String bestAlbumName;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
	private Long minParticipants;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
	private Long maxParticipants;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
	private Long minSingles;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
	private Long maxSingles;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
	private Long minAlbumsCount;

	@DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
	private Long maxAlbumsCount;

	@DecimalMin(value = "147", inclusive = false, message = "Coordinates.X должно быть > 157")
	private Integer minCoordinateX;

	@DecimalMin(value = "147", inclusive = false, message = "Coordinates.X должно быть > 147")
	private Integer maxCoordinateX;

	private Float minCoordinateY;

	private Float maxCoordinateY;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date establishmentDateBefore;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date establishmentDateAfter;

}
package org.is.bandmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.is.bandmanager.model.MusicGenre;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicBandDto {

	private Integer id;

	private String name;

	private CoordinatesDto coordinates;

	private MusicGenre genre;

	private Long numberOfParticipants;

	private Long singlesCount;

	private String description;

	private AlbumDto bestAlbum;

	private Long albumsCount;

	private Date establishmentDate;

	private PersonDto frontMan;

	private Date creationDate;

}
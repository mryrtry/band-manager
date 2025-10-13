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

    private Long coordinatesId;

    private MusicGenre genre;

    private Long numberOfParticipants;

    private Long singlesCount;

    private String description;

    private Long bestAlbumId;

    private Long albumsCount;

    private Date establishmentDate;

    private Long frontManId;

    private Date creationDate;

}
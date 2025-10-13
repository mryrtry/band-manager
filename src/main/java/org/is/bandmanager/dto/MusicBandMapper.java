package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.service.AlbumService;
import org.is.bandmanager.service.CoordinatesService;
import org.is.bandmanager.service.PersonService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class MusicBandMapper {

    @Autowired
    CoordinatesService coordinatesService;

    @Autowired
    AlbumService albumService;

    @Autowired
    PersonService personService;

    @Mapping(target = "coordinatesId", source = "coordinates.id")
    @Mapping(target = "bestAlbumId", source = "bestAlbum.id")
    @Mapping(target = "frontManId", source = "frontMan.id")
    public abstract MusicBandDto toDto(MusicBand musicBand);

    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "frontMan", ignore = true)
    public abstract MusicBand toEntity(MusicBandDto musicBandDto);

    public MusicBand toEntity(MusicBandRequest request) {
        return MusicBand.builder()
                .name(request.getName())
                .coordinates(coordinatesService.getEntity(request.getCoordinatesId()))
                .genre(request.getGenre())
                .numberOfParticipants(request.getNumberOfParticipants())
                .singlesCount(request.getSinglesCount())
                .description(request.getDescription())
                .bestAlbum(albumService.getEntity(request.getBestAlbumId()))
                .albumsCount(request.getAlbumsCount())
                .establishmentDate(request.getEstablishmentDate())
                .frontMan(personService.getEntity(request.getFrontManId()))
                .creationDate(new Date())
                .build();
    }

}
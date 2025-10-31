package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.MusicBand;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {AlbumMapper.class, PersonMapper.class, CoordinatesMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MusicBandMapper {

    @Mapping(target = "coordinates", source = "coordinates")
    @Mapping(target = "bestAlbum", source = "bestAlbum")
    @Mapping(target = "frontMan", source = "frontMan")
    MusicBandDto toDto(MusicBand musicBand);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "frontMan", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    MusicBand toEntity(MusicBandRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "frontMan", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    void updateEntityFromRequest(MusicBandRequest request, @MappingTarget MusicBand entity);

}
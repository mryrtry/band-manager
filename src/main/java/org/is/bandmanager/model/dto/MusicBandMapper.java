package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.MusicBand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        uses = {CoordinatesMapper.class, AlbumMapper.class, PersonMapper.class})
public interface MusicBandMapper {

    MusicBandMapper INSTANCE = Mappers.getMapper(MusicBandMapper.class);

    @Mapping(target = "coordinates", source = "coordinates")
    @Mapping(target = "bestAlbum", source = "bestAlbum")
    @Mapping(target = "frontMan", source = "frontMan")
    MusicBandDto toDto(MusicBand musicBand);

    @Mapping(target = "coordinates", source = "coordinates")
    @Mapping(target = "bestAlbum", source = "bestAlbum")
    @Mapping(target = "frontMan", source = "frontMan")
    MusicBand toEntity(MusicBandDto musicBandDto);

}
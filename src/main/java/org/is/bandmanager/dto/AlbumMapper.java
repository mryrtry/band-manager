package org.is.bandmanager.dto;

import org.is.bandmanager.model.Album;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumMapper INSTANCE = Mappers.getMapper(AlbumMapper.class);

    AlbumDto toDto(Album album);

    Album toEntity(AlbumDto albumDto);

}
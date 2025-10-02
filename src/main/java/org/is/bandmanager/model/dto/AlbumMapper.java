package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Album;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDto toDto(Album album);

    Album toEntity(AlbumDto albumDto);

}
package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDto toDto(Album album);

    @Mapping(target = "id", ignore = true)
    Album toEntity(AlbumRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntityFromRequest(AlbumRequest request, @MappingTarget Album entity);

}
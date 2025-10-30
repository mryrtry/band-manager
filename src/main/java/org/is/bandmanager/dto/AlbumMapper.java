package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDto toDto(Album album);

    @Mapping(target = "id", ignore = true)
    Album toEntity(AlbumRequest request);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AlbumRequest request, @MappingTarget Album entity);

}
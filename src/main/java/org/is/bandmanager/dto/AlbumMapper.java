package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDto toDto(Album album);

    @Mapping(target = "id", ignore = true)
    Album toEntity(AlbumRequest request);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AlbumRequest request, @MappingTarget Album entity);

}
package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesMapper INSTANCE = Mappers.getMapper(CoordinatesMapper.class);

    CoordinatesDto toDto(Coordinates coordinates);

    Coordinates toEntity(CoordinatesDto coordinatesDto);

}
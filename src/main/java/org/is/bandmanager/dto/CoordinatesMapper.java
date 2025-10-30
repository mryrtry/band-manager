package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

	CoordinatesDto toDto(Coordinates coordinates);

	@Mapping(target = "id", ignore = true)
	Coordinates toEntity(CoordinatesRequest request);

	@Mapping(target = "id", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntityFromRequest(CoordinatesRequest request, @MappingTarget Coordinates entity);

}
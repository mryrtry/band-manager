package org.is.bandmanager.dto.mapper;

import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Person;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = LocationMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PersonMapper {

    @Mapping(target = "location", source = "location")
    PersonDto toDto(Person person);

    @Mapping(target = "location", source = "location")
    Person toEntity(PersonDto personDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    Person toEntity(PersonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonRequest request, @MappingTarget Person entity);

}
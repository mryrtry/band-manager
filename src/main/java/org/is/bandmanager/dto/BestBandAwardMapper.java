package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.service.MusicBandService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BestBandAwardMapper {

    @Autowired
    private MusicBandService musicBandService;

    @Mapping(target = "band", source = "band")
    public abstract BestBandAwardDto toDto(BestBandAward award);

    @Mapping(target = "band", source = "band")
    public abstract BestBandAward toEntity(BestBandAwardDto awardDto);

    @Mapping(target = "band", ignore = true)
    public BestBandAward toEntity(BestBandAwardRequest request) {
        return BestBandAward.builder()
                .band(musicBandService.getEntity(request.getMusicBandId()))
                .genre(request.getGenre())
                .build();
    }

}
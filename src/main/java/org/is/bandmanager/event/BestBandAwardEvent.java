package org.is.bandmanager.event;

import lombok.Getter;
import org.is.bandmanager.dto.BestBandAwardDto;

import java.time.Instant;
import java.util.List;

@Getter
public class BestBandAwardEvent {

    private final EventType eventType;

    private final List<BestBandAwardDto> bestBandAwards;

    private final Instant timestamp;

    public BestBandAwardEvent(EventType eventType, BestBandAwardDto musicBand) {
        this.eventType = eventType;
        this.bestBandAwards = List.of(musicBand);
        this.timestamp = Instant.now();
    }

    public BestBandAwardEvent(EventType eventType, List<BestBandAwardDto> musicBands) {
        this.eventType = eventType;
        this.bestBandAwards = musicBands;
        this.timestamp = Instant.now();
    }

}

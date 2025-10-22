package org.is.bandmanager.event;

import lombok.Getter;
import org.is.bandmanager.dto.MusicBandDto;

import java.time.Instant;
import java.util.List;

@Getter
public class MusicBandEvent {

    private final EventType eventType;

    private final List<MusicBandDto> musicBands;

    private final Instant timestamp;

    public MusicBandEvent(EventType eventType, MusicBandDto musicBand) {
        this.eventType = eventType;
        this.musicBands = List.of(musicBand);
        this.timestamp = Instant.now();
    }

    public MusicBandEvent(EventType eventType, List<MusicBandDto> musicBands) {
        this.eventType = eventType;
        this.musicBands = musicBands;
        this.timestamp = Instant.now();
    }

}

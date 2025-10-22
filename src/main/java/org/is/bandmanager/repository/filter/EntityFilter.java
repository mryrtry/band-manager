package org.is.bandmanager.repository.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MusicBandFilter.class, name = "musicBand"),
        @JsonSubTypes.Type(value = BestBandAwardFilter.class, name = "bestBandAward")
})
public interface EntityFilter {
}

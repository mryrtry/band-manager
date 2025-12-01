package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.is.bandmanager.validation.UniqueMusicBandName;


@Data
@Getter
@Setter
@Jacksonized
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MusicBandUpdateRequest extends MusicBandBaseRequest {

	@NotBlank(message = "MusicBand.Name не может быть пустым")
	@UniqueMusicBandName
	private String name;

	private Long version;

}

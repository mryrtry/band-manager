package org.is.bandmanager.dto.importRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationImportRequest {

	private Integer x;

	@NotNull(message = "Location.Y не может быть пустым")
	private Long y;

	@NotNull(message = "Location.Z не может быть пустым")
	private Long z;

}

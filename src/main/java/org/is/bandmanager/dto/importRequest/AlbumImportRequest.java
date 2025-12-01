package org.is.bandmanager.dto.importRequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class AlbumImportRequest {

	@NotBlank(message = "Album.Name не может быть пустым")
	private String name;

	@NotNull(message = "Album.Tracks не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "Album.Tracks должно быть > 0")
	private Long tracks;

	@DecimalMin(value = "0", inclusive = false, message = "Album.Sales должно быть > 0")
	private Integer sales;

}

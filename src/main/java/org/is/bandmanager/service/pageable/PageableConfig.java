package org.is.bandmanager.service.pageable;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableConfig {

    private int page = 0;

    private int size = 10;

    private List<String> sort = List.of("id");

    private String direction = "ASC";

}

package org.is.bandmanager.util.pageable;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.is.bandmanager.constants.PageableConstants;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableConfig {

    private int page = PageableConstants.DEFAULT_PAGE;

    private int size = PageableConstants.DEFAULT_PAGE_SIZE;

    private List<String> sort = PageableConstants.DEFAULT_SORT_FIELDS;

    private String direction = PageableConstants.DEFAULT_SORT_ORDER;

}

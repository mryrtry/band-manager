package org.is.bandmanager.util.pageable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.constants.PageableConstants;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageableConfig {

    private int page = PageableConstants.DEFAULT_PAGE;

    private int size = PageableConstants.DEFAULT_PAGE_SIZE;

    private List<String> sort = PageableConstants.DEFAULT_SORT_FIELDS;

    private String direction = PageableConstants.DEFAULT_SORT_ORDER;

}

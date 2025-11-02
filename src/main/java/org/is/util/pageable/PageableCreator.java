package org.is.util.pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageableCreator {

    public static Pageable create(PageableConfig config, PageableType type) {
        int page = Math.max(config.getPage(), 0);
        int size = config.getSize();

        List<String> sortFields = (config.getSort() == null || config.getSort().isEmpty())
                ? List.of(type.getDefaultField())
                : config.getSort().stream()
                .filter(type.getAllowedFields()::contains)
                .toList();

        if (sortFields.isEmpty()) {
            sortFields = List.of(type.getDefaultField());
        }

        Sort.Direction direction = parseDirection(config.getDirection());
        Sort sort = Sort.by(direction, sortFields.toArray(String[]::new));

        return PageRequest.of(page, size, sort);
    }

    private static Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.ASC;
        }
    }

}
package com.ead.course.services.impl;

import com.ead.course.services.UtilsService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilsServiceImpl implements UtilsService {

    private final String REQUEST_URI = "http://localhost:8087";

    @Override
    public String createUrl(UUID courseId, Pageable pageable) {
        return REQUEST_URI + "/api/v1/users?courseId=" + courseId + "&page=" + pageable.getPageNumber() + "&size=" +
                pageable.getPageSize() + "&sort=" + pageable.getSort().toString().replaceAll(": ", ",");
    }
}

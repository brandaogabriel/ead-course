package com.ead.course.clients;

import com.ead.course.dtos.ResponsePageDto;
import com.ead.course.dtos.UserDto;
import com.ead.course.services.UtilsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Log4j2
@Component
public class AuthUserClient {

    @Value("${ead.api.url.authuser}")
    private String REQUEST_URL_AUTHUSER;

    private final UtilsService utilsService;
    private final RestTemplate restTemplate;

    public AuthUserClient(UtilsService utilsService, RestTemplate restTemplate) {
        this.utilsService = utilsService;
        this.restTemplate = restTemplate;
    }

    public Page<UserDto> getAllUsersByCourse(UUID courseId, Pageable pageable) {
        List<UserDto> fetchResult = null;
        ResponseEntity<ResponsePageDto<UserDto>> result = null;
        String url = REQUEST_URL_AUTHUSER + utilsService.createUrlGetAllUsersByCourse(courseId, pageable);

        log.debug("Request URL: {}", url);
        log.info("Request URL: {}", url);
        try {
            ParameterizedTypeReference<ResponsePageDto<UserDto>> responseType = new ParameterizedTypeReference<>(){};
            result = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            fetchResult = result.getBody().getContent();
            log.debug("Response Number of elements: {}", fetchResult.size());
        } catch (HttpStatusCodeException e) {
            log.error("Error request /api/v1/users {}", e);
        }
        log.info("Ending request /api/v1/users courseId {}", courseId);
        return result.getBody();
    }

    public ResponseEntity<UserDto> getOneUserById(UUID userId) {
        String url = REQUEST_URL_AUTHUSER + "/api/v1/users/" + userId;
        return restTemplate.exchange(url, HttpMethod.GET, null, UserDto.class);
    }
}

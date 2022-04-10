package com.ead.course.controllers;

import com.ead.course.clients.AuthUserClient;
import com.ead.course.dtos.SubscriptionDto;
import com.ead.course.dtos.UserDto;
import com.ead.course.enums.UserStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.CourseUserModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.CourseUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseUserController {

    private final CourseService courseService;
    private final CourseUserService courseUserService;
    private final AuthUserClient authUserClient;

    public CourseUserController(CourseService courseService, CourseUserService courseUserService, AuthUserClient authUserClient) {
        this.courseService = courseService;
        this.courseUserService = courseUserService;
        this.authUserClient = authUserClient;
    }

    @GetMapping("/api/v1/courses/{courseId}/users")
    public ResponseEntity<Page<UserDto>> getAllUsersByCourse(
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "courseId") UUID courseId) {

        return ResponseEntity.status(HttpStatus.OK).body(authUserClient.getAllUsersByCourse(courseId, pageable));
    }

    @PostMapping("/api/v1/courses/{courseId}/users/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(
            @PathVariable(value = "courseId") UUID courseId,
            @RequestBody @Valid SubscriptionDto request) {
        ResponseEntity<UserDto> responseUser;

        Optional<CourseModel> possibleCourse = courseService.findById(courseId);
        if (possibleCourse.isEmpty()) {
            log.warn("POST saveSubscriptionUserInCourse, courseId {} - NOT FOUND", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        if (courseUserService.existsByCourseAndUserId(possibleCourse.get(), request.getUserId())) {
            log.warn("POST saveSubscriptionUserInCourse, courseId {} - User already registered userId {}", courseId, request.getUserId());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("User already registered in course");
        }
        try {
            responseUser = authUserClient.getOneUserById(request.getUserId());
            if (responseUser.getBody().getUserStatus().equals(UserStatus.BLOCKED)) {
                log.warn("POST saveSubscriptionUserInCourse, courseId {} - User is blocked userId {}", courseId, request.getUserId());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("User is blocked.");
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                log.warn("POST saveSubscriptionUserInCourse, userId {} - NOT FOUND", request.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            } else {
                log.error("POST saveSubscriptionUserInCourse Internal Server Error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
            }
        }

        var courseUserModel = courseUserService.save(new CourseUserModel(null, possibleCourse.get(), request.getUserId()));

        //enviar post para ms authuser


        return ResponseEntity.status(HttpStatus.CREATED).body("Subscription created successfully.");
    }
}

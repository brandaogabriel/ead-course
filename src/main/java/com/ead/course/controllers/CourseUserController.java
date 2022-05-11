package com.ead.course.controllers;

import com.ead.course.dtos.SubscriptionDto;
import com.ead.course.enums.UserStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.UserModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.UserService;
import com.ead.course.specifications.SpecificationTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseUserController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseUserController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/api/v1/courses/{courseId}/users")
    public ResponseEntity<Object> getAllUsersByCourse(
            SpecificationTemplate.UserSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "courseId") UUID courseId) {

        Optional<CourseModel> possibleCourse = courseService.findById(courseId);
        if (possibleCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll(SpecificationTemplate.userCourseId(courseId).and(spec), pageable));
    }

    @PostMapping("/api/v1/courses/{courseId}/users/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(
            @PathVariable(value = "courseId") UUID courseId,
            @RequestBody @Valid SubscriptionDto request) {
        Optional<CourseModel> possibleCourse = courseService.findById(courseId);
        if (possibleCourse.isEmpty()) {
            log.warn("POST saveSubscriptionUserInCourse, courseId {} - NOT FOUND", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        if (courseService.existsByCourseAndUser(courseId, request.getUserId())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: Subscription already exists");
        }
        Optional<UserModel> possibleUserModel = userService.findById(request.getUserId());
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found");
        }
        if (possibleUserModel.get().getUserStatus().equals(UserStatus.BLOCKED.toString())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: User is blocked");
        }

        courseService.saveSubscriptionUserInCourseAndSendNotification(possibleCourse.get(), possibleUserModel.get());
        return ResponseEntity.status(HttpStatus.CREATED).body("Subscription created successfully");
    }

}

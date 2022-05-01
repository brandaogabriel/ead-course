package com.ead.course.controllers;

import com.ead.course.dtos.CourseDto;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import com.ead.course.specifications.SpecificationTemplate;
import com.ead.course.validation.CourseValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseService courseService;
    private final CourseValidator courseValidator;

    public CourseController(CourseService courseService, CourseValidator courseValidator) {
        this.courseService = courseService;
        this.courseValidator = courseValidator;
    }

    @PostMapping
    public ResponseEntity<Object> saveCourse(@RequestBody CourseDto request, Errors errors) {
        log.info("POST saveCourse {} - START", request.toString());
        courseValidator.validate(request, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        }

        var course = new CourseModel();
        BeanUtils.copyProperties(request, course);
        course.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        course.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        log.info("POST saveCourse SUCCESS");

        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.save(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("id") UUID id) {
        log.info("DELETE deleteCourse, courseId {} - START", id);

        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            log.warn("DELETE deleteCourse, courseId {} - NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        courseService.delete(possibleCourse.get());
        log.info("DELETE deleteCourse, courseId {} - DELETED", id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCourse(@PathVariable("id") UUID id, @RequestBody @Valid CourseDto request) {
        log.info("PUT updateCourse, courseId {}, body {} - START", id, request.toString());


        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            log.info("PUT updateCourse, courseId {} - NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        var course = possibleCourse.get();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setCourseStatus(request.getCourseStatus());
        course.setCourseLevel(request.getCourseLevel());
        course.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        log.info("PUT updateCourse, courseId {} - SUCCESS", id);
        log.debug("PUT updateCourse, body {} - SUCCESS", course.toString());

        return ResponseEntity.status(HttpStatus.OK).body(courseService.save(course));
    }

    @GetMapping
    public ResponseEntity<Page<CourseModel>> getAllCourses(SpecificationTemplate.CourseSpec spec,
                                                           @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable,
                                                           @RequestParam(required = false) UUID userId) {
        log.info("GET getAllCourses paged - START");
        if (userId != null) {
            return ResponseEntity.status(HttpStatus.OK).body(courseService.findAll(SpecificationTemplate.courseUserId(userId).and(spec), pageable));
        }
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findAll(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneCourse(@PathVariable("id") UUID id) {
        log.info("GET getOneCourse, courseId {} - START", id);

        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            log.warn("GET getOneCourse, courseId {} - NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(possibleCourse.get());
    }
}

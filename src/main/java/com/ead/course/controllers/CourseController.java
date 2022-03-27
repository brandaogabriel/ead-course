package com.ead.course.controllers;

import com.ead.course.dtos.CourseDto;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import com.ead.course.specifications.SpecificationTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Object> saveCourse(@RequestBody @Valid CourseDto request) {
        var course = new CourseModel();
        BeanUtils.copyProperties(request, course);
        course.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        course.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.save(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("id") UUID id) {
        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        courseService.delete(possibleCourse.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCourse(@PathVariable("id") UUID id, @RequestBody @Valid CourseDto request) {
        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        var course = possibleCourse.get();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setCourseStatus(request.getCourseStatus());
        course.setCourseLevel(request.getCourseLevel());
        course.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.OK).body(courseService.save(course));
    }

    @GetMapping
    public ResponseEntity<Page<CourseModel>> getAllCourses(SpecificationTemplate.CourseSpec spec,
                                                           @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findAll(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneCourse(@PathVariable("id") UUID id) {
        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(possibleCourse.get());
    }
}

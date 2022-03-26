package com.ead.course.controllers;

import com.ead.course.dtos.CourseDto;
import com.ead.course.dtos.ModuleDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.ModuleService;
import org.springframework.beans.BeanUtils;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseService courseService;

    public ModuleController(ModuleService moduleService, CourseService courseService) {
        this.moduleService = moduleService;
        this.courseService = courseService;
    }

    @PostMapping("/api/v1/courses/{id}/modules")
    public ResponseEntity<Object> saveModule(@PathVariable("id") UUID id, @RequestBody @Valid ModuleDto request) {
        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        var module = new ModuleModel();
        BeanUtils.copyProperties(request, module);
        module.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        module.setCourse(possibleCourse.get());

        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.save(module));
    }

    @DeleteMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {
        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        moduleService.delete(possibleModule.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> updateModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId,
                                               @RequestBody @Valid ModuleDto request) {
        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        var module = possibleModule.get();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.save(module));
    }

    @GetMapping("/api/v1/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleModel>> getAllModules(@PathVariable("courseId") UUID courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findAllByCourse(courseId));
    }

    @GetMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> getOneModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {
        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        return ResponseEntity.status(HttpStatus.OK).body(possibleModule.get());
    }
}

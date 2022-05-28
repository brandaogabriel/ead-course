package com.ead.course.controllers;

import com.ead.course.dtos.ModuleDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.ModuleService;
import com.ead.course.specifications.SpecificationTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseService courseService;

    public ModuleController(ModuleService moduleService, CourseService courseService) {
        this.moduleService = moduleService;
        this.courseService = courseService;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping("/api/v1/courses/{id}/modules")
    public ResponseEntity<Object> saveModule(@PathVariable("id") UUID id, @RequestBody @Valid ModuleDto request) {
        log.info("POST saveModule, courseId {} - START", id);
        Optional<CourseModel> possibleCourse = courseService.findById(id);
        if (possibleCourse.isEmpty()) {
            log.warn("POST saveModule, courseId {} - NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        var module = new ModuleModel();
        BeanUtils.copyProperties(request, module);
        module.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        module.setCourse(possibleCourse.get());

        log.info("POST saveModule, courseId {} - SUCCESS", id);
        log.debug("POST saveModule {} - SUCCESS", module.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.save(module));
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> deleteModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {
        log.info("DELETE deleteModule, courseId {}, moduleId {} - START", courseId, moduleId);
        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            log.warn("DELETE deleteModule, courseId {}, moduleId {} - NOT FOUND", courseId, moduleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        moduleService.delete(possibleModule.get());
        log.info("DELETE deleteModule, courseId {}, moduleId {} - SUCCESS", courseId, moduleId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> updateModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId,
                                               @RequestBody @Valid ModuleDto request) {
        log.info("PUT updateModule, courseId {}, moduleId {} - START", courseId, moduleId);

        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            log.warn("PUT updateModule, courseId {}, moduleId {} - NOT FOUND", courseId, moduleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        var module = possibleModule.get();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());

        log.info("PUT updateModule, courseId {}, moduleId {} - SUCCESS", courseId, moduleId);
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.save(module));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/api/v1/courses/{courseId}/modules")
    public ResponseEntity<Page<ModuleModel>> getAllModules(@PathVariable("courseId") UUID courseId,
                                                           SpecificationTemplate.ModuleSpec spec,
                                                           @PageableDefault(page = 0, size = 10, sort = "moduleId", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET getAllModules paged - START");
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findAllByCourse(SpecificationTemplate.moduleCourseId(courseId).and(spec), pageable));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/api/v1/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> getOneModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {
        log.info("GET getOneModule, courseId {}, moduleId {} - START", courseId, moduleId);

        Optional<ModuleModel> possibleModule = moduleService.findByModuleIntoCourse(courseId, moduleId);
        if (possibleModule.isEmpty()) {
            log.warn("GET getOneModule, courseId {}, moduleId {} - NOT FOUND", courseId, moduleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        log.info("GET getOneModule, courseId {}, moduleId {} - SUCCESS", courseId, moduleId);
        return ResponseEntity.status(HttpStatus.OK).body(possibleModule.get());
    }
}

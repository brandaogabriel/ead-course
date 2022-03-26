package com.ead.course.repositories;

import com.ead.course.models.ModuleModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<ModuleModel, UUID> {

    /**
     * With @EntityGraph annotation we can fetch associations like "course"
     * of entity ModuleModel, even if the association is defined with FetchType LAZY.
     * Works like an override to change the FetchType from LAZY to EAGER to the specific fetch below.
     *
     * @param title the module title
     * @return the module entity with relation course
     */
    @EntityGraph(attributePaths = "course")
    Optional<ModuleModel> findByTitle(String title);

    @Query(value = "SELECT * FROM tb_module WHERE course_course_id = :courseId", nativeQuery = true)
    List<ModuleModel> findAllModulesIntoCourse(@Param("courseId") UUID courseId);
}

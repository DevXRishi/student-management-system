package com.rishi.student_management.repository;

import com.rishi.student_management.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByNameContainingIgnoreCase(String name);

    @Query("""
            SELECT s FROM Student s
            WHERE (:course IS NULL OR :course = '' OR LOWER(s.course) = LOWER(:course))
              AND (:search IS NULL OR :search = ''
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.city) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.course) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR s.phone LIKE CONCAT('%', :search, '%'))
            """)
    Page<Student> searchStudents(
            @Param("search") String search,
            @Param("course") String course,
            Pageable pageable
    );

    @Query("SELECT DISTINCT s.course FROM Student s ORDER BY s.course ASC")
    List<String> findDistinctCourses();
}

package com.rishi.student_management.controller;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/studentmgmt")
@Tag(name = "Student Management APIs", description = "CRUD APIs for Educore Student Management System")
@SecurityRequirement(name = "bearer-key")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student Created Successfully"),
            @ApiResponse(responseCode = "400", description = "Validation Failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Add Student", description = "Creates a new student in the database")
    public StudentResponse addStudent(@Valid @RequestBody StudentRequest request) {
        logger.info("POST /studentmgmt called");
        return service.addStudent(request);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Students Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    @Operation(summary = "Get All Students", description = "Returns paginated, filtered, and sorted list of students")
    public Page<StudentResponse> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String course) {

        logger.info(
                "GET /studentmgmt called - page: {}, size: {}, sortBy: {}, direction: {}, search: {}, course: {}",
                page, size, sortBy, direction, search, course
        );

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCourse = course != null && !course.isBlank();
        if (hasSearch || hasCourse) {
            return service.searchStudents(search, course, page, size, sortBy, direction);
        }
        return service.getAllStudents(page, size, sortBy, direction);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/courses")
    @Operation(summary = "Get Distinct Courses", description = "Returns all unique course names")
    public List<String> getAllCourses() {
        return service.getAllCourses();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student Found"),
            @ApiResponse(responseCode = "404", description = "Student Not Found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get Student By ID", description = "Returns student details using ID")
    public StudentResponse getStudentById(@PathVariable Integer id) {
        logger.info("GET /studentmgmt/{}", id);
        return service.getStudentById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update Student", description = "Updates an existing student")
    public StudentResponse updateStudent(
            @PathVariable Integer id,
            @Valid @RequestBody StudentRequest request) {
        logger.info("PUT /studentmgmt/{}", id);
        return service.updateStudent(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Student", description = "Deletes a student using ID")
    public String deleteStudent(@PathVariable Integer id) {
        logger.info("DELETE /studentmgmt/{}", id);
        service.deleteStudent(id);
        return "Student Deleted Successfully";
    }
}

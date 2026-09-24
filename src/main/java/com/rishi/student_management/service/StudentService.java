package com.rishi.student_management.service;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import org.springframework.data.domain.Page;

public interface StudentService {

    StudentResponse addStudent(StudentRequest request);

    Page<StudentResponse> getAllStudents(
            int page,
            int size,
            String sortBy,
            String direction
    );

    StudentResponse getStudentById(Integer id);

    StudentResponse updateStudent(Integer id, StudentRequest request);

    void deleteStudent(Integer id);
}
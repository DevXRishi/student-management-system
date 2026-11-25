package com.rishi.student_management.service.impl;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.entity.Student;
import com.rishi.student_management.exception.StudentNotFoundException;
import com.rishi.student_management.repository.StudentRepository;
import com.rishi.student_management.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "name", "email", "phone", "city", "course", "marks");

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public StudentResponse addStudent(StudentRequest request) {
        logger.info("Adding new student");
        Student student = new Student();
        applyRequest(student, request);

        Student saved = studentRepository.save(student);
        logger.info("Student saved successfully with ID {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public Page<StudentResponse> getAllStudents(
            int page,
            int size,
            String sortBy,
            String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(this::toResponse);
    }

    @Override
    public Page<StudentResponse> searchStudents(
            String search,
            String course,
            int page,
            int size,
            String sortBy,
            String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        String trimmedSearch = search == null ? null : search.trim();
        String trimmedCourse = course == null ? null : course.trim();
        return studentRepository.searchStudents(trimmedSearch, trimmedCourse, pageable)
                .map(this::toResponse);
    }

    @Override
    public List<String> getAllCourses() {
        return studentRepository.findDistinctCourses();
    }

    @Override
    public StudentResponse getStudentById(Integer id) {
        logger.info("Fetching student with ID {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        return toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Integer id, StudentRequest request) {
        logger.info("Updating student with ID {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));

        applyRequest(student, request);
        Student updated = studentRepository.save(student);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStudent(Integer id) {
        logger.info("Deleting student with ID {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student with ID " + id + " not found"));
        studentRepository.delete(student);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(safeSort).descending()
                : Sort.by(safeSort).ascending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setName(request.getName().trim());
        student.setEmail(request.getEmail().trim());
        student.setPhone(request.getPhone().trim());
        student.setCity(request.getCity().trim());
        student.setCourse(request.getCourse().trim());
        student.setMarks(request.getMarks());
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setEmail(student.getEmail());
        response.setPhone(student.getPhone());
        response.setCity(student.getCity());
        response.setCourse(student.getCourse());
        response.setMarks(student.getMarks());
        return response;
    }
}

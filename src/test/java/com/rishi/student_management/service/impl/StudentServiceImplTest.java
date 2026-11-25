package com.rishi.student_management.service.impl;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.entity.Student;
import com.rishi.student_management.exception.StudentNotFoundException;
import com.rishi.student_management.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private StudentRequest request;

    @BeforeEach
    void setUp() {
        request = new StudentRequest();
        request.setName("Rishi");
        request.setEmail("rishi@gmail.com");
        request.setPhone("9876543210");
        request.setCity("Nagpur");
        request.setCourse("Computer Engineering");
        request.setMarks(90);

        student = new Student();
        student.setId(1);
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setCity(request.getCity());
        student.setCourse(request.getCourse());
        student.setMarks(request.getMarks());
    }

    @Test
    void addStudent_shouldSaveAndReturnResponse() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.addStudent(request);

        assertNotNull(response);
        assertEquals(student.getId(), response.getId());
        assertEquals(student.getName(), response.getName());
        assertEquals(student.getEmail(), response.getEmail());
        assertEquals(student.getPhone(), response.getPhone());
        assertEquals(student.getCity(), response.getCity());
        assertEquals(student.getCourse(), response.getCourse());
        assertEquals(student.getMarks(), response.getMarks());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void getStudentById_shouldReturnStudent() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Rishi", response.getName());
        verify(studentRepository).findById(1);
    }

    @Test
    void getStudentById_whenMissing_shouldThrowNotFound() {
        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudentById(100)
        );

        assertEquals("Student not found with id: 100", exception.getMessage());
        verify(studentRepository).findById(100);
    }

    @Test
    void getAllStudents_shouldFallbackUnsafeSortFieldToId() {
        when(studentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student)));

        Page<StudentResponse> response = studentService.getAllStudents(0, 5, "nonExistentColumn", "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(studentRepository).findAll(pageableCaptor.capture());
        assertNotNull(pageableCaptor.getValue().getSort().getOrderFor("id"));
        assertEquals(1, response.getContent().size());
    }

    @Test
    void searchStudents_shouldTrimAndFilter() {
        when(studentRepository.searchStudents(eq("Rishi"), eq("CSE"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student)));

        Page<StudentResponse> response = studentService.searchStudents("  Rishi ", " CSE ", 0, 10, "name", "desc");

        assertEquals(1, response.getContent().size());
        verify(studentRepository).searchStudents(eq("Rishi"), eq("CSE"), any(Pageable.class));
    }

    @Test
    void getAllCourses_shouldReturnDistinctCourses() {
        when(studentRepository.findDistinctCourses()).thenReturn(List.of("CSE", "IT"));

        List<String> courses = studentService.getAllCourses();

        assertEquals(List.of("CSE", "IT"), courses);
        verify(studentRepository).findDistinctCourses();
    }

    @Test
    void updateStudent_shouldUpdateAndSave() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.updateStudent(1, request);

        assertNotNull(response);
        assertEquals("Rishi", response.getName());
        verify(studentRepository).findById(1);
        verify(studentRepository).save(student);
    }

    @Test
    void updateStudent_whenMissing_shouldThrowNotFound() {
        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.updateStudent(100, request)
        );

        assertEquals("Student not found with id: 100", exception.getMessage());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_shouldDelete() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1);

        verify(studentRepository).findById(1);
        verify(studentRepository).delete(student);
    }

    @Test
    void deleteStudent_whenMissing_shouldThrowNotFound() {
        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.deleteStudent(100)
        );

        assertEquals("Student with ID 100 not found", exception.getMessage());
        verify(studentRepository, never()).delete(any(Student.class));
    }
}

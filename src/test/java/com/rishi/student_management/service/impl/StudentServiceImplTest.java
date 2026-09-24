package com.rishi.student_management.service.impl;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.entity.Student;
import com.rishi.student_management.exception.StudentNotFoundException;
import com.rishi.student_management.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import static org.mockito.Mockito.never;

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
    void testAddStudent() {

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

        verify(studentRepository, times(1)).save(any(Student.class));

    }

    @Test
    void testGetStudentById() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentById(1);

        assertNotNull(response);

        assertEquals(student.getId(), response.getId());
        assertEquals(student.getName(), response.getName());
        assertEquals(student.getEmail(), response.getEmail());
        assertEquals(student.getPhone(), response.getPhone());
        assertEquals(student.getCity(), response.getCity());
        assertEquals(student.getCourse(), response.getCourse());
        assertEquals(student.getMarks(), response.getMarks());

        verify(studentRepository, times(1)).findById(1);

    }

    @Test
    void testGetStudentById_NotFound() {

        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(100));

        assertEquals("Student not found with id: 100", exception.getMessage());

        verify(studentRepository, times(1)).findById(100);

    }

    @Test
    void testGetAllStudents() {

        Student student2 = new Student();

        student2.setId(2);
        student2.setName("Rahul");
        student2.setEmail("rahul@gmail.com");
        student2.setPhone("9999999999");
        student2.setCity("Pune");
        student2.setCourse("IT");
        student2.setMarks(85);

        List<Student> students = Arrays.asList(student, student2);
        Page<Student> studentPage = new PageImpl<>(students);

        when(studentRepository.findAll(any(Pageable.class)))
                .thenReturn(studentPage);

        Page<StudentResponse> response =
                studentService.getAllStudents(0, 5, "id", "asc");

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("Rishi", response.getContent().get(0).getName());
        assertEquals("Rahul", response.getContent().get(1).getName());

        verify(studentRepository, times(1))
                .findAll(any(Pageable.class));

    }

    @Test
    void testUpdateStudent() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.updateStudent(1, request);

        assertNotNull(response);

        assertEquals(student.getId(), response.getId());

        assertEquals(student.getName(), response.getName());

        assertEquals(student.getEmail(), response.getEmail());

        assertEquals(student.getPhone(), response.getPhone());

        assertEquals(student.getCity(), response.getCity());

        assertEquals(student.getCourse(), response.getCourse());

        assertEquals(student.getMarks(), response.getMarks());

        verify(studentRepository, times(1)).findById(1);

        verify(studentRepository, times(1)).save(any(Student.class));

    }

    @Test
    void testUpdateStudent_NotFound() {

        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> studentService.updateStudent(100, request));

        assertEquals("Student not found with id: 100", exception.getMessage());

        verify(studentRepository, times(1)).findById(100);

        verify(studentRepository, never()).save(any(Student.class));

    }

    @Test
    void testDeleteStudent() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1);

        verify(studentRepository, times(1)).findById(1);

        verify(studentRepository, times(1)).delete(student);

    }

    @Test
    void testDeleteStudent_NotFound() {

        when(studentRepository.findById(100)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(100));

        assertEquals("Student with ID 100 not found", exception.getMessage());

        verify(studentRepository, times(1)).findById(100);

        verify(studentRepository, never()).delete(any(Student.class));

    }

}
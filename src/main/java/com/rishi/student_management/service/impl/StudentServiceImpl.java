package com.rishi.student_management.service.impl;

import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.entity.Student;
import com.rishi.student_management.repository.StudentRepository;
import com.rishi.student_management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rishi.student_management.exception.StudentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger =
            LoggerFactory.getLogger(StudentServiceImpl.class);


    @Autowired
    StudentRepository studentRepository;

    @Override
    public StudentResponse addStudent(StudentRequest request) {

        logger.info("Adding new student...");
        Student student=new Student();

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setCity(request.getCity());
        student.setCourse(request.getCourse());
        student.setMarks(request.getMarks());

        Student studentSaved=studentRepository.save(student);
        logger.info("Student saved successfully with ID {}",
                studentSaved.getId());

        StudentResponse response=new StudentResponse();
        response.setId(studentSaved.getId());
        response.setName(studentSaved.getName());
        response.setEmail(studentSaved.getEmail());
        response.setPhone(studentSaved.getPhone());
        response.setCity(studentSaved.getCity());
        response.setCourse(studentSaved.getCourse());
        response.setMarks(studentSaved.getMarks());

        return response;
    }

    @Override
    public Page<StudentResponse> getAllStudents(
            int page,
            int size,
            String sortBy,
            String direction) {

        logger.info(
                "Fetching students - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction
        );

        Sort sort;

        if (direction.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> students =
                studentRepository.findAll(pageable);

        logger.info(
                "Total Students Found: {}",
                students.getTotalElements()
        );

        return students.map(student -> {

            StudentResponse response =
                    new StudentResponse();

            response.setId(student.getId());
            response.setName(student.getName());
            response.setEmail(student.getEmail());
            response.setPhone(student.getPhone());
            response.setCity(student.getCity());
            response.setCourse(student.getCourse());
            response.setMarks(student.getMarks());

            return response;
        });
    }

    @Override
    public StudentResponse getStudentById(Integer id) {
        logger.info("Fetching student with ID {}",
                id);
        Student student = studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        logger.info("Student Found");

        StudentResponse studentResponse=new StudentResponse();

        studentResponse.setId(student.getId());
        studentResponse.setName(student.getName());
        studentResponse.setEmail(student.getEmail());
        studentResponse.setPhone(student.getPhone());
        studentResponse.setCity(student.getCity());
        studentResponse.setCourse(student.getCourse());
        studentResponse.setMarks(student.getMarks());

        return studentResponse;
    }

    @Override
    public StudentResponse updateStudent(Integer id, StudentRequest request) {
        logger.info("Updating student with ID {}",
                id);
        Student student = studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));


        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setCity(request.getCity());
        student.setCourse(request.getCourse());
        student.setMarks(request.getMarks());

        Student updatedStudent = studentRepository.save(student);
        logger.info("Student Updated Successfully");

        StudentResponse response = new StudentResponse();
        response.setId(updatedStudent.getId());
        response.setName(updatedStudent.getName());
        response.setEmail(updatedStudent.getEmail());
        response.setPhone(updatedStudent.getPhone());
        response.setCity(updatedStudent.getCity());
        response.setCourse(updatedStudent.getCourse());
        response.setMarks(updatedStudent.getMarks());

        return response;
    }

    @Override
    public void deleteStudent(Integer id) {
        logger.info("Deleting student with ID {}",
                id);
        Student student=studentRepository.findById(id)
                .orElseThrow(() ->
                        new StudentNotFoundException(
                                "Student with ID " + id + " not found"
                        ));
        logger.info("Student Deleted Successfully");
        studentRepository.delete(student);
    }
}

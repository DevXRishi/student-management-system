package com.rishi.student_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishi.student_management.config.SecurityConfig;
import com.rishi.student_management.dto.StudentRequest;
import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.exception.CustomAccessDeniedHandler;
import com.rishi.student_management.exception.CustomAuthenticationEntryPoint;
import com.rishi.student_management.exception.StudentNotFoundException;
import com.rishi.student_management.filter.JwtAuthenticationFilter;
import com.rishi.student_management.service.CustomUserDetailsService;
import com.rishi.student_management.service.JwtService;
import com.rishi.student_management.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@WebMvcTest(StudentController.class)
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void getAllStudentsWithoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/studentmgmt")).andExpect(status().isUnauthorized());
    }

    @Test
    void getAllStudents_shouldReturn200() throws Exception {

        StudentResponse student = studentResponse();

        Page<StudentResponse> page =
                new PageImpl<>(List.of(student));

        when(service.getAllStudents(0, 5, "id", "asc"))
                .thenReturn(page);

        mockMvc.perform(get("/studentmgmt")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Rishi"))
                .andExpect(jsonPath("$.content[0].email").value("rishi@gmail.com"));
    }

    @Test
    void addStudent_shouldReturnSuccess() throws Exception {
        StudentRequest request = studentRequest();
        StudentResponse response = studentResponse();

        when(service.addStudent(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/studentmgmt").with(user("admin").roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Rishi")).andExpect(jsonPath("$.email").value("rishi@gmail.com"));

        verify(service).addStudent(any(StudentRequest.class));
    }

    @Test
    void getStudentById_shouldReturn200() throws Exception {
        when(service.getStudentById(1)).thenReturn(studentResponse());

        mockMvc.perform(get("/studentmgmt/1").with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Rishi")).andExpect(jsonPath("$.email").value("rishi@gmail.com"));

        verify(service).getStudentById(1);
    }

    @Test
    void getStudentById_shouldReturn404() throws Exception {
        when(service.getStudentById(1)).thenThrow(new StudentNotFoundException("Student not found with id: 1"));

        mockMvc.perform(get("/studentmgmt/1").with(user("admin").roles("ADMIN"))).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.message").value("Student not found with id: 1"));

        verify(service).getStudentById(1);
    }

    @Test
    void updateStudent_shouldReturn200() throws Exception {
        StudentRequest request = studentRequest();
        StudentResponse response = studentResponse();

        when(service.updateStudent(1, request)).thenReturn(response);

        mockMvc.perform(put("/studentmgmt/1").with(user("admin").roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Rishi")).andExpect(jsonPath("$.email").value("rishi@gmail.com"));

        verify(service).updateStudent(1, request);
    }

    @Test
    void deleteStudent_shouldReturnSuccess() throws Exception {
        doNothing().when(service).deleteStudent(1);

        mockMvc.perform(delete("/studentmgmt/1").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$").value("Student Deleted Successfully"));

        verify(service).deleteStudent(1);
    }

    private StudentRequest studentRequest() {
        StudentRequest request = new StudentRequest();
        request.setName("Rishi");
        request.setEmail("rishi@gmail.com");
        request.setPhone("9876543210");
        request.setCity("Nagpur");
        request.setCourse("CSE");
        request.setMarks(90);
        return request;
    }

    private StudentResponse studentResponse() {
        StudentResponse response = new StudentResponse();
        response.setId(1);
        response.setName("Rishi");
        response.setEmail("rishi@gmail.com");
        response.setPhone("9876543210");
        response.setCity("Nagpur");
        response.setCourse("CSE");
        response.setMarks(90);
        return response;
    }
}

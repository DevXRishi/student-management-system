package com.rishi.student_management.dto;


import lombok.Data;

@Data
public class StudentResponse {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String course;
    private Integer marks;
}

package com.rishi.student_management.entity;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="studentmgmt")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false,unique = true)
    private String phone;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String course;

    @Column(nullable = false)
    private Integer marks;


}

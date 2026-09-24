package com.rishi.student_management.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        System.out.println(
                encoder.matches(
                        "admin123",
                        "$2a$10$ykeAxYno03PjPk29wifxseMfnZMQk66Xu7yhrCSP0uJvhM2pASSQK"
                )
        );

    }

}
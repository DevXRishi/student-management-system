package com.rishi.student_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StudentRequest {

    @Schema(description = "Student Name", example = "Rishi")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(description = "Student Email", example = "rishi@example.com")
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    private String email;

    @Schema(description = "Student Phone", example = "9876543210")
    @NotBlank(message = "Phone cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must contain exactly 10 digits")
    private String phone;

    @Schema(description = "Student Course", example = "Computer Science")
    @NotBlank(message = "Course cannot be blank")
    private String course;

    @Schema(description = "Student City", example = "Nagpur")
    @NotBlank(message = "City cannot be blank")
    private String city;

    @Schema(description = "Student Marks", example = "85")
    @NotNull(message = "Marks cannot be null")
    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot be greater than 100")
    private Integer marks;
}

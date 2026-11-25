
package com.rishi.student_management.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final Path uploadDir = Paths.get("uploads");

    private final Set<String> allowedTypes = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please select a file.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body("File size must not exceed 5 MB.");
        }

        if (!allowedTypes.contains(file.getContentType())) {
            return ResponseEntity.badRequest()
                    .body("Only PDF, PNG, and JPEG files are allowed.");
        }

        Files.createDirectories(uploadDir);

        String originalName = Paths.get(
                file.getOriginalFilename() == null
                        ? "file"
                        : file.getOriginalFilename()
        ).getFileName().toString();

        String filename = UUID.randomUUID()
                + "_" + originalName;

        Path destination = uploadDir.resolve(filename);

        file.transferTo(destination);

        return ResponseEntity.ok(
                "File uploaded successfully: " + filename
        );
    }
}

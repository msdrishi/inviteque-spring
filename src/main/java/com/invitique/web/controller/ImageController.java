package com.invitique.web.controller;

import com.invitique.domain.model.TemplateImage;
import com.invitique.domain.repository.TemplateImageRepository;
import com.invitique.service.CloudinaryMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*") // Update as per your security policy
public class ImageController {

    @Autowired
    private TemplateImageRepository templateImageRepository;

    @Autowired
    private CloudinaryMigrationService migrationService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            TemplateImage image = TemplateImage.builder()
                    .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .build();

            TemplateImage savedImage = templateImageRepository.save(image);

            // Construct the local URL for this image
            String imageUrl = "/api/images/" + savedImage.getId();
            
            Map<String, String> response = new HashMap<>();
            response.put("secure_url", imageUrl);
            response.put("url", imageUrl);
            response.put("public_id", savedImage.getId().toString());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        Optional<TemplateImage> imageOpt = templateImageRepository.findById(id);
        if (imageOpt.isPresent()) {
            TemplateImage image = imageOpt.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .body(image.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestBody Map<String, String> payload) {
        try {
            String publicId = payload.get("publicId");
            if (publicId != null) {
                templateImageRepository.deleteById(UUID.fromString(publicId));
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/migrate-cloudinary")
    public ResponseEntity<String> migrateCloudinary() {
        migrationService.migrateCloudinaryImages();
        return ResponseEntity.ok("Migration triggered successfully.");
    }

    @PostMapping("/revert-migration")
    public ResponseEntity<String> revertMigration() {
        migrationService.revertCloudinaryMigration();
        return ResponseEntity.ok("Revert process completed.");
    }
}

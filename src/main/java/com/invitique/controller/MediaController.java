package com.invitique.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@CrossOrigin(origins = "*") // Adjust based on your security setup
public class MediaController {

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    @GetMapping("/upload-url")
    public ResponseEntity<Map<String, String>> getUploadUrl(@RequestParam("filename") String filename) {
        
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        
        String uploadUrl = presignedRequest.url().toString();
        String finalPublicUrl = publicUrl + "/" + filename;

        Map<String, String> response = new HashMap<>();
        response.put("preSignedUrl", uploadUrl);
        response.put("publicUrl", finalPublicUrl);

        return ResponseEntity.ok(response);
    }
}

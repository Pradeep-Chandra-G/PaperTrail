package com.pradeep.papertrail.controller;

import com.pradeep.papertrail.dto.ImageUploadResponse;
import com.pradeep.papertrail.model.User;
import com.pradeep.papertrail.repository.UserRepository;
import com.pradeep.papertrail.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    public ImageController(CloudinaryService cloudinaryService, UserRepository userRepository) {
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, user.getId());

            ImageUploadResponse response = new ImageUploadResponse(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"),
                    (String) uploadResult.get("format"),
                    user.getId(),
                    (String) uploadResult.get("folder")
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(
            @RequestParam("publicId") String publicId,
            Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify that the public ID belongs to the user's folder
            if (!publicId.startsWith("papertrail/user_" + user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to delete this image");
            }

            Map<String, Object> result = cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/url")
    public ResponseEntity<?> getImageUrl(@RequestParam("publicId") String publicId) {
        try {
            String url = cloudinaryService.getImageUrl(publicId);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }
}
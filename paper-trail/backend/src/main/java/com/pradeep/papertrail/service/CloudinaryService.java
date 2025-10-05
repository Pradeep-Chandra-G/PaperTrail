package com.pradeep.papertrail.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload an image to Cloudinary in a user-specific folder
     * @param file The image file to upload
     * @param userId The ID of the user uploading the image
     * @return Map containing upload result details (url, public_id, etc.)
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Upload with user-specific folder
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "papertrail/user_" + userId,
                "resource_type", "auto",
                "use_filename", true,
                "unique_filename", true
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    /**
     * Delete an image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Get the URL for an image
     * @param publicId The public ID of the image
     * @return The image URL
     */
    public String getImageUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }

    /**
     * Delete all images in a user's folder
     * @param userId The user ID
     * @throws IOException if deletion fails
     */
    public void deleteUserFolder(Long userId) throws Exception {
        String folder = "papertrail/user_" + userId;
        cloudinary.api().deleteResourcesByPrefix(folder, ObjectUtils.emptyMap());
    }
}
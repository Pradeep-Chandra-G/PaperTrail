package com.pradeep.papertrail.dto;

public class ImageUploadResponse {
    private String url;
    private String publicId;
    private String format;
    private Long userId;
    private String folder;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(String url, String publicId, String format, Long userId, String folder) {
        this.url = url;
        this.publicId = publicId;
        this.format = format;
        this.userId = userId;
        this.folder = folder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
package com.greentrack.service;
import com.greentrack.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Service @Slf4j
public class FileUploadService {
    @Value("${app.upload.path}") private String uploadPath;
    private static final Map<String,String> ALLOWED_EXT_BY_TYPE = Map.of(
        "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final long MAX = 10 * 1024 * 1024;

    public String uploadFile(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) throw new BusinessException("File is empty");
        if (file.getSize() > MAX) throw new BusinessException("File exceeds 10MB limit");
        String ext = ALLOWED_EXT_BY_TYPE.get(file.getContentType());
        if (ext == null) throw new BusinessException("Only JPEG, PNG, WebP allowed");
        try {
            Path dir = Paths.get(uploadPath, subfolder).normalize();
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + "." + ext;
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subfolder + "/" + name;
        } catch (IOException e) { throw new BusinessException("Upload failed"); }
    }

    public void deleteFile(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        try { Files.deleteIfExists(Paths.get(uploadPath, url.substring("/uploads/".length())).normalize()); }
        catch (IOException e) { log.warn("Failed to delete file {}: {}", url, e.getMessage()); }
    }
}
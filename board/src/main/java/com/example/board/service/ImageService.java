package com.example.board.service;

import com.example.board.dto.post.ImageResponse;
import com.example.board.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    @Value("${file.upload.location}")
    private String uploadPath;

    public String saveFile(MultipartFile file) throws FileUploadException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            validateImage(originalFilename);
            String storedFileName = createStoredFileName(originalFilename);

            Path destinationFile = Paths.get(uploadPath)
                    .resolve(Paths.get(storedFileName))
                    .normalize().toAbsolutePath();

            //save file
            file.transferTo(destinationFile);

            return storedFileName;
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file", e);
        }
    }

    private String createStoredFileName(String originalFilename) {
        String ext = extractExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExtension(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private void validateImage(String originalFileName) {
        String extension = extractExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension))
            throw new InvalidFileTypeException("Invalid file type: " + extension);
    }

    public ImageResponse savedImages(MultipartFile file) throws FileUploadException {
        String imageUrl = saveFile(file);
        return new ImageResponse(imageUrl, file.getOriginalFilename(), imageUrl);
    }

}

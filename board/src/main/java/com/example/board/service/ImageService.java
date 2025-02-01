package com.example.board.service;

import com.example.board.dto.post.ImageResponse;
import com.example.board.exception.FileDeleteException;
import com.example.board.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    @Value("${file.upload.location}")
    private String uploadPath;
    @Value("${file.upload.temp}")
    private String uploadTempDir;


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

    public void deleteImage(String storedFileName) {
        try {
            Path filePath = Paths.get(uploadPath)
                    .resolve(storedFileName)
                    .normalize()
                    .toAbsolutePath();

            if (Files.exists(filePath)){
                Files.delete(filePath);
                log.info("File deleted successfully: {}", storedFileName);
            }else {
                log.warn("File not found: {}", storedFileName);
            }
        } catch (IOException e){
            log.error("Failed to delete file: {}", storedFileName, e);
            throw new FileDeleteException("파일 삭제 중 오류가 발생했습니다: "+storedFileName, e);
        }
    }

    public String uploadImage(MultipartFile file, boolean isTemporary) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateImage(originalFileName);

        String fileName = createStoredFileName(originalFileName);
        Path uploadDir = Paths.get(isTemporary ? uploadTempDir : uploadPath);
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath);

        return (isTemporary ? "/temp-images/" : "/perm-images/") + fileName;
    }

    public void moveToPermanent(String tempUrl) throws IOException {
        String fileName = tempUrl.replace("/temp-images/", "");
        Path source = Paths.get(uploadTempDir, fileName);
        Path target = Paths.get(uploadPath, fileName);

        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

}

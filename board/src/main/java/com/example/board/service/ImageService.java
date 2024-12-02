package com.example.board.service;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${file.upload.location}")
    private String uploadPath;

    public String saveFile(MultipartFile file) throws FileUploadException {
        if (file.isEmpty()){
            throw new IllegalArgumentException("Failed to store empty file");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String storedFileName = createStoredFileName(originalFilename);

            Path destinationFile = Paths.get(uploadPath)
                    .resolve(Paths.get(storedFileName))
                    .normalize().toAbsolutePath();

            //save file
            file.transferTo(destinationFile);

            return storedFileName;
        } catch (IOException e){
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


}

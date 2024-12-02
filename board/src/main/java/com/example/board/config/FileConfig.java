package com.example.board.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileConfig {
    @Value("${file.upload.location}")
    private String fileUploadLocation;

    @PostConstruct
    public void init(){
        try {
            Files.createDirectories(Paths.get(fileUploadLocation));
        }catch (IOException e){
            throw new RuntimeException("Could not create upload directory");
        }
    }

}

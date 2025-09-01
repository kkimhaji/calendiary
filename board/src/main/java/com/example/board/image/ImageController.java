package com.example.board.image;

import com.example.board.image.dto.ImageConfirmRequest;
import com.example.board.image.dto.ImageDeleteRequest;
import com.example.board.image.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/temp-upload")
    public ResponseEntity<ImageUploadResponse> uploadTempImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("domain") String domain) {
        try {
            ImageDomain imageDomain = ImageDomain.valueOf(domain.toUpperCase());
            String imageUrl = imageService.uploadTempImage(file, imageDomain);

            return ResponseEntity.ok(new ImageUploadResponse(imageUrl, true, "Upload successful"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageUploadResponse(null, false, "Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmImage(@RequestBody ImageConfirmRequest request) {
        try {
            ImageDomain domain = ImageDomain.valueOf(request.domain().toUpperCase());
            imageService.moveToPermanent(request.tempUrl(), domain);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestBody ImageDeleteRequest request) {
        try {
            ImageDomain domain = ImageDomain.valueOf(request.domain().toUpperCase());
            imageService.deleteImage(request.fileName(), domain);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

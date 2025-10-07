package com.example.board.image;

import com.example.board.image.dto.ImageConfirmRequest;
import com.example.board.image.dto.ImageDeleteRequest;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> uploadTempImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("domain") String domain) throws IOException {

        ImageDomain imageDomain = ImageDomain.valueOf(domain.toUpperCase());
        String imageUrl = imageService.uploadTempImage(file, imageDomain);

        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmImage(@RequestBody ImageConfirmRequest request) throws IOException {
        ImageDomain domain = ImageDomain.valueOf(request.domain().toUpperCase());
        imageService.moveToPermanent(request.tempUrl(), domain);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestBody ImageDeleteRequest request) {
        ImageDomain domain = ImageDomain.valueOf(request.domain().toUpperCase());
        imageService.deleteImage(request.fileName(), domain);
        return ResponseEntity.ok().build();
    }
}
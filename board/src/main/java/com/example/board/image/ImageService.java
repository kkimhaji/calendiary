package com.example.board.image;

import com.example.board.common.exception.FileDeleteException;
import com.example.board.common.exception.InvalidFileTypeException;
import com.example.board.post.Post;
import com.example.board.post.PostImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private static final Pattern SRC_PATTERN = Pattern.compile("src=\"(.*?)\"");
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    // 게시판 경로
    @Value("${file.post.upload.location}")
    private String postUploadDir;
    @Value("${file.post.upload.temp}")
    private String postTempDir;
    // 일기 경로
    @Value("${file.diary.upload.location}")
    private String diaryUploadDir;
    @Value("${file.diary.upload.temp}")
    private String diaryTempDir;

    public String saveFile(MultipartFile file, ImageDomain domain) throws FileUploadException {
        validate(file.getOriginalFilename());

        String storedName = uuid(file.getOriginalFilename());
        Path dest = Paths.get(resolveUploadDir(domain), storedName)
                .toAbsolutePath().normalize();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file", e);
        }

        return storedName;   // DB에는 파일명만 보관
    }

    public String uploadTempImage(MultipartFile file, ImageDomain domain) throws IOException {
        validate(file.getOriginalFilename());

        String storedName = uuid(file.getOriginalFilename());
        Path tempPath = Paths.get(resolveTempDir(domain), storedName);

        Files.createDirectories(tempPath.getParent());
        Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        return domain.tempPrefix() + storedName;   // 프론트에 돌려줄 임시 URL
    }

    public String processContentImages(String content, ImageDomain domain) throws IOException {
        if (content == null || content.isEmpty()) {
            return content;
        }

        List<String> imageUrls = extractImageUrlsFromContent(content);

        for (String imageUrl : imageUrls) {
            // 전체 URL에서 경로 추출
            String pathOnly = extractPathFromUrl(imageUrl);
//            임시 이미지 경로인지 확인
            if (pathOnly.startsWith(domain.tempPrefix())) {

                try {
                    // 영구 저장소로 이동
                    String permPath = moveToPermanent(pathOnly, domain);
                    // content에서 모든 형태의 URL 교체
                    content = content.replace(imageUrl, permPath);

                } catch (IOException e) {
                    log.error("❌ 이미지 이동 실패: {}", pathOnly, e);
                    // 실패해도 계속 진행 (다른 이미지 처리)
                }
            } else {
                log.debug("임시 이미지 아님, 건너뜀: {}", pathOnly);
            }
        }
        return content;
    }

    public String moveToPermanent(String tempPath, ImageDomain domain) throws IOException {
        // 파일명 추출
        String fileName = tempPath.substring(tempPath.lastIndexOf('/') + 1);
        Path source = Paths.get(resolveTempDir(domain), fileName);
        Path target = Paths.get(resolveUploadDir(domain), fileName);

        if (!Files.exists(source)) {
            log.error("❌ 원본 파일이 존재하지 않음: {}", source);
            throw new IOException("임시 파일을 찾을 수 없습니다: " + fileName);
        }

        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

        // 영구 경로 반환 (상대 경로)
        return domain.permPrefix() + fileName;
    }

    public void deleteImage(String storedName, ImageDomain domain) {
        Path path = Paths.get(resolveUploadDir(domain), storedName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Delete failed: {}", storedName, e);
            throw new FileDeleteException("파일 삭제 중 오류: " + storedName, e);
        }
    }

    public void deleteAllPostImages(Post post) throws IOException {
        for (PostImage img : post.getImages()) deleteImage(img.getStoredFileName(), ImageDomain.POST);
        post.clearImages();
    }

    private String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 이미 상대 경로면 그대로 반환
        if (url.startsWith("/")) {
            return url;
        }

        // 전체 URL이면 경로만 추출
        // http://localhost:8080/post-temp-images/xxx.png
        // → /post-temp-images/xxx.png
        try {
            // protocol://host/path 형태에서 path 추출
            int protocolEnd = url.indexOf("://");
            if (protocolEnd != -1) {
                int pathStart = url.indexOf("/", protocolEnd + 3);
                if (pathStart != -1) {
                    return url.substring(pathStart);
                }
            }
        } catch (Exception e) {
            log.warn("URL 파싱 실패, 원본 반환: {}", url, e);
        }

        return url;
    }

    public List<String> extractImageUrlsFromContent(String html) {
        List<String> list = new ArrayList<>();
        Matcher m = SRC_PATTERN.matcher(html);
        while (m.find()) list.add(m.group(1));
        return list;
    }

    private String uuid(String original) {
        String ext = original.substring(original.lastIndexOf('.') + 1);
        return UUID.randomUUID() + "." + ext;
    }

    private void validate(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) throw new InvalidFileTypeException("Invalid type: " + ext);
    }

    private String resolveUploadDir(ImageDomain d) {
        return d == ImageDomain.POST ? postUploadDir : diaryUploadDir;
    }

    private String resolveTempDir(ImageDomain d) {
        return d == ImageDomain.POST ? postTempDir : diaryTempDir;
    }
}

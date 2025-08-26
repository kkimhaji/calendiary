package com.example.board.image;

import com.example.board.diary.Diary;
import com.example.board.diary.DiaryImage;
import com.example.board.post.Post;
import com.example.board.post.PostImage;
import com.example.board.post.dto.ImageResponse;
import com.example.board.common.exception.FileDeleteException;
import com.example.board.common.exception.InvalidFileTypeException;
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
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    // 게시판 경로
    @Value("${file.post.upload.location}") private String postUploadDir;
    @Value("${file.post.upload.temp}")     private String postTempDir;

    // 일기 경로
    @Value("${file.diary.upload.location}") private String diaryUploadDir;
    @Value("${file.diary.upload.temp}")     private String diaryTempDir;

    public String saveFile(MultipartFile file, ImageDomain domain) throws FileUploadException {
        validate(file.getOriginalFilename());

        String storedName = uuid(file.getOriginalFilename());
        Path dest = Paths.get(resolveUploadDir(domain), storedName)
                .toAbsolutePath().normalize();

        try { file.transferTo(dest); }
        catch (IOException e) { throw new FileUploadException("Failed to store file", e); }

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
        for (String tempUrl : extractImageUrlsFromContent(content)) {
            if (tempUrl.startsWith(domain.tempPrefix())) {
                String permUrl = moveToPermanent(tempUrl, domain);
                content = content.replace(tempUrl, permUrl);
            }
        }
        return content;
    }

    public String moveToPermanent(String tempUrl, ImageDomain domain) throws IOException {
        String file = tempUrl.substring(tempUrl.lastIndexOf('/') + 1);

        Path source = Paths.get(resolveTempDir(domain), file);
        Path target = Paths.get(resolveUploadDir(domain), file);

        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return tempUrl.replace(domain.tempPrefix(), domain.permPrefix());
    }

    public void deleteImage(String storedName, ImageDomain domain) {
        Path path = Paths.get(resolveUploadDir(domain), storedName);
        try {
            Files.deleteIfExists(path);
            log.info("Deleted file: {}", storedName);
        } catch (IOException e) {
            log.error("Delete failed: {}", storedName, e);
            throw new FileDeleteException("파일 삭제 중 오류: " + storedName, e);
        }
    }

    public void deleteAllPostImages(Post post) throws IOException {
        for (PostImage img : post.getImages()) deleteImage(img.getStoredFileName(), ImageDomain.POST);
        post.clearImages();
    }

    private static final Pattern SRC_PATTERN = Pattern.compile("src=\"(.*?)\"");
    public List<String> extractImageUrlsFromContent(String html) {
        List<String> list = new ArrayList<>();
        Matcher m = SRC_PATTERN.matcher(html);
        while (m.find()) list.add(m.group(1));
        return list;
    }

    private String uuid(String original){
        String ext = original.substring(original.lastIndexOf('.')+1);
        return UUID.randomUUID() + "." + ext;
    }

    private void validate(String filename){
        String ext = filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
        if(!ALLOWED_EXTENSIONS.contains(ext)) throw new InvalidFileTypeException("Invalid type: "+ext);
    }

    private String resolveUploadDir(ImageDomain d){ return d==ImageDomain.POST ? postUploadDir  : diaryUploadDir; }
    private String resolveTempDir  (ImageDomain d){ return d==ImageDomain.POST ? postTempDir    : diaryTempDir; }
}

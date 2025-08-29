package com.example.board.common.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
@Slf4j
public class TempFileCleanScheduler {

    @Value("${file.post.upload.temp}")
    private String postTempDirPath;

    @Value("${file.diary.upload.temp}")
    private String diaryTempDirPath;

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 새벽 3시 실행
    public void cleanTempFiles() {
        cleanTempDirectory(postTempDirPath, "Post");
        cleanTempDirectory(diaryTempDirPath, "Diary");
    }

    private void cleanTempDirectory(String tempDirPath, String domain) {
        File tempDir = new File(tempDirPath);

        if (!tempDir.exists()) {
            log.debug("{} temp directory does not exist: {}", domain, tempDirPath);
            return;
        }

        File[] files = tempDir.listFiles();
        if (files == null) {
            log.warn("Cannot list files in {} temp directory: {}", domain, tempDirPath);
            return;
        }

        long deletedCount = Arrays.stream(files)
                .filter(file -> !file.isDirectory())
                .filter(file -> System.currentTimeMillis() - file.lastModified() > 86_400_000)  // 24시간 지난 파일
                .peek(file -> log.debug("Deleting old temp file: {}", file.getName()))
                .mapToLong(file -> file.delete() ? 1 : 0)
                .sum();

        if (deletedCount > 0) {
            log.info("Cleaned {} old temp files from {} directory: {}", deletedCount, domain, tempDirPath);
        }
    }
}

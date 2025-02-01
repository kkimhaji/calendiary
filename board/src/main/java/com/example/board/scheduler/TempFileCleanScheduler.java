package com.example.board.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
public class TempFileCleanScheduler {
    @Value("${file.upload.temp}")
    private String tempDirPath;

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 새벽 3시 실행
    public void cleanTempFiles() {
        File tempDir = new File(tempDirPath);
        if (tempDir.exists()) {
            Arrays.stream(tempDir.listFiles())
                    .filter(file -> !file.isDirectory())
                    .filter(file -> System.currentTimeMillis() - file.lastModified() > 86_400_000)  // 24시간 지난 파일
                    .forEach(File::delete);
        }
    }
}

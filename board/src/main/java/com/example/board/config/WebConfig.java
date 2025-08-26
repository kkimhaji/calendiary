package com.example.board.config;

import com.example.board.permission.utils.PermissionToStringConverter;
import com.example.board.permission.utils.StringToPermissionConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.post.upload.location}")  private String postUploadDir;
    @Value("${file.post.upload.temp}")      private String postTempDir;
    @Value("${file.diary.upload.location}") private String diaryUploadDir;
    @Value("${file.diary.upload.temp}")     private String diaryTempDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /* 게시판 이미지 */
        registry.addResourceHandler("/post-images/**")
                .addResourceLocations("file:" + postUploadDir + "/");

        registry.addResourceHandler("/post-temp-images/**")
                .addResourceLocations("file:" + postTempDir + "/")
                .setCachePeriod(0);

        /* 일기 이미지 */
        registry.addResourceHandler("/diary-images/**")
                .addResourceLocations("file:" + diaryUploadDir + "/");

        registry.addResourceHandler("/diary-temp-images/**")
                .addResourceLocations("file:" + diaryTempDir + "/")
                .setCachePeriod(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToPermissionConverter());
        registry.addConverter(new PermissionToStringConverter());
    }
}

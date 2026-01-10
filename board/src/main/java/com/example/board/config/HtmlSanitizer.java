package com.example.board.config;

import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class HtmlSanitizer {
    private final PolicyFactory policyFactory;

    public HtmlSanitizer() {
        Pattern imageUrlPattern = Pattern.compile(
                "^(https?://[^\\s]+|/[^\\s]+|data:image/[^\\s]+)$",
                Pattern.CASE_INSENSITIVE
        );

        policyFactory = new HtmlPolicyBuilder()
                // CKEditor에서 사용하는 모든 태그 허용
                .allowElements(
                        // 텍스트 관련
                        "p", "div", "span", "br",
                        // 제목
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        // 리스트
                        "ul", "ol", "li",
                        // 텍스트 서식
                        "strong", "em", "u", "s", "b", "i",
                        // 블록
                        "blockquote", "pre", "code",
                        // 링크
                        "a",
                        // 이미지
                        "img", "figure", "figcaption",
                        // 테이블
                        "table", "thead", "tbody", "tr", "th", "td",
                        // 기타
                        "hr"
                )

                // 이미지 태그 속성 (src에 http/https 허용)
                .allowAttributes("src")
                .matching(imageUrlPattern)  // URL 패턴 검증
                .onElements("img")

                .allowAttributes("alt", "title", "width", "height")
                .onElements("img")

                // 이미지 스타일 속성 허용 (CKEditor 이미지 정렬/크기)
                .allowAttributes("style")
                .matching(Pattern.compile(
                        "^(width|height|float|margin|display):\\s*[^;]+;?(\\s*(width|height|float|margin|display):\\s*[^;]+;?)*$",
                        Pattern.CASE_INSENSITIVE
                ))
                .onElements("img")

                // figure 태그 (CKEditor 이미지 컨테이너)
                .allowAttributes("class", "style")
                .onElements("figure", "figcaption")

                // 일반 스타일 속성
                .allowAttributes("class")
                .onElements("p", "div", "span", "ul", "ol", "li", "blockquote", "table", "tr", "td", "th")

                // 링크 속성
                .allowAttributes("href", "title", "target")
                .onElements("a")

                // 허용된 프로토콜 명시 (중요!)
                .allowUrlProtocols("http", "https")

                // 표준 프로토콜 허용
                .allowStandardUrlProtocols()

                .toFactory();
    }

    public String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        String sanitized = policyFactory.sanitize(html);

        return sanitized;
    }
}

package com.example.board.config;

import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HtmlSanitizer {
    private final PolicyFactory policyFactory;

    public HtmlSanitizer() {
        policyFactory = new HtmlPolicyBuilder()
                .allowElements("p", "div", "h1", "h2", "h3", "span", "img")
                // img 태그의 경우 src, alt 속성만 허용
                .allowAttributes("src", "alt").onElements("img")
                // 스타일 속성 제한적 허용
                .allowAttributes("class").onElements("p", "div", "span")
                .toFactory();
    }

    public String sanitize(String html){
        return policyFactory.sanitize(html);
    }
}

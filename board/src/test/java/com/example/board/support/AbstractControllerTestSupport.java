package com.example.board.support;

import com.example.board.domain.member.Member;
import com.example.board.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

//@WebMvcTest
@AutoConfigureMockMvc
public abstract class AbstractControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TestDataFactory testDataFactory;

    @MockBean
    protected MemberService memberService;
    protected Member member1;
    protected Member member2;

    @BeforeEach
    public void setUp(){
        member1 = testDataFactory.createMember("test1@test.com", "test1", "1234");
        member2 = testDataFactory.createMember("test2@test.com", "test2", "1234");
    }

}

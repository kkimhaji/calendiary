package com.example.board.support;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.member.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
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
    protected UserPrincipal principal1;
    protected UserPrincipal principal2;

    @BeforeEach
    public void setUp() {
        member1 = testDataFactory.createMember("test1@test.com", "test1", "1234");
        member2 = testDataFactory.createMember("test2@test.com", "test2", "1234");

        principal1 = new UserPrincipal(member1);
        principal2 = new UserPrincipal(member2);
    }

}

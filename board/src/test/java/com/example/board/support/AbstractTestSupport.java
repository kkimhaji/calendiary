package com.example.board.support;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.team.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public abstract class AbstractTestSupport {

    @Autowired
    protected TestDataFactory testDataFactory;
    @Autowired
    protected MemberRepository memberRepository;

    protected Member member1;
    protected Member member2;

    @BeforeEach
    public void setUp(){
        member1 = testDataFactory.createMember("test1@test.com", "test1", "1234");
        member2 = testDataFactory.createMember("test2@test.com", "test2", "1234");
    }

    @AfterEach
    void cleanUp(){
        memberRepository.deleteAll();
    }

}

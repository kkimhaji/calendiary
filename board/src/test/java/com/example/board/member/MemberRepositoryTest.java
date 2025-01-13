package com.example.board.member;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp(){
        entityManager.clear();
    }

    @Test
    @DisplayName("멤버 생성 테스트")
    void memberCreateTest(){
        //given
        Member member1 = Member.builder()
                .email("aaa").password("123456").nickname("hello")
                .build();
        //when
        Member result1 = memberRepository.save(member1);
        entityManager.flush();
        //then
        assertThat(result1.getEmail()).isEqualTo(member1.getEmail());
    }

    @Test
    @DisplayName("멤버 삭제 테스트")
    void memberDeleteTest(){
        //given
        Member member1 = Member.builder().email("aaa").password("123456").nickname("hello").build();
        //when
        Member result1 = memberRepository.save(member1);
        memberRepository.delete(result1);

        //then
//        assertThatThrownBy(()->memberRepository.findById(member1.getMemberId())).isInstanceOf(IllegalArgumentException.class);
        assertThat(memberRepository.findById(member1.getMemberId())).isEmpty();
    }



}

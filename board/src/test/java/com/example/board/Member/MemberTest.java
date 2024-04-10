package com.example.board.Member;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockitoExtension.class)
public class MemberTest {
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("멤버 생성 테스트")
    void memberCreateTest(){
        //given
        Member member1 = Member.builder().email("aaa").password("123456").nickname("hello").build();
        //when
        Member result1 = memberRepository.save(member1);

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

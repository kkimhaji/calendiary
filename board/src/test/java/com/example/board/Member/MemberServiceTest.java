package com.example.board.Member;

import com.example.board.domain.member.Member;
import com.example.board.service.MemberService;
import com.example.board.support.AbstractTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan("com.example.board")
@Transactional
public class MemberServiceTest extends AbstractTestSupport {
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void newPwdTest(){
        String newPwd = "abcd";
        memberService.updatePassword(member1, newPwd);
        Member updatedMember = memberRepository.findById(member1.getMemberId()).get();
        String updatedPwd = updatedMember.getPassword();
        assertThat(passwordEncoder.matches(newPwd, updatedPwd)).isTrue();
    }

    @Test
    void updateNameTest(){
        String newName = "update Test";
        memberService.updateMemberName(member1, newName);
        Member updatedMember = memberRepository.findById(member1.getMemberId()).get();
        assertThat(updatedMember.getNickname()).isEqualTo(newName);
    }
}

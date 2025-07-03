package com.example.board.member;

import com.example.board.support.AbstractTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberServiceTest extends AbstractTestSupport {
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void updateNameTest() {
        String newName = "update Test";
        memberService.updateMemberName(member1, newName);
        Member updatedMember = memberRepository.findById(member1.getMemberId()).get();
        assertThat(updatedMember.getNickname()).isEqualTo(newName);
    }
}

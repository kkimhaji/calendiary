package com.example.board.member;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.dto.MemberInfoResponse;
import com.example.board.member.dto.MemberInfoSummaryResponse;
import com.example.board.member.dto.PasswordResetRequest;
import com.example.board.support.AbstractTestSupport;
import com.example.board.teamMember.EmailService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    PasswordEncoder passwordEncoder;
    private Member member;
    @Autowired
    private MemberRepository memberRepository;
    @MockBean // 실제 이메일 발송 대신 Mock 처리
    private EmailService emailService;

    @BeforeEach
    void init() {
        member = memberRepository.save(Member.createMember("test@example.com", "oldNick", passwordEncoder.encode("oldPwd"), true, null, null));
    }

    @Test
    void updateNameTest() {
        String newName = "update Test";
        memberService.updateMemberName(member, newName);
        assertThat(member.getNickname()).isEqualTo(newName);
    }

    @Test
    @DisplayName("비밀번호 확인 - 일치")
    void checkPassword_match() {
        boolean match = memberService.checkPassword(member, "oldPwd");
        assertThat(match).isTrue();
    }

    @Test
    @DisplayName("비밀번호 확인 - 불일치")
    void checkPassword_notMatch() {
        boolean match = memberService.checkPassword(member, "wrongPwd");
        assertThat(match).isFalse();
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_success() {
        // given
        String newPassword = "newPwd123";

        // when
        memberService.updatePassword(member, newPassword);

        // then
        Member updated = memberRepository.findById(member.getMemberId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("임시 비밀번호 발급 성공 (이메일 발송 Mock)")
    void issueTempPassword_success() {
        // given
        PasswordResetRequest request = new PasswordResetRequest(member.getEmail());
        given(emailService.generateRandomCode()).willReturn("TEMP1234");

        // when
        memberService.issueTempPassword(request);

        // then
        Member updated = memberRepository.findById(member.getMemberId()).orElseThrow();
        assertThat(passwordEncoder.matches("TEMP1234", updated.getPassword())).isTrue();
        then(emailService).should().sendTempPasswordEmail(updated, "TEMP1234");
    }
    @Test
    @DisplayName("임시 비밀번호 발급 실패 - 존재하지 않는 이메일")
    void issueTempPassword_emailNotFound() {
        PasswordResetRequest request = new PasswordResetRequest("notfound@example.com");

        assertThatThrownBy(() -> memberService.issueTempPassword(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("member with the email not found");
    }

    @Test
    @DisplayName("간략 회원 정보 조회")
    void getMemberInfoSummary_success() {
        UserPrincipal principal = new UserPrincipal(member);

        MemberInfoSummaryResponse response = memberService.getMemberInfoSummary(principal);

        assertThat(response.id()).isEqualTo(member.getMemberId());
        assertThat(response.nickname()).isEqualTo(member.getNickname());
    }


    @Test
    @DisplayName("계정 페이지 정보 조회")
    void getInfoForAccountPage_success() {
        UserPrincipal principal = new UserPrincipal(member);

        MemberInfoResponse response = memberService.getInfoForAccountPage(principal);

        assertThat(response.memberId()).isEqualTo(member.getMemberId());
        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.nickname()).isEqualTo(member.getNickname());
    }
}

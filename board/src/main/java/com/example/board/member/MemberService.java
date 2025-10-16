package com.example.board.member;

import com.example.board.auth.UserPrincipal;
import com.example.board.common.exception.MemberNotFoundException;
import com.example.board.common.service.EntityValidationService;
import com.example.board.member.dto.MemberInfoResponse;
import com.example.board.member.dto.MemberInfoSummaryResponse;
import com.example.board.member.dto.PasswordResetRequest;
import com.example.board.teamMember.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final EntityValidationService validationService;
    private final MemberDeletionService memberDeletionService;

    public String updateMemberName(Member member, String newNickname) {
        Member targetMember = validationService.validateMemberExists(member.getMemberId());
        targetMember.updateName(newNickname);
        return newNickname;
    }

    @Transactional(readOnly = true)
    //비밀번호 변경 시 먼저 현재 사용자의 비밀번호 확인 후 비밀번호 변경 기능으로 넘어감
    public boolean checkPassword(Member member, String currentPwd) {
        return passwordEncoder.matches(currentPwd, member.getPassword());
    }

    public void updatePassword(Member member, String newPassword) {
        String encodedPwd = passwordEncoder.encode(newPassword);
        member.updatePassword(encodedPwd);
        memberRepository.save(member);
    }

    //임시 비밀번호 발급
    public void issueTempPassword(PasswordResetRequest request) {
        String tmpPwd = emailService.generateRandomCode();
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new MemberNotFoundException("member with the email not found"));
        updatePassword(member, tmpPwd);
        emailService.sendTempPasswordEmail(member, tmpPwd);
    }

    @Transactional(readOnly = true)
    //헤더에서 닉네임 표시 및 계정 정보 페이지 이동을 위해
    public MemberInfoSummaryResponse getMemberInfoSummary(UserPrincipal userPrincipal) {
        Member member = userPrincipal.getMember();
        return new MemberInfoSummaryResponse(member.getMemberId(), member.getNickname());
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getInfoForAccountPage(UserPrincipal principal) {
        Member member = principal.getMember();
        return new MemberInfoResponse(member.getMemberId(), member.getEmail(), member.getNickname());
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deleteMember(Member member, String password) {
        memberDeletionService.deleteMember(member, password, passwordEncoder);
    }
}

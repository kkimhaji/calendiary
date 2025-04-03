package com.example.board.service;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.dto.member.MemberInfoSummaryResponse;
import com.example.board.dto.member.PasswordChangeRequest;
import com.example.board.dto.member.PasswordResetRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.action.internal.EntityActionVetoException;
import org.springframework.security.access.AccessDeniedException;
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

    public void updateMemberName(Member member, String newNickname){
        member.updateName(newNickname);
    }

    //비밀번호 변경 시 먼저 현재 사용자의 비밀번호 확인 후 비밀번호 변경 기능으로 넘어감
    public void checkPassword(Member member, String currentPwd){
        if (passwordEncoder.encode(currentPwd).equals(member.getPassword())){
            throw new AccessDeniedException("비밀번호가 일치하지 않습니다.");
        }
    }

    public void updatePassword(Member member, String newPassword){
        String encodedPwd = passwordEncoder.encode(newPassword);
        member.updatePassword(encodedPwd);
        memberRepository.save(member);
    }

    //임시 비밀번호 발급
    public void issueTempPassword(PasswordResetRequest request){
        String tmpPwd = emailService.generateRandomCode();
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new EntityNotFoundException("member with the email not found"));
        updatePassword(member, tmpPwd);
        emailService.sendTempPasswordEmail(member, tmpPwd);
    }

    //헤더에서 닉네임 표시 및 계정 정보 페이지 이동을 위해
    public MemberInfoSummaryResponse getMemberInfoSummary(UserPrincipal userPrincipal){
        Member member = userPrincipal.getMember();
        return new MemberInfoSummaryResponse(member.getMemberId(), member.getNickname());
    }
}

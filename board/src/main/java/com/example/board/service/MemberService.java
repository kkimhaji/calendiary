package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
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
    }

    //임시 비밀번호 발급
    public void issueTempPassword(Member member){
        String tmpPwd = emailService.generateRandomCode();
        member.updatePassword(passwordEncoder.encode(tmpPwd));
        memberRepository.save(member);
        emailService.sendTempPasswordEmail(member, tmpPwd);
    }
}

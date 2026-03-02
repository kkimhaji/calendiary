package com.example.board.teamMember;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8은 Java 표준에서 항상 지원되므로 실질적으로 도달하지 않는 분기
            throw new MessagingException("Failed to encode email address: " + fromName, e);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);
    }

    public void sendVerificationEmail(Member member) {
        String subject = "Account Verification";
        String verificationCode = member.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            sendEmail(member.getEmail(), subject, htmlMessage);
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

    public void resendVerificationCode(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isPresent()){
            Member member = optionalMember.get();
            if (member.isEnabled())
                throw new RuntimeException("Account is already verified");

            member.setVerification(generateRandomCode(), LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(member);
            memberRepository.save(member);
        }else throw new RuntimeException("user not found");
    }

    public String generateRandomCode(){
        Random random = new Random();
        int code = random.nextInt(90000) + 10000;
        return String.valueOf(code);
    }

    public void sendTempPasswordEmail(Member member, String tmpPwd) {
        String subject = "Your Temporary Password";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">This is Calendiary!</h2>"
                + "<p style=\"font-size: 16px;\">This is your temporary password:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Your temp password:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + tmpPwd + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            sendEmail(member.getEmail(), subject, htmlMessage);
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

}

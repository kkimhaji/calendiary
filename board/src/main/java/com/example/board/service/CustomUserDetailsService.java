package com.example.board.service;

import com.example.board.domain.member.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userPK) throws UsernameNotFoundException {
        return memberRepository.findById(Long.valueOf(userPK))
                .orElseThrow(()->new UsernameNotFoundException(userPK + "-> 데이터베이스에서 찾을 수 없습니다."));
    }

//    private User createUser(String username, Member user){
//        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
//                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName())).collect(Collectors.toList());
//
//        return new Member(user.getEmail(), user.getPassword(), grantedAuthorities);
//    }
}

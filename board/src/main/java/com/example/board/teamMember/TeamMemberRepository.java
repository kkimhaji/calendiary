package com.example.board.teamMember;

import com.example.board.member.Member;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.team.dto.TeamInfoResponse;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.example.board.teamMember.dto.TeamMemberDTO;
import com.example.board.teamMember.dto.TeamMemberInfo;
import com.example.board.teamMember.dto.TeamMemberInfoListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findAllByTeamId(Long teamId);

    Optional<TeamMember> findByTeamAndMember(Team team, Member member);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.member m " +
            "JOIN FETCH tm.role r " +
            "WHERE tm.team.id = :teamId")
    List<TeamMember> findAllWithDetailsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.role " +
            "WHERE tm.team.id = :teamId AND tm.member = :member")
    Optional<TeamMember> findByTeamIdAndMember(@Param("teamId") Long teamId, @Param("member") Member member);

    @Query("SELECT tm FROM TeamMember tm " +
            "WHERE tm.team = :team AND tm.role = :role")
    List<TeamMember> findAllByTeamAndRole(
            @Param("team") Team team,
            @Param("role") TeamRole role
    );

    @Query("SELECT tm FROM TeamMember tm " +
            "WHERE tm.team = :team AND tm.member.id IN :memberIds")
    List<TeamMember> findAllByTeamAndMemberIdIn(
            @Param("team") Team team,
            @Param("memberIds") List<Long> memberIds
    );

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "JOIN FETCH tm.member m " +
            "JOIN FETCH tm.role r " +
            "WHERE t.id = :teamId AND m.id = :memberId")
    Optional<TeamMember> findByTeamIdAndMemberId(@Param("teamId") Long teamId, @Param("memberId") Long memberId);

    @Query("SELECT new com.example.board.team.dto.TeamListDTO(t.id, t.name) " +
            "FROM Team t " +
            "JOIN TeamMember tm ON t = tm.team " +
            "WHERE tm.member.id = :memberId")
    List<TeamListDTO> findTeamListByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT new com.example.board.teamMember.dto.TeamMemberDTO(tm.id, tm.teamNickname) " +
            "FROM TeamMember tm " +
            "WHERE tm.role.id = :roleId")
    List<TeamMemberDTO> findMembersByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT new com.example.board.teamMember.dto.TeamMemberInfoListDTO(" +
            "m.email, tm.teamNickname, r.roleName, r.id) " +
            "FROM TeamMember tm " +
            "JOIN tm.member m " +
            "JOIN tm.role r " +
            "WHERE tm.team.id = :teamId")
    List<TeamMemberInfoListDTO> findMembersByTeamId(@Param("teamId") Long teamId);

    //검색 + 페이징 처리
    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.member m " +
            "JOIN FETCH tm.role r " +
            "WHERE tm.team.id = :teamId " +
            "AND (m.email LIKE %:keyword% OR tm.teamNickname LIKE %:keyword%)")
    Page<TeamMember> findAllWithDetailsByTeamId(
            @Param("teamId") Long teamId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 역할 기반 멤버 검색 + 페이징 쿼리
    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.member m " +
            "JOIN FETCH tm.role r " +
            "WHERE tm.role.id = :roleId " + // 역할 ID 조건 추가
            "AND (m.email LIKE %:keyword% OR tm.teamNickname LIKE %:keyword%)")
    // 검색 조건
    Page<TeamMember> findByRoleId(
            @Param("roleId") Long roleId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT tm.team.id FROM TeamMember tm WHERE tm.member.id = :memberId")
    List<Long> findTeamIdsByMemberId(@Param("memberId") Long memberId);

    // 멤버 ID로 팀 정보 및 닉네임 조회
    @Query("SELECT new com.example.board.team.dto.TeamInfoResponse(tm.team.id, tm.team.name, tm.teamNickname) " +
            "FROM TeamMember tm WHERE tm.member.id = :memberId")
    List<TeamInfoResponse> findTeamInfoAndNicknameByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT new com.example.board.teamMember.dto.TeamMemberInfo(tm.teamNickname, r.roleName, tm.joinedAt) " +
            "FROM TeamMember tm " +
            "JOIN tm.role r " +
            "WHERE tm.team.id = :teamId AND tm.member.id = :memberId")
    Optional<TeamMemberInfo> findTeamMemberInfoFromTeamIdAndMemberId(
            @Param("teamId") Long teamId,
            @Param("memberId") Long memberId
    );

    //팀 탈퇴 시 마지막으로 남은 관리자인지 확인
    int countByTeamIdAndRoleId(Long teamId, Long roleId);

    @Query("SELECT new com.example.board.teamMember.dto.MemberProfileResponse(" +
            "m.email, tm.teamNickname, r.roleName, tm.joinedAt) " +
            "FROM TeamMember tm " +
            "JOIN tm.member m " +
            "JOIN tm.role r " +
            "WHERE tm.id = :teamMemberId")
    Optional<MemberProfileResponse> findMemberProfileByTeamMemberId(
            @Param("teamMemberId") Long teamMemberId);

    boolean existsByTeamAndMember(Team team, Member member);

    boolean existsByTeamAndTeamNickname(Team team, String teamNickname);
}

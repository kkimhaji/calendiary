package com.example.board.domain.teamMember;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.dto.member.TeamMemberDTO;
import com.example.board.dto.member.TeamMemberInfoListDTO;
import com.example.board.dto.team.TeamListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<Team> findAllTeamByMember(Member member);
    List<Member> findAllMemberByTeam(Team team);
    List<TeamMember> findAllByTeam(Team team);
    Optional<TeamMember> findByTeamAndMember(Team team, Member member);

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

    @Query("SELECT new com.example.board.dto.team.TeamListDTO(t.id, t.name) " +
            "FROM Team t " +
            "JOIN TeamMember tm ON t = tm.team " +
            "WHERE tm.member.id = :memberId")
    List<TeamListDTO> findTeamListByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT new com.example.board.dto.member.TeamMemberDTO(tm.id, tm.teamNickname) " +
            "FROM TeamMember tm " +
            "WHERE tm.role.id = :roleId")
    List<TeamMemberDTO> findMembersByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT new com.example.dto.member.TeamMemberInfoListDTO(" +
            "m.email, tm.teamNickname, r.roleName, r.id) " + // ✅ r.id 추가
            "FROM TeamMember tm " +
            "JOIN tm.member m " +
            "JOIN tm.role r " +
            "WHERE tm.team.id = :teamId")
    List<TeamMemberInfoListDTO> findMembersByTeamId(@Param("teamId") Long teamId);

}

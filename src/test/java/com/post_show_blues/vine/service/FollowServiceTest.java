package com.post_show_blues.vine.service;

import com.post_show_blues.vine.domain.follow.Follow;
import com.post_show_blues.vine.domain.follow.FollowRepository;
import com.post_show_blues.vine.domain.member.Member;
import com.post_show_blues.vine.domain.memberimg.MemberImgRepository;
import com.post_show_blues.vine.domain.notice.Notice;
import com.post_show_blues.vine.domain.notice.NoticeRepository;
import com.post_show_blues.vine.dto.follow.FollowMemberResultDTO;
import com.post_show_blues.vine.dto.follow.FollowerMemberResultDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@Log4j2
@SpringBootTest
@Transactional
public class FollowServiceTest {

    @Autowired
    FollowService followService;
    @Autowired
    FollowRepository followRepository;
    @Autowired
    NoticeRepository noticeRepository;
    @Autowired
    AuthService authService;
    @Autowired
    MemberImgRepository memberImgRepository;

    @Test
    public void 팔로우() throws Exception {
        //given
        Member memberA = memberA();
        Member memberB = memberB();

        followService.isFollow(memberA.getId(), memberB.getId());

        //when
        List<Follow> followList = followRepository.findAll();
        Follow follow = followList.get(followList.size() - 1);

        //then
        assertThat(follow.getFromMemberId().getId()).isEqualTo(memberA.getId());
        assertThat(follow.getToMemberId().getId()).isEqualTo(memberB.getId());
    }

    @Test
    public void 알람테이블() throws Exception {
        //given
        Member memberA = memberA();
        Member memberB = memberB();
        followService.isFollow(memberA.getId(), memberB.getId());

        //when
        List<Notice> noticeList = noticeRepository.findAll();
        Notice notice = noticeList.get(noticeList.size() - 1);

        //then
        assertThat(notice.getMemberId()).isEqualTo(memberB.getId());

    }


    @Test
    public void 언팔로우() throws Exception {
        //given
        Member memberA = memberA();
        Member memberB = memberB();
        followService.isFollow(memberA.getId(), memberB.getId());

        List<Follow> followList = followRepository.findAll();
        Follow follow = followList.get(followList.size() - 1);

        //when
        followService.isUnFollow(memberA.getId(), memberB.getId());
        List<Follow> unFollow = followRepository.findAll();

        //then
        assertThat(unFollow.contains(follow)).isEqualTo(false);
    }

    @Test
    public void 팔로우_목록() throws Exception {
        //given
        //a가 b, c를 팔로우
        //a의 팔로우 목록 b, c
        Member memberA = memberA();
        Member memberB = memberB();
        Member memberC = memberC();
        followService.isFollow(memberA.getId(), memberB.getId()); //a가 b 팔로우
        followService.isFollow(memberA.getId(), memberC.getId()); //a가 c 팔로우

        //when
        List<FollowMemberResultDTO> followMembers = followService.followMember(memberA.getId());


        boolean isMemberB = followMembers.stream().anyMatch(m -> m.getId().equals(memberB.getId()));
        boolean isMemberC = followMembers.stream().anyMatch(m -> m.getId().equals(memberC.getId()));

        //then
        assertThat(isMemberB).isEqualTo(true);
        assertThat(isMemberC).isEqualTo(true);



    }

    @Test
    public void 팔로워_목록() throws Exception {
        //given
        //b, c가 a를 팔로우
        //a의 팔로워 b, c
        Member memberA = memberA();
        Member memberB = memberB();
        Member memberC = memberC();
        followService.isFollow(memberB.getId(), memberA.getId()); //b가 a 팔로우
        followService.isFollow(memberC.getId(), memberA.getId()); //c가 a 팔로우

        //when
        List<FollowerMemberResultDTO> followerMembers = followService.followerMember(memberA.getId());

        boolean isMemberB = followerMembers.stream().anyMatch(m -> m.getId().equals(memberB.getId()));
        boolean isMemberC = followerMembers.stream().anyMatch(m -> m.getId().equals(memberC.getId()));

        //then
        assertThat(isMemberB).isEqualTo(true);
        assertThat(isMemberC).isEqualTo(true);

    }

    Member memberA() {
        Member memberA = Member.builder()
                .name("memberA")
                .email("member@duksung.ac.kr")
                .nickname("memberNickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("덕성대학교")
                .build();
        authService.join(memberA, Optional.empty());
        return memberA;
    }

    Member memberB() {
        Member memberB = Member.builder()
                .name("memberB")
                .email("member@kookmin.ac.kr")
                .nickname("Nickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        authService.join(memberB, Optional.empty());
        return memberB;
    }

    Member memberC() {
        Member memberC = Member.builder()
                .name("memberC")
                .email("memberC@kookmin.ac.kr")
                .nickname("memberC")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        authService.join(memberC, Optional.empty());
        return memberC;
    }


}
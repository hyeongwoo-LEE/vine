package com.post_show_blues.vine.service;

import com.post_show_blues.vine.domain.meeting.Meeting;
import com.post_show_blues.vine.domain.meeting.MeetingRepository;
import com.post_show_blues.vine.domain.member.Member;
import com.post_show_blues.vine.domain.member.MemberRepository;
import com.post_show_blues.vine.domain.notice.Notice;
import com.post_show_blues.vine.domain.notice.NoticeRepository;
import com.post_show_blues.vine.domain.participant.Participant;
import com.post_show_blues.vine.domain.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService{

    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final MeetingRepository meetingRepository;
    private final NoticeRepository noticeRepository;


    /**
     * 참여요청
     */
    @Override
    public Long request(Long meetingId, Long memberId) {

        Optional<Meeting> findMeeting = meetingRepository.findById(meetingId);
        Meeting meeting = findMeeting.get();

        Optional<Member> findMember = memberRepository.findById(memberId);
        Member member = findMember.get();

        //TODO 2021.06.03.-참여마감시간 < 현재시간 예외처리- hyeongwoo
        //요청시간 체크
        String reqDeadline = meeting.getReqDeadline();



        //참여인원 체크
        int maxNumber = meeting.getMaxNumber();
        int currentNumber = meeting.getCurrentNumber();

        if(maxNumber <= currentNumber){
            throw new IllegalStateException("참여인원 초과입니다.");
        }

        Participant participant = Participant.builder()
                .member(member)
                .meeting(meeting)
                .build();

        participantRepository.save(participant);
        
        //TODO 2021.06.04.-요청시 방장에게 수락요청 알림-hyeongwoo
        /*Long masterId = meeting.getMember().getId();

        Notice notice = Notice.builder()
                .memberId(masterId)
                .text(member.getNickname()+"님이 " + meeting.getTitle()+"방에 참여 요청했습니다.")
                .link()
                .build();

        noticeRepository.save(notice);*/

        return participant.getId();
    }

    @Override
    public void accept(Long participantId) {

        Optional<Participant> result = participantRepository.findById(participantId);
        Participant participant = result.get();

        Meeting meeting = participant.getMeeting();

        //모임 참여 현재인원 1증가
        meeting.addCurrentNumber();

        //참여 request변수 true로 변경
        participant.changeReq(true);

        //TODO 2021.06.04.-수락시 요청한 회원에게 수락완료 알림-hyeongwoo
        /*Long memberId = participant.getMember().getId();

        Notice notice = Notice.builder()
                .id(memberId)
                .text(meeting.getTitle() + "방 수락이 완료되었습니다.")
                .link()
                .build();

        noticeRepository.save(notice);*/

    }

    /**
     * 참여조회
     */
    @Override
    @Transactional(readOnly = true)
    public Participant findOne(Long id) {
        Optional<Participant> result = participantRepository.findById(id);

        Participant participant = result.get();

        return participant;
    }
}

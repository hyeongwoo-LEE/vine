package com.post_show_blues.vine.service;

import com.post_show_blues.vine.domain.category.Category;
import com.post_show_blues.vine.domain.meeting.Meeting;
import com.post_show_blues.vine.domain.meeting.MeetingRepository;
import com.post_show_blues.vine.domain.meetingimg.MeetingImg;
import com.post_show_blues.vine.domain.meetingimg.MeetingImgRepository;
import com.post_show_blues.vine.domain.member.Member;
import com.post_show_blues.vine.domain.notice.NoticeRepository;
import com.post_show_blues.vine.domain.participant.ParticipantRepository;
import com.post_show_blues.vine.dto.MeetingDTO;
import com.post_show_blues.vine.dto.MeetingImgDTO;
import com.sun.xml.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService{

    private final MeetingRepository meetingRepository;
    private final MeetingImgRepository meetingImgRepository;
    private final NoticeRepository noticeRepository;
    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public Long register(MeetingDTO meetingDTO) {
        Map<String, Object> result = dtoToEntity(meetingDTO);

        Meeting meeting = (Meeting) result.get("meeting");

        meetingRepository.save(meeting);

        List<MeetingImg> meetingImgList = (List<MeetingImg>) result.get("meetingImgList");

        // 사진 첨부 유무 확인
        if(meetingImgList != null && meetingImgList.size() > 0){
            for(MeetingImg meetingImg : meetingImgList){
                meetingImgRepository.save(meetingImg);
            }
        }


        //TODO 2021.06.02. 모임 등록시 자신의 팔로워들에게 알림 생성 - hyeongwoo

        return meeting.getId();
    }

    @Override
    public void modify(MeetingDTO meetingDTO) {
        Optional<Meeting> result = meetingRepository.findById(meetingDTO.getMeetingId());

        if(result.isPresent()){
            Meeting meeting = result.get();

            //변경
            meeting.changeCategory(Category.builder().id(meetingDTO.getCategoryId()).build());
            meeting.changeMember(Member.builder().id(meetingDTO.getMasterId()).build());
            meeting.changeTitle(meetingDTO.getTitle());
            meeting.changeText(meetingDTO.getText());
            meeting.changePlace(meetingDTO.getPlace());
            meeting.changeMaxNumber(meetingDTO.getMaxNumber());
            meeting.changeMeetDate(meetingDTO.getMeetDate());
            meeting.changeReqDeadline(meetingDTO.getReqDeadline());
            meeting.changeChatLink(meetingDTO.getChatLink());

            meetingRepository.save(meeting);

            /* 여기서부터 img 변경 */
            // meeting의 사진 모두 삭제
            meetingImgRepository.deleteByMeeting(meeting);

            List<MeetingImg> meetingImgList = getImgDtoToEntity(meetingDTO, meeting);
            // 사진 첨부 유무 확인

            if(meetingImgList != null && meetingImgList.size() > 0){
                for(MeetingImg meetingImg : meetingImgList){
                    meetingImgRepository.save(meetingImg);
                }
            }
        }

    }

    @Override
    public void remove(Long meetingId) {
        Optional<Meeting> result = meetingRepository.findById(meetingId);
        Meeting meeting = result.get();

        // participant -> meetingImg -> meeting 순으로 삭제
        participantRepository.deleteByMeeting(meeting);
        meetingImgRepository.deleteByMeeting(meeting);
        meetingRepository.deleteById(meetingId);
    }

    @Override
    public Meeting findOne(Long meetingId) {
        Optional<Meeting> result = meetingRepository.findById(meetingId);

        Meeting meeting = result.get();
        return meeting;
    }
}
package com.post_show_blues.vine.service;

import com.post_show_blues.vine.domain.category.Category;
import com.post_show_blues.vine.domain.category.CategoryRepository;
import com.post_show_blues.vine.domain.meeting.Meeting;
import com.post_show_blues.vine.domain.meeting.MeetingRepository;
import com.post_show_blues.vine.domain.meetingimg.MeetingImg;
import com.post_show_blues.vine.domain.meetingimg.MeetingImgRepository;
import com.post_show_blues.vine.domain.member.Member;
import com.post_show_blues.vine.domain.member.MemberRepository;
import com.post_show_blues.vine.domain.memberimg.MemberImg;
import com.post_show_blues.vine.domain.memberimg.MemberImgRepository;
import com.post_show_blues.vine.domain.participant.Participant;
import com.post_show_blues.vine.domain.participant.ParticipantRepository;
import com.post_show_blues.vine.domain.requestParticipant.RequestParticipant;
import com.post_show_blues.vine.domain.requestParticipant.RequestParticipantRepository;
import com.post_show_blues.vine.dto.MeetingDTO;
import com.post_show_blues.vine.dto.MeetingImgDTO;
import com.post_show_blues.vine.dto.PageRequestDTO;
import com.post_show_blues.vine.dto.PageResultDTO;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class MeetingServiceImplTest {

    @Autowired MeetingService meetingService;
    @Autowired MeetingRepository meetingRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired MeetingImgRepository meetingImgRepository;
    @Autowired RequestParticipantRepository requestParticipantRepository;
    @Autowired MemberImgRepository memberImgRepository;

   @Test
    void 모임등록() throws Exception{
        //given
        Member member = createMember();
        Category category = createCategory();

        List<MeetingImgDTO> meetingImgDTOList = new ArrayList<>();

        //ImgDTO 생성 2개
        MeetingImgDTO meetingImgDTOA = MeetingImgDTO.builder()
                .fileName("MeetingImgA")
                .filePath("/hyeongWoo")
                .uuid(UUID.randomUUID().toString())
                .build();

        MeetingImgDTO meetingImgDTOB = MeetingImgDTO.builder()
                .fileName("MeetingImgB")
                .filePath("/hyeongWoo")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgDTOList.add(meetingImgDTOA);
        meetingImgDTOList.add(meetingImgDTOB);


        MeetingDTO meetingDTO = MeetingDTO.builder()
                .categoryId(category.getId())
                .masterId(member.getId())
                .title("MeetingA")
                .text("meet")
                .place("A")
                .meetDate(LocalDateTime.of(2021,07,13,16,00))
                .reqDeadline(LocalDateTime.of(2021,06,05,16,00))
                .dDay(Period.between(LocalDate.now(),
                        LocalDateTime.of(2021,06,05,00,00)
                                .toLocalDate()).getDays())
                .maxNumber(4)
                .imgDTOList(meetingImgDTOList)
                .build();

        //when
        Long saveId = meetingService.register(meetingDTO);

        //then
        Meeting meeting = meetingService.findOne(saveId);
        Assertions.assertThat(meeting.getTitle()).isEqualTo("MeetingA");
        Assertions.assertThat(meeting.getMember().getId()).isEqualTo(member.getId());
        Assertions.assertThat(meeting.getCategory().getId()).isEqualTo(category.getId());
        //Assertions.assertThat(meeting.getDDay()).isEqualTo(2);

        List<MeetingImg> meetingImgList = meetingImgRepository.findByMeeting(meeting);
        Assertions.assertThat(meetingImgList.size()).isEqualTo(2);

    }

    @Test
    void 등록시_meetDate_deadline_비교() throws Exception{
        //given
        Member member = createMember();
        Category category = createCategory();

        MeetingDTO meetingDTO = MeetingDTO.builder()
                .categoryId(category.getId())
                .masterId(member.getId())
                .title("MeetingA")
                .text("meet")
                .place("A")
                .meetDate(LocalDateTime.of(2021,06,04,16,30))
                .reqDeadline(LocalDateTime.of(2021,06,04,17,00))
                .dDay(Period.between(LocalDate.now(),
                        LocalDateTime.of(2021,06,05,00,00)
                                .toLocalDate()).getDays())
                .maxNumber(4)
                .build();
        
        //when
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> meetingService.register(meetingDTO));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("활동일이 신청마감일보다 빠릅니다.");
    }


    @Test
    void 모임수정() throws Exception{
        //given
        Meeting meeting = createMeeting();

        MeetingImg memberImgA = MeetingImg.builder()
                .meeting(meeting)
                .filePath("/hyeonWoo")
                .fileName("MemberImgA")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgRepository.save(memberImgA);

        //수정 데이터
        Category category1 = createCategory();

        Member member1 = Member.builder()
                .name("member")
                .email("memberA@kookmin.ac.kr")
                .nickname("memberANickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        memberRepository.save(member1);


        List<MeetingImgDTO> meetingImgDTOList = new ArrayList<>();

        MeetingImgDTO meetingImgDTOB = MeetingImgDTO.builder()
                .fileName("MeetingImgB")
                .filePath("/hyeongWoo")
                .uuid(UUID.randomUUID().toString())
                .build();

        MeetingImgDTO meetingImgDTOC = MeetingImgDTO.builder()
                .fileName("MeetingImgC")
                .filePath("/hyeongWoo")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgDTOList.add(meetingImgDTOB);
        meetingImgDTOList.add(meetingImgDTOC);

        //MeetingDTO 생성
        MeetingDTO meetingDTO = MeetingDTO.builder()
                .meetingId(meeting.getId())
                .categoryId(category1.getId()) //변경
                .masterId(member1.getId()) //변경
                .title("MeetingB") //meetingA -> meeting B로 변경
                .text("meet2") //meet -> meet2
                .place("B") // A -> B
                .meetDate(LocalDateTime.of(2021,07,14,00,00)) //5 -> 6일로 변경
                .reqDeadline(LocalDateTime.of(2021,06,04,00,00))
                .dDay(Period.between(LocalDate.now(),
                        LocalDateTime.of(2021,06,05,00,00)
                                .toLocalDate()).getDays())
                .maxNumber(5) // 4 -> 5로 변경
                .imgDTOList(meetingImgDTOList)
                .build();

        //when
        meetingService.modify(meetingDTO);

        //then
        Optional<Meeting> result = meetingRepository.findById(meeting.getId());
        Meeting findMeeting = result.get();

        Assertions.assertThat(findMeeting.getTitle()).isEqualTo("MeetingB");
        Assertions.assertThat(findMeeting.getMember().getId()).isEqualTo(member1.getId());
        Assertions.assertThat(findMeeting.getCategory().getId()).isEqualTo(category1.getId());
        //Assertions.assertThat(findMeeting.getDDay()).isEqualTo(3);

        List<MeetingImg> meetingImgList = meetingImgRepository.findByMeeting(meeting);
        Assertions.assertThat(meetingImgList.size()).isEqualTo(2);

    }

    @Test
    void 인원수정오류() throws Exception{
        //given
        Meeting meeting = createMeeting();

        //수정 데이터
        Category category1 = createCategory();
        Member member1 = Member.builder()
                .name("member")
                .email("memberA@kookmin.ac.kr")
                .nickname("memberANickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        memberRepository.save(member1);

        MeetingDTO meetingDTO = MeetingDTO.builder()
                .meetingId(meeting.getId())
                .categoryId(category1.getId())
                .masterId(member1.getId())
                .title("MeetingB") //meetingA -> meeting B로 변경
                .text("meet2") //meet -> meet2
                .place("B") // A -> B
                .meetDate(LocalDateTime.of(2021,06,05,00,00))
                .reqDeadline(LocalDateTime.of(2021,06,04,00,00))
                .dDay(Period.between(LocalDate.now(),
                        LocalDateTime.of(2021,06,05,00,00)
                                .toLocalDate()).getDays())
                .maxNumber(2) // 4 -> 2로 변경
                .build();

        //when
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> meetingService.modify(meetingDTO));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("참여인원 초과입니다.");

    }

    @Test
    void 모임삭제() throws Exception{
        //given
        Participant participant = createParticipant();

        Meeting meeting = participant.getMeeting();
        Long meetingId = meeting.getId();

        MeetingImg meetingImg = MeetingImg.builder()
                .meeting(meeting)
                .filePath("/hyeongWoo")
                .fileName("MeetingImgA")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgRepository.save(meetingImg);

        //meeting 에 요청자 생성
        Member member =Member.builder()
                .name("member")
                .email("memberB@kookmin.ac.kr")
                .nickname("memberBNickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();
        memberRepository.save(member);

        RequestParticipant requestParticipant = RequestParticipant.builder()
                .meeting(meeting)
                .member(member)
                .build();
        requestParticipantRepository.save(requestParticipant);

        //when
        meetingService.remove(meetingId);

        //then
        //삭제된 모임에 참여된 레코드 검색
        NoSuchElementException e1 = assertThrows(NoSuchElementException.class,
                () -> (participantRepository.findById(participant.getId())).get());

        //삭제된 모임에 참여요청한 레코드 검색
        NoSuchElementException e2 = assertThrows(NoSuchElementException.class,
                () -> (requestParticipantRepository.findById(requestParticipant.getId())).get());

        //삭제된 모임방의 사진 검색
        NoSuchElementException e3 = assertThrows(NoSuchElementException.class,
                () -> meetingImgRepository.findById(meetingImg.getId()).get());

        //삭제된 모임방 검색
        NoSuchElementException e4 = assertThrows(NoSuchElementException.class,
                () -> meetingRepository.findById(meetingId).get());


        Assertions.assertThat(e1.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e2.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e3.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e4.getMessage()).isEqualTo("No value present");

    }

    @Test
    void 모임리스트조회() throws Exception{
        //given

        //카테고리 생성
        Category category1 = Category.builder().name("스포츠").build();
        Category category2 = Category.builder().name("음악").build();
        Category category3 = Category.builder().name("맛집").build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        //meeting 생성
        IntStream.rangeClosed(1,5).forEach(i -> {

            Category category = createCategory();

            Member member = Member.builder()
                    .name("member"+i)
                    .email("member"+i+"@kookmin.ac.kr")
                    .nickname("member"+i+"Nickname")
                    .password("1111")
                    .phone("010-0000-0000")
                    .university("국민대학교")
                    .build();
            memberRepository.save(member);

            MemberImg memberImg = MemberImg.builder()
                    .member(member)
                    .fileName("MemberImg"+i)
                    .filePath("/hyeongwoo")
                    .uuid(UUID.randomUUID().toString())
                    .build();
            memberImgRepository.save(memberImg);

            Meeting meeting = Meeting.builder()
                    .category(category)
                    .member(member)
                    .title("Meeting"+i)
                    .text("meet")
                    .place("A")
                    .meetDate(LocalDateTime.of(2021,8,06,00,00))
                    .reqDeadline(LocalDateTime.of(2021,06,04,00,00))
                    .dDay(Period.between(LocalDate.now(),
                            LocalDateTime.of(2021,8,05,00,00)
                                    .toLocalDate()).getDays())
                    .maxNumber(4)
                    .currentNumber(3)
                    .build();
            meetingRepository.save(meeting);

        });

        List<Meeting> meetingList = meetingRepository.findAll();

        Meeting meeting1 = meetingList.get(0);
        Meeting meeting2 = meetingList.get(1);
        Meeting meeting3 = meetingList.get(2);
        Meeting meeting4 = meetingList.get(3);
        Meeting meeting5 = meetingList.get(4);

        meeting1.changeCategory(category2); // category = "음악"
        meeting2.changeCategory(category1); // category = "스포츠"
        meeting3.changeCategory(category1); // category = "스포츠"
        meeting4.changeCategory(category1); // category = "스포츠"
        meeting5.changeCategory(category3); // category = "맛집"

        meeting2.changeTitle("10시까지 풋살 모집"); // meeting2
        meeting4.changeTitle("풋살할 사람"); //meeting4

        //participant 생성
        IntStream.rangeClosed(6,8).forEach(i->{
            Member member = Member.builder()
                    .name("member"+i)
                    .email("member"+i+"@kookmin.ac.kr")
                    .nickname("member"+i+"Nickname")
                    .password("1111")
                    .phone("010-0000-0000")
                    .university("국민대학교")
                    .build();
            memberRepository.save(member);

            MemberImg memberImg = MemberImg.builder()
                    .member(member)
                    .fileName("MemberImg"+i)
                    .filePath("/hyeongwoo")
                    .uuid(UUID.randomUUID().toString())
                    .build();
            memberImgRepository.save(memberImg);

            Participant participant = Participant.builder()
                    .meeting(meeting2)
                    .member(member)
                    .build();

            participantRepository.save(participant);
        });

        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .category(category1)
                .keyword("풋살")
                .page(1)
                .size(10)
                .build();

        //when
        /**
         * meeting2,3,4 category1(스포츠)
         * meeting2,4 title =  ~풋살~
         * = meeting2,4만 출력
         * meeting2 참여자 3
         * meeting4 참여자 x
         */
        PageResultDTO<MeetingDTO, Object[]> result = meetingService.getMeetingList(pageRequestDTO);

        //then
        List<MeetingDTO> meetingDTOList = result.getDtoList();

        for (MeetingDTO meetingDTO : meetingDTOList){
            System.out.println(meetingDTO);
        }

        Assertions.assertThat(result.getPage()).isEqualTo(1);
        Assertions.assertThat(result.getDtoList().size()).isEqualTo(2);
        Assertions.assertThat(result.getTotalPage()).isEqualTo(1);
    }

    @Test
    void 모임리스트조회_참여자사진X() throws Exception{
        //given
        //meeting 생성
        IntStream.rangeClosed(1,5).forEach(i -> {

            Category category = createCategory();

            Member member = Member.builder()
                    .name("member"+i)
                    .email("member"+i+"@kookmin.ac.kr")
                    .nickname("member"+i+"Nickname")
                    .password("1111")
                    .phone("010-0000-0000")
                    .university("국민대학교")
                    .build();
            memberRepository.save(member);

            MemberImg memberImg = MemberImg.builder()
                    .member(member)
                    .fileName("MemberImg"+i)
                    .filePath("/hyeongwoo")
                    .uuid(UUID.randomUUID().toString())
                    .build();
            memberImgRepository.save(memberImg);

            Meeting meeting = Meeting.builder()
                    .category(category)
                    .member(member)
                    .title("Meeting"+i)
                    .text("meet")
                    .place("A")
                    .meetDate(LocalDateTime.of(2021,8,06,00,00))
                    .reqDeadline(LocalDateTime.of(2021,06,04,00,00))
                    .dDay(Period.between(LocalDate.now(),
                            LocalDateTime.of(2021,8,05,00,00)
                                    .toLocalDate()).getDays())
                    .maxNumber(4)
                    .currentNumber(3)
                    .build();
            meetingRepository.save(meeting);

        });

        List<Meeting> meetingList = meetingRepository.findAll();

        Meeting meeting = meetingList.get(0);

        //participant 생성
        IntStream.rangeClosed(6,8).forEach(i->{
            Member member = Member.builder()
                    .name("member"+i)
                    .email("member"+i+"@kookmin.ac.kr")
                    .nickname("member"+i+"Nickname")
                    .password("1111")
                    .phone("010-0000-0000")
                    .university("국민대학교")
                    .build();
            memberRepository.save(member);

            Participant participant = Participant.builder()
                    .meeting(meeting)
                    .member(member)
                    .build();

            participantRepository.save(participant);
        });

        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(1)
                .size(10)
                .build();

        //when
        /**
            첫번째 모임에만 참여자 존재하며 프로필 사진은 x
         */
        PageResultDTO<MeetingDTO, Object[]> result = meetingService.getMeetingList(pageRequestDTO);

        //then
        List<MeetingDTO> meetingDTOList = result.getDtoList();

        for (MeetingDTO meetingDTO : meetingDTOList){
            System.out.println(meetingDTO);
        }

        Assertions.assertThat(result.getPage()).isEqualTo(1);
        Assertions.assertThat(result.getDtoList().size()).isEqualTo(5);
        Assertions.assertThat(result.getTotalPage()).isEqualTo(1);
    }

    @Test
    void 모임_조회페이지DTO() throws Exception{
        //given
        Meeting meeting = createMeeting();

        MeetingImg meetingImg1 = MeetingImg.builder()
                .meeting(meeting)
                .fileName("MeetingImg1")
                .filePath("/hyeongwoo1")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgRepository.save(meetingImg1);

        MeetingImg meetingImg2 = MeetingImg.builder()
                .meeting(meeting)
                .fileName("MeetingImg2")
                .filePath("/hyeongwoo2")
                .uuid(UUID.randomUUID().toString())
                .build();

        meetingImgRepository.save(meetingImg2);



        //when
        MeetingDTO meetingDTO = meetingService.getMeeting(meeting.getId());

        //then
        Assertions.assertThat(meetingDTO.getMeetingId()).isEqualTo(meeting.getId());
        Assertions.assertThat(meetingDTO.getCategoryName()).isEqualTo("categoryA");
        Assertions.assertThat(meetingDTO.getImgDTOList().size()).isEqualTo(2);
    }

    @Test
    void 모임_조회페이지DTO_사진x경우() throws Exception{
        //given
        Meeting meeting = createMeeting();

        //when
        MeetingDTO meetingDTO = meetingService.getMeeting(meeting.getId());

        //then
        Assertions.assertThat(meetingDTO.getMeetingId()).isEqualTo(meeting.getId());
        Assertions.assertThat(meetingDTO.getCategoryName()).isEqualTo("categoryA");
        Assertions.assertThat(meetingDTO.getImgDTOList().size()).isEqualTo(0);
    }

    private Participant createParticipant() {
        Meeting meeting = createMeeting();
        Member member =Member.builder()
                .name("member")
                .email("memberA@kookmin.ac.kr")
                .nickname("memberANickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        memberRepository.save(member);

        Participant participant = Participant.builder()
                .meeting(meeting)
                .member(member)
                .build();

        participantRepository.save(participant);

        return participant;
    }

    private Meeting createMeeting() {
        Category category = createCategory();
        Member member = createMember();

        Meeting meeting = Meeting.builder()
                .category(category)
                .member(member)
                .title("MeetingA")
                .text("meet")
                .place("A")
                .meetDate(LocalDateTime.of(2021,06,05,00,00))
                .reqDeadline(LocalDateTime.of(2021,06,04,00,00))
                .dDay(Period.between(LocalDate.now(),
                        LocalDateTime.of(2021,06,05,00,00)
                                .toLocalDate()).getDays())
                .maxNumber(4)
                .currentNumber(3)
                .build();

        meetingRepository.save(meeting);

        return meeting;
    }

    private Category createCategory() {
        Category category = Category.builder()
                .name("categoryA")
                .build();

        categoryRepository.save(category);

        return category;
    }

    private Member createMember() {
        Member member = Member.builder()
                .name("member")
                .email("member@kookmin.ac.kr")
                .nickname("memberNickname")
                .password("1111")
                .phone("010-0000-0000")
                .university("국민대학교")
                .build();

        memberRepository.save(member);

        return member;
    }
}
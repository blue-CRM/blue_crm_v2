package com.blue.visit.service;

import com.blue.auth.dto.MeetingRoomDto;
import com.blue.auth.mapper.MeetingRoomMapper;
import com.blue.global.exception.AuthException;
import com.blue.visit.dto.*;
import com.blue.visit.mapper.VisitScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitScheduleService {
  
  private final MeetingRoomMapper meetingRoomMapper;
  private final VisitScheduleMapper mapper;
  
  private void requireLogin(String email) {
    if (email == null || email.isBlank()) {
      throw new AuthException("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
    }
  }
  
  private void requireValidRange(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new AuthException("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    if (!from.isBefore(to)) {
      throw new AuthException("기간이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    // 필요하면 과도한 범위 제한 (예: 14일)
    // if (ChronoUnit.DAYS.between(from, to) > 14) throw ...
  }
  
  @Transactional(readOnly = true)
  public List<VisitScheduleRowDto> listWeek(String email, LocalDate from, LocalDate to) {
    requireLogin(email);
    requireValidRange(from, to);
    
    LocalDateTime fromDt = from.atStartOfDay();
    LocalDateTime toDt = to.atStartOfDay(); // exclusive
    return mapper.findSchedules(fromDt, toDt);
  }
  
  @Transactional(readOnly = true)
  public VisitScheduleFocusDto focusByCustomer(String email, Long customerId) {
    requireLogin(email);
    if (customerId == null) {
      throw new AuthException("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    
    VisitScheduleFocusDto dto = mapper.findFocusByCustomerId(customerId);
    if (dto == null || dto.getVisitId() == null) {
      throw new AuthException("예약이 없습니다.", HttpStatus.NOT_FOUND);
    }
    return dto;
  }
  
  @Transactional
  public void create(String requesterEmail, VisitScheduleUpsertReq req) {
    validate(req);
    
    VisitUserContextDto ctx = requireUser(requesterEmail);
    requireWritable(ctx);
    
    int selectable = mapper.countSelectableCustomer(req.getCustomerId(), ctx.getUserId());
    if (selectable == 0) {
      throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
    
    int conflict = mapper.countConflicts(req.getRoomId(), req.getStartAt(), req.getEndAt(), null);
    if (conflict > 0) throw new AuthException("해당 시간에 이미 예약이 있습니다.", HttpStatus.FORBIDDEN);
    
    mapper.insertSchedule(req.getCustomerId(), req.getRoomId(), ctx.getUserId(),
        req.getStartAt(), req.getEndAt(), req.getMemo());
  }
  
  @Transactional
  public void update(String requesterEmail, Long visitId, VisitScheduleUpsertReq req) {
    validate(req);
    
    VisitUserContextDto ctx = requireUser(requesterEmail);
    requireWritable(ctx);
    
    VisitScheduleMetaDto meta = mapper.findScheduleMeta(visitId);
    if (meta == null || meta.getCustomerId() == null) {
      throw new AuthException("존재하지 않는 일정입니다.", HttpStatus.FORBIDDEN);
    }
    
    // customerId 조작 방지
    if (!meta.getCustomerId().equals(req.getCustomerId())) {
      throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
    
    if (!ctx.getUserId().equals(meta.getScheduledByUserId())) {
      throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
    
    int conflict = mapper.countConflicts(req.getRoomId(), req.getStartAt(), req.getEndAt(), visitId);
    if (conflict > 0) throw new AuthException("해당 시간에 이미 예약이 있습니다.", HttpStatus.FORBIDDEN);
    
    mapper.updateSchedule(visitId, req.getRoomId(), req.getStartAt(), req.getEndAt(), req.getMemo());
  }
  
  @Transactional
  public void delete(String requesterEmail, Long visitId) {
    VisitUserContextDto ctx = requireUser(requesterEmail);
    requireWritable(ctx);
    
    VisitScheduleMetaDto meta = mapper.findScheduleMeta(visitId);
    if (meta == null || meta.getCustomerId() == null) {
      throw new AuthException("존재하지 않는 일정입니다.", HttpStatus.FORBIDDEN);
    }
    
    if (!ctx.getUserId().equals(meta.getScheduledByUserId())) {
      throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
    
    mapper.deleteSchedule(visitId);
  }
  
  @Transactional
  public List<VisitCustomerPickDto> listSelectableCustomers(String requesterEmail, String keyword) {
    VisitUserContextDto ctx = requireUser(requesterEmail);
    requireWritable(ctx); // MANAGER/STAFF만
    
    return mapper.findSelectableCustomers(ctx.getUserId(), keyword);
  }
  
  @Transactional(readOnly = true)
  public List<MeetingRoomDto> listActiveRoomsForVisit(String email) {
    requireLogin(email);
    return meetingRoomMapper.findActiveRooms();
  }
  
  public VisitCustomerPickDto getCustomerById(String email, Long customerId) {
    VisitCustomerPickDto dto = mapper.findCustomerById(customerId);
    if (dto == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return dto;
  }
  
  private void validate(VisitScheduleUpsertReq req) {
    if (req.getCustomerId() == null) throw new IllegalArgumentException("customerId 필수");
    if (req.getRoomId() == null) throw new IllegalArgumentException("roomId 필수");
    if (req.getStartAt() == null || req.getEndAt() == null) throw new IllegalArgumentException("startAt/endAt 필수");
    if (!req.getEndAt().isAfter(req.getStartAt())) throw new IllegalArgumentException("endAt은 startAt 이후여야 함");
    
    if (!req.getStartAt().toLocalDate().equals(req.getEndAt().toLocalDate())) {
      throw new AuthException("일정은 하루를 넘길 수 없습니다.", HttpStatus.FORBIDDEN);
    }
    
    int etc = mapper.isEtcRoom(req.getRoomId());
    if (etc > 0) {
      if (req.getMemo() == null || req.getMemo().isBlank()) {
        throw new AuthException("기타 회의실은 메모가 필수입니다.", HttpStatus.FORBIDDEN);
      }
    }
  }
  
  private VisitUserContextDto requireUser(String email) {
    requireLogin(email);
    VisitUserContextDto ctx = mapper.findUserContextByEmail(email);
    if (ctx == null || ctx.getUserId() == null) {
      throw new AuthException("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
    }
    return ctx;
  }
  
  private void requireWritable(VisitUserContextDto ctx) {
    String role = ctx.getRole();
    boolean ok = "MANAGER".equals(role) || "STAFF".equals(role);
    if (!ok) throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
  }
}
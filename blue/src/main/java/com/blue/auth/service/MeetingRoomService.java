package com.blue.auth.service;

import com.blue.auth.dto.MeetingRoomCreateReq;
import com.blue.auth.dto.MeetingRoomDto;
import com.blue.auth.dto.MeetingRoomUpdateReq;
import com.blue.auth.mapper.MeetingRoomMapper;
import com.blue.global.exception.AuthException;
import com.blue.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {
  
  private final MeetingRoomMapper meetingRoomMapper;
  private final UserMapper userMapper;
  
  private void requireSuper(String email) {
    if (email == null || email.isBlank()) {
      throw new AuthException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
    }
    Boolean ok = userMapper.isSuperByEmail(email);
    if (!Boolean.TRUE.equals(ok)) {
      throw new AuthException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
  }
  
  private String requireValidName(String roomName) {
    String name = (roomName == null) ? "" : roomName.trim();
    if (name.length() < 2) {
      throw new AuthException("회의실명은 2자 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
    }
    return name;
  }
  
  private void requireValidRoomId(long roomId) {
    if (roomId <= 0) {
      throw new AuthException("잘못된 회의실 ID 입니다.", HttpStatus.BAD_REQUEST);
    }
  }
  
  @Transactional(readOnly = true)
  public List<MeetingRoomDto> list(String email, String keyword) {
    requireSuper(email);
    return meetingRoomMapper.findRooms(keyword);
  }
  
  @Transactional(readOnly = true)
  public List<MeetingRoomDto> listActiveForVisit(String email) {
    requireSuper(email);
    return meetingRoomMapper.findActiveRooms();
  }
  
  @Transactional
  public void create(String email, MeetingRoomCreateReq req) {
    requireSuper(email);
    if (req == null) {
      throw new AuthException("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    
    String name = requireValidName(req.getRoomName());
    
    if (meetingRoomMapper.existsByName(name, null) > 0) {
      throw new AuthException("이미 존재하는 회의실명입니다.", HttpStatus.CONFLICT);
    }
    
    int inserted = meetingRoomMapper.insertRoom(name);
    if (inserted == 0) {
      throw new AuthException("이미 존재하는 회의실명입니다.", HttpStatus.CONFLICT);
    }
  }
  
  @Transactional
  public void update(String email, long roomId, MeetingRoomUpdateReq req) {
    requireSuper(email);
    requireValidRoomId(roomId);
    if (req == null) {
      throw new AuthException("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    
    String name = requireValidName(req.getRoomName());
    
    if (meetingRoomMapper.existsByName(name, roomId) > 0) {
      throw new AuthException("이미 존재하는 회의실명입니다.", HttpStatus.CONFLICT);
    }
    
    // null 넘어오면 기존값 유지(안전)
    Boolean current = meetingRoomMapper.findIsActive(roomId);
    if (current == null) {
      throw new AuthException("존재하지 않는 회의실입니다.", HttpStatus.NOT_FOUND);
    }
    boolean isActive = (req.getIsActive() == null) ? current : Boolean.TRUE.equals(req.getIsActive());
    
    int updated = meetingRoomMapper.updateRoom(roomId, name, isActive);
    if (updated == 0) {
      throw new AuthException("존재하지 않는 회의실입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  // 소프트 삭제 = 중지 (행은 남김)
  @Transactional
  public void softDelete(String email, long roomId) {
    requireSuper(email);
    requireValidRoomId(roomId);
    
    int updated = meetingRoomMapper.softDeleteRoom(roomId);
    if (updated == 0) {
      throw new AuthException("존재하지 않는 회의실입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  // 하드 삭제 = 완전 삭제
  @Transactional
  public void hardDelete(String email, long roomId) {
    requireSuper(email);
    requireValidRoomId(roomId);
    
    // 먼저 존재 확인
    Integer exists = meetingRoomMapper.existsById(roomId);
    if (exists == null || exists == 0) {
      throw new AuthException("존재하지 않는 회의실입니다.", HttpStatus.NOT_FOUND);
    }
    
    try {
      int deleted = meetingRoomMapper.hardDeleteRoom(roomId);
      if (deleted == 0) {
        throw new AuthException("존재하지 않는 회의실입니다.", HttpStatus.NOT_FOUND);
      }
    } catch (DataIntegrityViolationException e) {
      // FK 걸려있으면 여기로 떨어짐
      throw new AuthException("연결된 데이터가 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT);
    }
  }
}
package com.blue.auth.service;

import com.blue.auth.dto.ExpertDto;
import com.blue.auth.dto.MyInfoResponse;
import com.blue.auth.mapper.ExpertMapper;
import com.blue.auth.mapper.MyInfoMapper;
import com.blue.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertAdminService {
  
  private final ExpertMapper expertMapper;
  private final MyInfoMapper myInfoMapper;
  
  // SUPERADMIN 체크 (GrantsDto 안 쓰고 user_role 바로 써도 되면 프로젝트 스타일에 맞춰 바꿔도 됨)
  private void ensureSuper(String email) {
    MyInfoResponse user = myInfoMapper.findByEmail(email);
    if (user == null) {
      throw new AuthException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
    if (user.getGrants() == null || !user.getGrants().isSuper()) {
      throw new AuthException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
  }
  
  @Transactional(readOnly = true)
  public List<ExpertDto> getExperts(String callerEmail, String keyword) {
    ensureSuper(callerEmail);
    String k = (keyword == null) ? null : keyword.trim();
    if (!StringUtils.hasText(k)) {
      k = null;
    }
    return expertMapper.findAll(k);
  }
  
  @Transactional
  public void createExpert(String callerEmail, ExpertDto dto) {
    ensureSuper(callerEmail);
    
    if (dto == null) {
      throw new AuthException("전문가 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    String name = dto.getExpertName() != null ? dto.getExpertName().trim() : "";
    if (!StringUtils.hasText(name)) {
      throw new AuthException("전문가 이름은 필수입니다.", HttpStatus.BAD_REQUEST);
    }
    if (name.length() > 100) {
      throw new AuthException("전문가 이름이 너무 깁니다. (최대 100자)", HttpStatus.BAD_REQUEST);
    }
    
    try {
      int inserted = expertMapper.insert(name);
      if (inserted != 1) {
        throw new AuthException("전문가 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (DuplicateKeyException e) {
      // uq_experts_name
      throw new AuthException("이미 존재하는 전문가 이름입니다.", HttpStatus.BAD_REQUEST);
    }
  }
  
  @Transactional
  public void renameExpert(String callerEmail, Long expertId, ExpertDto dto) {
    ensureSuper(callerEmail);
    
    if (expertId == null) {
      throw new AuthException("expertId가 필요합니다.", HttpStatus.BAD_REQUEST);
    }
    if (dto == null) {
      throw new AuthException("전문가 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    
    String name = dto.getExpertName() != null ? dto.getExpertName().trim() : "";
    if (!StringUtils.hasText(name)) {
      throw new AuthException("전문가 이름은 필수입니다.", HttpStatus.BAD_REQUEST);
    }
    if (name.length() > 100) {
      throw new AuthException("전문가 이름이 너무 깁니다. (최대 100자)", HttpStatus.BAD_REQUEST);
    }
    
    try {
      int updated = expertMapper.rename(expertId, name);
      if (updated == 0) {
        throw new AuthException("대상 전문가를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      }
    } catch (DuplicateKeyException e) {
      throw new AuthException("이미 존재하는 전문가 이름입니다.", HttpStatus.BAD_REQUEST);
    }
  }
  
  @Transactional
  public void deleteExpert(String callerEmail, Long expertId) {
    ensureSuper(callerEmail);
    
    if (expertId == null) {
      throw new AuthException("expertId가 필요합니다.", HttpStatus.BAD_REQUEST);
    }
    
    // 이미 유저가 물려 있으면 삭제 불가
    int cnt = expertMapper.countUsersUsing(expertId);
    if (cnt > 0) {
      throw new AuthException("이미 사용자에 연결된 전문가입니다. 먼저 해당 사용자들의 전문가 설정을 해제하세요.",
          HttpStatus.BAD_REQUEST);
    }
    
    try {
      int deleted = expertMapper.delete(expertId);
      if (deleted == 0) {
        throw new AuthException("대상 전문가를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      }
    } catch (DataIntegrityViolationException e) {
      // 혹시 동시성으로 FK 에러 나면 한 번 더 방어
      throw new AuthException("해당 전문가가 여전히 사용자와 연결되어 있어 삭제할 수 없습니다.",
          HttpStatus.BAD_REQUEST);
    }
  }
}

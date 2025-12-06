package com.blue.auth.service;

import com.blue.auth.dto.GrantsDto;
import com.blue.auth.dto.IpWhitelistDto;
import com.blue.auth.dto.MyInfoResponse;
import com.blue.auth.mapper.IpWhitelistMapper;
import com.blue.auth.mapper.MyInfoMapper;
import com.blue.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IpWhitelistService {
  
  private final IpWhitelistMapper ipWhitelistMapper;
  private final MyInfoMapper myInfoMapper;
  
  /** 슈퍼계정인지 체크 */
  private void ensureSuper(String email) {
    MyInfoResponse user = myInfoMapper.findByEmail(email);
    if (user == null) {
      throw new AuthException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
    
    GrantsDto grants = user.getGrants();
    if (grants == null || !grants.isSuper()) {
      throw new AuthException("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
  }
  
  // 로그인 시 사용 (여기는 슈퍼여부랑 상관 없음)
  @Transactional(readOnly = true)
  public boolean isAllowed(String ip) {
    return ipWhitelistMapper.countActiveByIp(ip) > 0;
  }
  
  // 설정 화면용 조회 (슈퍼만)
  @Transactional(readOnly = true)
  public List<IpWhitelistDto> getAll(String callerEmail) {
    ensureSuper(callerEmail);
    return ipWhitelistMapper.findAll();
  }
  
  @Transactional
  public void create(String callerEmail, IpWhitelistDto dto) {
    ensureSuper(callerEmail);
    
    // 기본값 처리
    if (dto.getIsActive() == null || dto.getIsActive().isBlank()) {
      dto.setIsActive("Y");
    }
    ipWhitelistMapper.insert(dto);
  }
  
  @Transactional
  public void update(String callerEmail, Long ipId, IpWhitelistDto dto) {
    ensureSuper(callerEmail);
    
    dto.setIpId(ipId); // path variable로 받은 id를 여기다 세팅
    int affected = ipWhitelistMapper.update(dto);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 IP 입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  @Transactional
  public void deactivate(String callerEmail, Long ipId) {
    ensureSuper(callerEmail);
    
    int affected = ipWhitelistMapper.deactivate(ipId);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 IP 입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  @Transactional
  public void deleteHard(String callerEmail, Long ipId) {
    ensureSuper(callerEmail);
    
    if (ipId == null) {
      throw new AuthException("잘못된 IP ID 입니다.", HttpStatus.BAD_REQUEST);
    }
    
    int affected = ipWhitelistMapper.deleteHard(ipId);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 IP 입니다.", HttpStatus.NOT_FOUND);
    }
  }
}
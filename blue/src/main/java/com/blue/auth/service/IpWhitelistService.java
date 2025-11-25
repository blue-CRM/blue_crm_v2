package com.blue.auth.service;

import com.blue.auth.dto.IpWhitelistDto;
import com.blue.auth.mapper.IpWhitelistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IpWhitelistService {
  
  private final IpWhitelistMapper ipWhitelistMapper;
  
  // 로그인 시 사용
  public boolean isAllowed(String ip) {
    return ipWhitelistMapper.countActiveByIp(ip) > 0;
  }
  
  // 설정 화면용 조회
  public List<IpWhitelistDto> getAll() {
    return ipWhitelistMapper.findAll();
  }
  
  @Transactional
  public void create(IpWhitelistDto dto) {
    // 기본값 처리
    if (dto.getIsActive() == null || dto.getIsActive().isBlank()) {
      dto.setIsActive("Y");
    }
    ipWhitelistMapper.insert(dto);
  }
  
  @Transactional
  public void update(Long ipId, IpWhitelistDto dto) {
    dto.setIpId(ipId); // path variable로 받은 id를 여기다 세팅
    ipWhitelistMapper.update(dto);
  }
  
  @Transactional
  public void deactivate(Long ipId) {
    ipWhitelistMapper.deactivate(ipId);
  }
}

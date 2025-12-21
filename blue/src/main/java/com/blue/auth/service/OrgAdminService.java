package com.blue.auth.service;

import com.blue.auth.dto.BranchDto;
import com.blue.auth.dto.CenterDto;
import com.blue.auth.dto.GrantsDto;
import com.blue.auth.dto.MyInfoResponse;
import com.blue.auth.mapper.MyInfoMapper;
import com.blue.auth.mapper.OrgAdminMapper;
import com.blue.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgAdminService {
  
  private final OrgAdminMapper orgAdminMapper;
  private final MyInfoMapper myInfoMapper;
  
  /** 슈퍼 계정인지 체크 */
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
  
  // ====================
  // BRANCHES (지점)
  // ====================
  
  @Transactional(readOnly = true)
  public List<BranchDto> getBranches(String callerEmail, String keyword) {
    ensureSuper(callerEmail);
    
    String k = (keyword == null || keyword.isBlank())
        ? null
        : keyword.trim();
    
    return orgAdminMapper.findBranches(k);
  }
  
  @Transactional
  public void createBranch(String callerEmail, String branchName) {
    ensureSuper(callerEmail);
    
    if (branchName == null || branchName.isBlank()) {
      throw new AuthException("지점 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    String trimmed = branchName.trim();
    
    if (orgAdminMapper.existsBranchName(trimmed, null)) {
      throw new AuthException("이미 존재하는 지점명입니다.", HttpStatus.CONFLICT);
    }
    
    BranchDto dto = new BranchDto();
    dto.setBranchName(trimmed);
    orgAdminMapper.insertBranch(dto);
  }
  
  @Transactional
  public void renameBranch(String callerEmail, Long branchId, String newName) {
    ensureSuper(callerEmail);
    
    if (branchId == null || branchId <= 0) {
      throw new AuthException("잘못된 지점 ID 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (newName == null || newName.isBlank()) {
      throw new AuthException("지점 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    String trimmed = newName.trim();
    
    if (orgAdminMapper.existsBranchName(trimmed, branchId)) {
      throw new AuthException("이미 존재하는 지점명입니다.", HttpStatus.CONFLICT);
    }
    
    BranchDto dto = new BranchDto();
    dto.setBranchId(branchId);
    dto.setBranchName(trimmed);
    
    int affected = orgAdminMapper.updateBranchName(dto);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 지점입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  @Transactional
  public void deleteBranch(String callerEmail, Long branchId) {
    ensureSuper(callerEmail);
    
    if (branchId == null || branchId <= 0) {
      throw new AuthException("잘못된 지점 ID 입니다.", HttpStatus.BAD_REQUEST);
    }
    
    // 본사(1) 삭제 막을 거면 여기
    if (branchId == 1L) {
      throw new AuthException("본사 지점은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    
    int centers = orgAdminMapper.countCentersInBranch(branchId);
    if (centers > 0) {
      throw new AuthException("소속 팀이 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT);
    }
    
    int affected = orgAdminMapper.deleteBranch(branchId);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 지점입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  // ====================
  // CENTERS (팀/센터)
  // ====================
  
  @Transactional(readOnly = true)
  public List<CenterDto> getCenters(String callerEmail, String keyword) {
    ensureSuper(callerEmail);
    
    String k = (keyword == null || keyword.isBlank())
        ? null
        : keyword.trim();
    
    return orgAdminMapper.findCenters(k);
  }
  
  private String normalizeHex6(String v) {
    if (v == null) return null;
    String s = v.trim();
    return s.matches("^#[0-9a-fA-F]{6}$") ? s : null;
  }
  
  @Transactional
  public void createCenter(String callerEmail, String centerName, Long branchId, String centerColor) {
    ensureSuper(callerEmail);
    
    if (centerName == null || centerName.isBlank()) {
      throw new AuthException("팀 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    String trimmed = centerName.trim();
    
    if (orgAdminMapper.existsCenterName(trimmed, null)) {
      throw new AuthException("이미 존재하는 팀명입니다.", HttpStatus.CONFLICT);
    }
    
    CenterDto dto = new CenterDto();
    dto.setCenterName(trimmed);
    dto.setBranchId(branchId);
    dto.setCenterColor(normalizeHex6(centerColor));
    
    orgAdminMapper.insertCenter(dto);
  }
  
  @Transactional
  public void updateCenter(String callerEmail, Long centerId, String centerName, Long branchId, String centerColor) {
    // 1) 권한 체크
    ensureSuper(callerEmail);
    
    // 2) 기본 검증
    if (centerId == null || centerId <= 0) {
      throw new AuthException("잘못된 팀 ID 입니다.", HttpStatus.BAD_REQUEST);
    }
    
    // 3) 본사 센터(팀) 보호
    if (centerId == 1L) {
      throw new AuthException("본사 팀은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    
    if (centerName == null || centerName.isBlank()) {
      throw new AuthException("팀 이름을 입력하세요.", HttpStatus.BAD_REQUEST);
    }
    String trimmed = centerName.trim();
    
    if (orgAdminMapper.existsCenterName(trimmed, centerId)) {
      throw new AuthException("이미 존재하는 팀명입니다.", HttpStatus.CONFLICT);
    }
    
    int updated = orgAdminMapper.updateCenter(centerId, trimmed, branchId, centerColor);
    if (updated == 0) {
      throw new AuthException("존재하지 않는 팀입니다.", HttpStatus.NOT_FOUND);
    }
  }
  
  @Transactional
  public void deleteCenter(String callerEmail, Long centerId) {
    ensureSuper(callerEmail);
    
    if (centerId == null || centerId <= 0) {
      throw new AuthException("잘못된 팀 ID 입니다.", HttpStatus.BAD_REQUEST);
    }
    
    // 본사 센터(id==1) 보호
    if (centerId == 1L) {
      throw new AuthException("본사 팀은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    
    int users = orgAdminMapper.countUsersInCenter(centerId);
    if (users > 0) {
      throw new AuthException("팀에 소속된 직원이 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT);
    }
    
    int affected = orgAdminMapper.deleteCenter(centerId);
    if (affected == 0) {
      throw new AuthException("존재하지 않는 팀입니다.", HttpStatus.NOT_FOUND);
    }
  }
}

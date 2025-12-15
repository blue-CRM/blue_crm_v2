package com.blue.customer.revoke.service;

import com.blue.customer.all.dto.PagedResponse;
import com.blue.customer.alloclog.dto.AllocLogInsertDto;
import com.blue.customer.alloclog.dto.CustomerOwnerSnapshotDto;
import com.blue.customer.alloclog.mapper.AllocLogMapper;
import com.blue.customer.revoke.dto.*;
import com.blue.customer.revoke.mapper.CustomerRevokeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerRevokeService {
  
  private final CustomerRevokeMapper mapper;
  private final AllocLogMapper allocLogMapper;
  
  public PagedResponse<RevokeListRowDto> list(String callerEmail,
                                              int page, int size,
                                              String keyword, String dateFrom, String dateTo,
                                              String category, String division, String status, String sort) {
    UserContextDto me = mapper.findUserContextByEmail(callerEmail);
    if (me == null) throw new IllegalArgumentException("인증 사용자 정보를 찾을 수 없습니다.");
    
    int offset = (page - 1) * size;
    List<RevokeListRowDto> items;
    int total;
    
    switch (me.getRole()) {
      case "SUPERADMIN" -> {
        items = mapper.findListForHq(offset, size, keyword, dateFrom, dateTo, category, division, status, sort, me.getVisible());
        total = mapper.countListForHq(keyword, dateFrom, dateTo, category, division, status, me.getVisible());
      }
      case "MANAGER" -> {
        items = mapper.findListForManager(offset, size, keyword, dateFrom, dateTo, category, status, sort, me.getCenterId(), me.getVisible(), me.getUserId());
        total = mapper.countListForManager(keyword, dateFrom, dateTo, category, status, me.getCenterId(), me.getVisible(), me.getUserId());
      }
      case "CENTERHEAD" -> {
        items = mapper.findListForCenterHead(offset, size, keyword, dateFrom, dateTo, category, status, sort, me.getCenterId(), me.getVisible(), me.getUserId());
        total = mapper.countListForCenterHead(keyword, dateFrom, dateTo, category, status, me.getCenterId(), me.getVisible(), me.getUserId());
      }
      case "EXPERT" -> {
        items = mapper.findListForExpert(offset, size, keyword, dateFrom, dateTo, category, status, sort, me.getExpertId(), me.getVisible());
        total = mapper.countListForExpert(keyword, dateFrom, dateTo, category, status, me.getExpertId(), me.getVisible());
      }
      default -> throw new IllegalArgumentException("이 메뉴는 분배권한이 있는 사용자만 사용할 수 있습니다.");
    }
    
    int totalPages = (int) Math.ceil((double) total / size);
    return new PagedResponse<>(items, totalPages, total);
  }
  
  @Transactional
  public RevokeResult revokeByHq(String callerEmail, RevokeReq req) {
    UserContextDto me = mapper.findUserContextByEmail(callerEmail);
    if (me == null || !"SUPERADMIN".equals(me.getRole())) {
      throw new IllegalArgumentException("본사만 수행할 수 있습니다.");
    }
    if (req.getCustomerIds() == null || req.getCustomerIds().isEmpty()) {
      return new RevokeResult(0, 0);
    }
    
    // 대상 잠금 (상태 NOT IN 없음/회수)
    List<Long> lockIds = mapper.lockCustomersForRevokeByHq(req.getCustomerIds());
    if (lockIds.isEmpty()) return new RevokeResult(0, req.getCustomerIds().size());
    
    // 회수 직전 담당자 스냅샷
    List<CustomerOwnerSnapshotDto> owners = allocLogMapper.findOwnersByCustomerIds(lockIds);
    
    // 회수 처리: 상태=회수, 프로=NULL, 예약시간=NULL, 최초/업셀 매출 = 0
    mapper.updateToRevoked(lockIds);
    
    // 매출 초기화 로그 기록 ('RESET' 타입)
    mapper.insertRevokeSalesLogs(lockIds, me.getUserId());
    
    // 회수 로그 기록
    writeRevokeLogs(lockIds, owners, me.getUserId(), null);
    
    return new RevokeResult(lockIds.size(), req.getCustomerIds().size() - lockIds.size());
  }
  
  @Transactional
  public RevokeResult revokeByManager(String callerEmail, RevokeReq req) {
    UserContextDto me = mapper.findUserContextByEmail(callerEmail);
    if (me == null || !"MANAGER".equals(me.getRole())) {
      throw new IllegalArgumentException("팀장만 수행할 수 있습니다.");
    }
    if (req.getCustomerIds() == null || req.getCustomerIds().isEmpty()) {
      return new RevokeResult(0, 0);
    }
    
    // 같은 팀 소속 프로의 건만 락
    List<Long> lockIds = mapper.lockCustomersForRevokeByManager(req.getCustomerIds(), me.getCenterId(), me.getUserId());
    if (lockIds.isEmpty()) return new RevokeResult(0, req.getCustomerIds().size());
    
    // 회수 직전 담당자 스냅샷
    List<CustomerOwnerSnapshotDto> owners = allocLogMapper.findOwnersByCustomerIds(lockIds);
    
    // 회수 처리
    mapper.updateToRevoked(lockIds);
    
    // 매출 초기화 로그 기록 ('RESET' 타입)
    mapper.insertRevokeSalesLogs(lockIds, me.getUserId());
    
    // 회수 로그 기록
    writeRevokeLogs(lockIds, owners, me.getUserId(), null);
    
    return new RevokeResult(lockIds.size(), req.getCustomerIds().size() - lockIds.size());
  }
  
  @Transactional
  public RevokeResult revokeByCenterHead(String callerEmail, RevokeReq req) {
    UserContextDto me = mapper.findUserContextByEmail(callerEmail);
    if (me == null || !"CENTERHEAD".equals(me.getRole())) {
      throw new IllegalArgumentException("센터장만 수행할 수 있습니다.");
    }
    if (req.getCustomerIds() == null || req.getCustomerIds().isEmpty()) {
      return new RevokeResult(0, 0);
    }
    
    // 센터 범위 내 대상만 락
    List<Long> lockIds = mapper.lockCustomersForRevokeByCenterHead(req.getCustomerIds(), me.getCenterId(), me.getUserId());
    if (lockIds.isEmpty()) return new RevokeResult(0, req.getCustomerIds().size());
    
    // 회수 직전 담당자 스냅샷
    List<CustomerOwnerSnapshotDto> owners = allocLogMapper.findOwnersByCustomerIds(lockIds);
    
    // 회수 처리
    mapper.updateToRevoked(lockIds);
    
    // 매출 초기화 로그 기록 ('RESET' 타입)
    mapper.insertRevokeSalesLogs(lockIds, me.getUserId());
    
    // 회수 로그 기록
    writeRevokeLogs(lockIds, owners, me.getUserId(), null);
    
    return new RevokeResult(lockIds.size(), req.getCustomerIds().size() - lockIds.size());
  }
  
  @Transactional
  public RevokeResult revokeByExpert(String callerEmail, RevokeReq req) {
    UserContextDto me = mapper.findUserContextByEmail(callerEmail);
    if (me == null || !"EXPERT".equals(me.getRole())) {
      throw new IllegalArgumentException("전문가만 수행할 수 있습니다.");
    }
    if (req.getCustomerIds() == null || req.getCustomerIds().isEmpty()) {
      return new RevokeResult(0, 0);
    }
    
    // 본인 담당만 락
    List<Long> lockIds = mapper.lockCustomersForRevokeByExpert(req.getCustomerIds(), me.getExpertId());
    if (lockIds.isEmpty()) return new RevokeResult(0, req.getCustomerIds().size());
    
    // 회수 직전 담당자 스냅샷
    List<CustomerOwnerSnapshotDto> owners = allocLogMapper.findOwnersByCustomerIds(lockIds);
    
    // 회수 처리
    mapper.updateToRevoked(lockIds);
    
    // 매출 초기화 로그 기록 ('RESET' 타입)
    mapper.insertRevokeSalesLogs(lockIds, me.getUserId());
    
    // 회수 로그 기록
    writeRevokeLogs(lockIds, owners, me.getUserId(), null);
    
    return new RevokeResult(lockIds.size(), req.getCustomerIds().size() - lockIds.size());
  }
  
  
  /**
   * 회수 로그 기록 공용 메서드
   * - from_user_id : 회수 직전 담당자
   * - to_user_id   : NULL (회수)
   * - is_final_assign:
   *      MANAGER + 상태 '없음'  => 0 (팀장 풀 회수, 통계 제외)
   *      그 외                    => 1 (확정 DB 회수, 통계에 반영)
   */
  private void writeRevokeLogs(List<Long> customerIds,
                               List<CustomerOwnerSnapshotDto> owners,
                               Long actedByUserId,
                               String memo) {
    if (customerIds == null || customerIds.isEmpty()) return;
    if (owners == null || owners.isEmpty()) return;
    
    Map<Long, CustomerOwnerSnapshotDto> ownerMap = owners.stream()
        .collect(Collectors.toMap(CustomerOwnerSnapshotDto::getCustomerId, o -> o));
    
    List<AllocLogInsertDto> logs = new ArrayList<>();
    
    for (Long customerId : customerIds) {
      CustomerOwnerSnapshotDto snap = ownerMap.get(customerId);
      if (snap == null || snap.getUserId() == null) {
        // 담당자 없는 건은 통계/로그 대상에서 제외
        continue;
      }
      
      boolean isManagerPool = "MANAGER".equals(snap.getUserRole()) && "없음".equals(snap.getStatus());
      
      int finalFlag = isManagerPool ? 0 : 1;
      
      AllocLogInsertDto dto = new AllocLogInsertDto();
      dto.setCustomerId(customerId);
      dto.setActionType("REVOKE");
      dto.setFromUserId(snap.getUserId());
      dto.setToUserId(null);
      dto.setActedByUserId(actedByUserId);
      dto.setIsFinalAssign(finalFlag);
      dto.setMemo(memo);
      logs.add(dto);
    }
    
    if (!logs.isEmpty()) {
      allocLogMapper.insertLogs(logs);
    }
  }
}

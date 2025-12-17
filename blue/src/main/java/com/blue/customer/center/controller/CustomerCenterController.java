package com.blue.customer.center.controller;

import com.blue.customer.all.dto.PagedResponse;
import com.blue.customer.center.dto.BranchSimpleDto;
import com.blue.customer.center.dto.CenterDbRowDto;
import com.blue.customer.center.dto.CenterSimpleDto;
import com.blue.customer.center.service.CustomerCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CustomerCenterController {
  
  private final CustomerCenterService service;
  
  /** 지점(브랜치) 드롭다운용 */
  @GetMapping("/work/center/branches")
  public ResponseEntity<List<BranchSimpleDto>> getBranches(Authentication auth) {
    return ResponseEntity.ok(service.getBranches(auth.getName()));
  }
  
  /** 팀(센터) 드롭다운용: 지점 선택 후 팀 목록 */
  @GetMapping("/work/center/teams")
  public ResponseEntity<List<CenterSimpleDto>> getTeams(
      Authentication auth,
      @RequestParam Long branchId
  ) {
    return ResponseEntity.ok(service.getTeams(auth.getName(), branchId));
  }
  
  /** 센터DB 목록 (조회 전용) */
  @GetMapping("/work/center/db")
  public PagedResponse<CenterDbRowDto> getCenterDb(
      Authentication auth,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String division,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String expertName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long branchId,
      @RequestParam(required = false) Long centerId
  ) {
    return service.getCenterDb(
        auth.getName(),
        page, size,
        keyword, dateFrom, dateTo,
        category, division, sort,
        expertName, status,
        branchId, centerId
    );
  }
  
  /** 엑셀 다운로드 */
  @GetMapping("/work/center/db/export")
  public ResponseEntity<byte[]> exportCenterDb(
      Authentication auth,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String division,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String expertName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long branchId,
      @RequestParam(required = false) Long centerId
  ) {
    return service.exportExcel(
        auth.getName(),
        keyword, dateFrom, dateTo,
        category, division, sort,
        expertName, status,
        branchId, centerId
    );
  }
}

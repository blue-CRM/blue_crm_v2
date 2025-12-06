package com.blue.dashboard.controller;

import com.blue.dashboard.dto.*;
import com.blue.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  
  private final DashboardService dashboardService;
  
  @GetMapping("/branches")
  public List<BranchDto> getBranches() {
    return dashboardService.findBranches();
  }
  
  @GetMapping("/centers")
  public List<CenterDto> getCenters() {
    return dashboardService.findCenters();
  }
  
  // 승인(Y) 사용자 리스트(간단 조회)
  @GetMapping("/users")
  public List<UserDto> getUsers() {
    return dashboardService.findUsers();
  }
  
  // 정확 일치 검색 (이름 또는 이메일) — 프론트에서 사용
  @GetMapping("/users/find")
  public List<UserDto> findUsersExact(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email
  ) {
    return dashboardService.findUsersExact(name, email);
  }
  
  // 개인 통계 조회
  @GetMapping("/stats/user")
  public UserStatsResponse getUserStats(
      @RequestParam Long userId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo
  ) {
    return dashboardService.getUserStats(userId, dateFrom, dateTo);
  }
  
  // 팀별 통계 조회
  @GetMapping("/stats/centers")
  public CenterStatsResponse getCenterStats(
      @RequestParam List<Long> centerIds,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo
  ) {
    return dashboardService.getCenterStats(centerIds, dateFrom, dateTo);
  }
  
  // 지점별 통계 조회
  @GetMapping("/stats/branches")
  public BranchStatsResponse getBranchStats(
      @RequestParam List<Long> branchIds,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo
  ) {
    return dashboardService.getBranchStats(branchIds, dateFrom, dateTo);
  }
  
  // KPI (프론트 호환)
  @GetMapping("/kpi")
  public SummaryDto getKpi() {
    return dashboardService.getSummary();
  }
  
  // KPI - 동일 기능 별칭 (/summary)
  @GetMapping("/summary")
  public SummaryDto getSummary() {
    return dashboardService.getSummary();
  }
}
package com.blue.dashboard.service;

import com.blue.dashboard.dto.*;
import com.blue.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
  
  private final DashboardMapper dashboardMapper;
  
  /* ───────── 지점 / 센터 / 유저 기본 조회 ───────── */
  
  public List<BranchDto> findBranches() {
    return dashboardMapper.findBranches();
  }
  
  public List<CenterDto> findCenters() {
    return dashboardMapper.findCenters(); // (리스트는 '본사' 제외)
  }
  
  public List<UserDto> findUsers() {
    return dashboardMapper.findUsers(); // 승인 Y
  }
  
  public List<UserDto> findUsersExact(String name, String email) {
    // 이름 우선, 없으면 이메일, 둘 다 없으면 빈 리스트
    if (name != null && !name.isBlank())
      return dashboardMapper.findUsersExactByName(name);
    if (email != null && !email.isBlank())
      return dashboardMapper.findUsersExactByEmail(email);
    return List.of();
  }
  
  /* ───────── 개인 통계 ───────── */
  
  public UserStatsResponse getUserStats(Long userId, String dateFrom, String dateTo) {
    if (userId == null) {
      return UserStatsResponse.builder()
          .userId(null)
          .dbRangeWithDup(0)
          .dbRangeOnly(0)
          .dbAllWithDup(0)
          .dbAllOnly(0)
          .build();
    }
    
    RangeStatsDto range = dashboardMapper.getUserRangeStats(userId, dateFrom, dateTo);
    if (range == null) {
      range = new RangeStatsDto();
    }
    
    TotalStatsDto total = dashboardMapper.getUserTotalStats(userId);
    if (total == null) {
      total = new TotalStatsDto();
    }
    
    int rangeMain = range.getMainCount();
    int rangeDup  = range.getDupCount();
    int totalMain = total.getMainCount();
    int totalDup  = total.getDupCount();
    
    return UserStatsResponse.builder()
        .userId(userId)
        .dbRangeWithDup(rangeMain + rangeDup)
        .dbRangeOnly(rangeMain)
        .dbAllWithDup(totalMain + totalDup)
        .dbAllOnly(totalMain)
        .build();
  }
  
  /* ───────── 센터 통계 ───────── */
  
  public CenterStatsResponse getCenterStats(List<Long> centerIds, String dateFrom, String dateTo) {
    if (centerIds == null || centerIds.isEmpty()) {
      return CenterStatsResponse.builder()
          .centerIds(Collections.emptyList())
          .totalUsers(0)
          .dbRangeWithDup(0)
          .dbRangeOnly(0)
          .dbAllWithDup(0)
          .dbAllOnly(0)
          .build();
    }
    
    RangeStatsDto range = dashboardMapper.getCenterRangeStats(centerIds, dateFrom, dateTo);
    if (range == null) {
      range = new RangeStatsDto();
    }
    
    TotalStatsDto total = dashboardMapper.getCenterTotalStats(centerIds);
    if (total == null) {
      total = new TotalStatsDto();
    }
    
    int totalUsers = dashboardMapper.countUsersInCenters(centerIds);
    
    int rangeMain = range.getMainCount();
    int rangeDup  = range.getDupCount();
    int totalMain = total.getMainCount();
    int totalDup  = total.getDupCount();
    
    return CenterStatsResponse.builder()
        .centerIds(centerIds)
        .totalUsers(totalUsers)
        .dbRangeWithDup(rangeMain + rangeDup)
        .dbRangeOnly(rangeMain)
        .dbAllWithDup(totalMain + totalDup)
        .dbAllOnly(totalMain)
        .build();
  }
  
  /* ───────── 지점 통계 ───────── */
  
  public BranchStatsResponse getBranchStats(List<Long> branchIds, String dateFrom, String dateTo) {
    if (branchIds == null || branchIds.isEmpty()) {
      return BranchStatsResponse.builder()
          .branchIds(Collections.emptyList())
          .totalUsers(0)
          .dbRangeWithDup(0)
          .dbRangeOnly(0)
          .dbAllWithDup(0)
          .dbAllOnly(0)
          .build();
    }
    
    RangeStatsDto range = dashboardMapper.getBranchRangeStats(branchIds, dateFrom, dateTo);
    if (range == null) {
      range = new RangeStatsDto();
    }
    
    TotalStatsDto total = dashboardMapper.getBranchTotalStats(branchIds);
    if (total == null) {
      total = new TotalStatsDto();
    }
    
    int totalUsers = dashboardMapper.countUsersInBranches(branchIds);
    
    int rangeMain = range.getMainCount();
    int rangeDup  = range.getDupCount();
    int totalMain = total.getMainCount();
    int totalDup  = total.getDupCount();
    
    return BranchStatsResponse.builder()
        .branchIds(branchIds)
        .totalUsers(totalUsers)
        .dbRangeWithDup(rangeMain + rangeDup)
        .dbRangeOnly(rangeMain)
        .dbAllWithDup(totalMain + totalDup)
        .dbAllOnly(totalMain)
        .build();
  }
  
  public SummaryDto getSummary() {
    int users    = dashboardMapper.countApprovedUsers();
    int centers  = dashboardMapper.countCentersExcludingHQ();
    int branches = dashboardMapper.countBranchesExcludingHQ();
    
    return new SummaryDto(users, centers, branches);
  }
}
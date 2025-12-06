package com.blue.dashboard.mapper;

import com.blue.dashboard.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {
  
  /* ───────── 지점 / 센터 / 유저 기본 조회 ───────── */
  
  // 지점 목록 (본사지점 제외)
  List<BranchDto> findBranches();
  
  // 센터 목록 (branch_id != 1만)
  List<CenterDto> findCenters();
  
  // 승인 사용자 목록
  List<UserDto> findUsers();
  
  // 정확 일치 검색: 이름
  List<UserDto> findUsersExactByName(@Param("name") String name);
  
  // 정확 일치 검색: 이메일
  List<UserDto> findUsersExactByEmail(@Param("email") String email);
  
  
  /* ───────── 개인 통계 ───────── */
  
  // 기간 내 합계
  RangeStatsDto getUserRangeStats(
      @Param("userId") Long userId,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo
  );
  
  // 전체 기간 합계
  TotalStatsDto getUserTotalStats(@Param("userId") Long userId);
  
  
  /* ───────── 센터 통계 ───────── */
  
  // 기간 내 합계 (센터 여러 개)
  RangeStatsDto getCenterRangeStats(
      @Param("centerIds") List<Long> centerIds,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo
  );
  
  // 전체 기간 합계 (센터 여러 개)
  TotalStatsDto getCenterTotalStats(@Param("list") List<Long> centerIds);
  
  // 센터들에 속한 직원 수 (MANAGER + STAFF)
  int countUsersInCenters(@Param("list") List<Long> centerIds);
  
  
  /* ───────── 지점 통계 ───────── */
  
  // 기간 내 합계 (지점 여러 개)
  RangeStatsDto getBranchRangeStats(
      @Param("branchIds") List<Long> branchIds,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo
  );
  
  // 전체 기간 합계 (지점 여러 개)
  TotalStatsDto getBranchTotalStats(@Param("list") List<Long> branchIds);
  
  // 지점에 속한 직원 수 (MANAGER + STAFF)
  int countUsersInBranches(@Param("list") List<Long> branchIds);
  
  
  /* ───────── KPI ───────── */
  
  // KPI: 승인 사용자 수
  int countApprovedUsers();
  
  // KPI: 센터 수 (본사지점 소속 제외)
  int countCentersExcludingHQ();
  
  // KPI: 지점 수 (본사지점 제외)
  int countBranchesExcludingHQ();
}
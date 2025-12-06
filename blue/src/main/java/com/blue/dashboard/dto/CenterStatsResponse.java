package com.blue.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CenterStatsResponse {
  private List<Long> centerIds;
  
  private int totalUsers;     // 선택 센터들의 STAFF+MANAGER 수
  
  // 기간 내
  private int dbRangeWithDup;
  private int dbRangeOnly;
  
  // 전체
  private int dbAllWithDup;
  private int dbAllOnly;
}
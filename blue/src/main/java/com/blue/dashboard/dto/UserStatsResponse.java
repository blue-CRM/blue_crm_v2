package com.blue.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
  private Long userId;
  
  // 기간 내
  private int dbRangeWithDup; // 기간 내 (유효+중복)
  private int dbRangeOnly;    // 기간 내 (유효만)
  
  // 전체
  private int dbAllWithDup;   // 전체 (유효+중복)
  private int dbAllOnly;      // 전체 (유효만)
}
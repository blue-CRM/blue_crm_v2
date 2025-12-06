package com.blue.dashboard.dto;

import lombok.Data;

@Data
public class TotalStatsDto {
  // 전체 기간 합계: mainCount = 유효/최초, dupCount = 중복
  private int mainCount;
  private int dupCount;
}
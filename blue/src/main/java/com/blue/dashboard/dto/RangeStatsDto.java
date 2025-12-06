package com.blue.dashboard.dto;

import lombok.Data;

@Data
public class RangeStatsDto {
  // 기간 내 합계: mainCount = 유효/최초, dupCount = 중복
  private int mainCount;
  private int dupCount;
}
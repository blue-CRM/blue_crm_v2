package com.blue.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDto {
  private int users;     // 승인 사용자 수
  private int centers;   // 팀(센터) 수
  private int branches;  // 지점 수
}
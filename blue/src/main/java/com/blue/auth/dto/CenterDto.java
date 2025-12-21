package com.blue.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CenterDto {
  private Long centerId;
  private Long branchId;
  private String centerName;
  private String centerColor;
}

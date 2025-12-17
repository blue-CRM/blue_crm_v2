package com.blue.customer.center.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchSimpleDto {
  private Long branchId;
  private String branchName;
}
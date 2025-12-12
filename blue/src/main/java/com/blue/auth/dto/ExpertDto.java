package com.blue.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpertDto {
  private Long expertId;
  private String expertName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
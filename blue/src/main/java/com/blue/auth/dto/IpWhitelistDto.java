package com.blue.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpWhitelistDto {
  private Long ipId;
  private String ipAddress;
  private String memo;
  private String isActive;   // 'Y' / 'N'
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

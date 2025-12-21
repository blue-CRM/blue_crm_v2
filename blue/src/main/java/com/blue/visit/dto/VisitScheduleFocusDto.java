package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitScheduleFocusDto {
  private Long visitId;
  private Long customerId;
  private Long roomId;
  private LocalDateTime visitAt;
  private LocalDateTime visitEndAt;
}
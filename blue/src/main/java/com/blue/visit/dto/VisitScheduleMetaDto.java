package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitScheduleMetaDto {
  private Long visitId;
  private Long customerId;
  private Long scheduledByUserId;
}
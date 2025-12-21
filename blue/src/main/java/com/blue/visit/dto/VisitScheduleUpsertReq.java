package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitScheduleUpsertReq {
  private Long customerId;
  private Long roomId;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private String memo;
}

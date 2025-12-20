package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitScheduleRowDto {
  private Long visitId;
  private Long customerId;
  private String customerName;
  private String customerPhone;
  
  private Long scheduledByUserId;
  private String scheduledByName;
  private Long centerId;
  private String centerName;
  private String centerColor;
  
  private Long roomId;
  private String roomName;
  
  private LocalDateTime visitAt;
  private LocalDateTime visitEndAt;
  private String memo;
}
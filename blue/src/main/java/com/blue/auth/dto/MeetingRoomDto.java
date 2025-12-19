package com.blue.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomDto {
  private Long roomId;
  private String roomName;
  private Boolean isActive;   // MySQL tinyint(1) ↔ Boolean
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
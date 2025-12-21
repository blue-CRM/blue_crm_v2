package com.blue.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingRoomUpdateReq {
  private String roomName;
  private Boolean isActive;
}
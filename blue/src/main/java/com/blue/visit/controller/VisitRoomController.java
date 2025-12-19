package com.blue.visit.controller;

import com.blue.auth.dto.MeetingRoomDto;
import com.blue.auth.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/work/visit")
public class VisitRoomController {
  
  private final MeetingRoomService meetingRoomService;
  
  @GetMapping("/rooms")
  public List<MeetingRoomDto> rooms(Authentication auth) {
    String email = (auth == null) ? null : auth.getName();
    return meetingRoomService.listActiveForVisit(email);
  }
}
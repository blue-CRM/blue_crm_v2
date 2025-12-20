package com.blue.visit.controller;

import com.blue.auth.dto.MeetingRoomDto;
import com.blue.auth.service.MeetingRoomService;
import com.blue.visit.dto.VisitCustomerPickDto;
import com.blue.visit.dto.VisitScheduleRowDto;
import com.blue.visit.dto.VisitScheduleUpsertReq;
import com.blue.visit.service.VisitScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/work/visit")
public class VisitRoomController {
  
  private final VisitScheduleService visitScheduleService;
  
  @GetMapping("/rooms")
  public List<MeetingRoomDto> rooms(Authentication auth) {
    String email = (auth == null) ? null : auth.getName();
    return visitScheduleService.listActiveRoomsForVisit(email);
  }
  
  @GetMapping("/schedules")
  public List<VisitScheduleRowDto> schedules(
      Authentication auth,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    String email = (auth == null) ? null : auth.getName();
    return visitScheduleService.listWeek(email, from, to);
  }
  
  @PostMapping("/schedules")
  public void create(Authentication auth, @RequestBody VisitScheduleUpsertReq req) {
    visitScheduleService.create(auth.getName(), req);
  }
  
  @PutMapping("/schedules/{visitId}")
  public void update(Authentication auth, @PathVariable Long visitId, @RequestBody VisitScheduleUpsertReq req) {
    visitScheduleService.update(auth.getName(), visitId, req);
  }
  
  @DeleteMapping("/schedules/{visitId}")
  public void delete(Authentication auth, @PathVariable Long visitId) {
    visitScheduleService.delete(auth.getName(), visitId);
  }
  
  @GetMapping("/customers")
  public List<VisitCustomerPickDto> customers(Authentication auth,
                                              @RequestParam(required = false) String keyword) {
    return visitScheduleService.listSelectableCustomers(auth.getName(), keyword);
  }
  
}
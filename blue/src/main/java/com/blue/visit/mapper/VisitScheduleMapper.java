package com.blue.visit.mapper;

import com.blue.visit.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VisitScheduleMapper {
  List<VisitScheduleRowDto> findSchedules(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);
  VisitScheduleFocusDto findFocusByCustomerId(@Param("customerId") Long customerId);
  
  int countConflicts(@Param("roomId") Long roomId,
                     @Param("startAt") LocalDateTime startAt,
                     @Param("endAt") LocalDateTime endAt,
                     @Param("excludeVisitId") Long excludeVisitId);
  
  int insertSchedule(@Param("customerId") Long customerId,
                     @Param("roomId") Long roomId,
                     @Param("scheduledByUserId") Long scheduledByUserId,
                     @Param("startAt") LocalDateTime startAt,
                     @Param("endAt") LocalDateTime endAt,
                     @Param("memo") String memo);
  
  int updateSchedule(@Param("visitId") Long visitId,
                     @Param("roomId") Long roomId,
                     @Param("startAt") LocalDateTime startAt,
                     @Param("endAt") LocalDateTime endAt,
                     @Param("memo") String memo);
  
  int deleteSchedule(@Param("visitId") Long visitId);
  
  VisitScheduleMetaDto findScheduleMeta(@Param("visitId") Long visitId);
  VisitCustomerPickDto findCustomerById(Long customerId);
  
  int countOwnedCustomer(@Param("customerId") Long customerId,
                         @Param("userId") Long userId);
  int countSelectableCustomer(@Param("customerId") Long customerId,
                              @Param("userId") Long userId);
  List<VisitCustomerPickDto> findSelectableCustomers(@Param("userId") Long userId,
                                                     @Param("keyword") String keyword);
  
  int isEtcRoom(@Param("roomId") Long roomId);
  VisitUserContextDto findUserContextByEmail(@Param("email") String email);
}
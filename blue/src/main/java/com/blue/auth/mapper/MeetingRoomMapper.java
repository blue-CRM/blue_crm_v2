package com.blue.auth.mapper;

import com.blue.auth.dto.MeetingRoomDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingRoomMapper {
  List<MeetingRoomDto> findRooms(@Param("keyword") String keyword);
  
  int insertRoom(@Param("roomName") String roomName);
  
  int updateRoom(@Param("roomId") long roomId,
                 @Param("roomName") String roomName,
                 @Param("isActive") boolean isActive);
  
  int softDeleteRoom(@Param("roomId") long roomId);
  int hardDeleteRoom(@Param("roomId") long roomId);
  
  Boolean findIsActive(@Param("roomId") long roomId);
  int existsByName(@Param("roomName") String roomName,
                   @Param("excludeId") Long excludeId);
  int existsById(@Param("roomId") long roomId);
  
  
}
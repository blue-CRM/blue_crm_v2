package com.blue.customer.common.stats.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDailyStatsMapper {
  
  // 1) 커서 조회
  long getLastProcessedLogId();
  
  // 2) 새로 생긴 로그 중 최댓값
  Long findMaxLogIdAfter(@Param("lastLogId") long lastLogId);
  
  // 3) 메인 DB: ASSIGN → +1
  int applyAssignMain(
      @Param("lastLogId") long lastLogId,
      @Param("maxLogId") long maxLogId
  );
  
  // 4) 메인 DB: REVOKE → -1
  int applyRevokeMain(
      @Param("lastLogId") long lastLogId,
      @Param("maxLogId") long maxLogId
  );
  
  // 5) 중복 DB: ASSIGN → +N
  int applyAssignDup(
      @Param("lastLogId") long lastLogId,
      @Param("maxLogId") long maxLogId
  );
  
  // 6) 중복 DB: REVOKE → -N
  int applyRevokeDup(
      @Param("lastLogId") long lastLogId,
      @Param("maxLogId") long maxLogId
  );
  
  // 7) 커서 갱신
  int updateCursor(@Param("newLastLogId") long newLastLogId);
}
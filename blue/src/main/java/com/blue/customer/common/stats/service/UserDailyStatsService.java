package com.blue.customer.common.stats.service;

import com.blue.customer.common.stats.mapper.UserDailyStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDailyStatsService {
  
  private final UserDailyStatsMapper statsMapper;
  
  /**
   * alloc_logs + alloc_log_cursor 기반 증분 반영
   * - 최초 1회: SQL 스크립트로 전체 백필 이미 실행했다고 가정
   * - 이후: last_log_id 이후 로그만 읽어서 user_daily_stats 가감
   */
  @Transactional
  public void applyIncrementalFromAllocLogs() {
    long lastLogId = statsMapper.getLastProcessedLogId();
    Long maxLogId = statsMapper.findMaxLogIdAfter(lastLogId);
    
    if (maxLogId == null) {
      log.info("[UserDailyStatsService] 신규 분배 로그 없음 (lastLogId={})", lastLogId);
      return;
    }
    
    log.info("[UserDailyStatsService] incr stats: lastLogId={} ~ maxLogId={}", lastLogId, maxLogId);
    
    // 메인 DB
    int aMain = statsMapper.applyAssignMain(lastLogId, maxLogId);
    int rMain = statsMapper.applyRevokeMain(lastLogId, maxLogId);
    
    // 중복 DB
    int aDup = statsMapper.applyAssignDup(lastLogId, maxLogId);
    int rDup = statsMapper.applyRevokeDup(lastLogId, maxLogId);
    
    statsMapper.updateCursor(maxLogId);
    
    log.info("[UserDailyStatsService] main(+): {}, main(-): {}, dup(+): {}, dup(-): {}, cursor -> {}",
        aMain, rMain, aDup, rDup, maxLogId);
  }
}
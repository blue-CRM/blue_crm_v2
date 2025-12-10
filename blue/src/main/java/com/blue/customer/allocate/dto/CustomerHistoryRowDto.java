package com.blue.customer.allocate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerHistoryRowDto {
  private Long logId;          // = customer_past_users.past_user_id
  private String userName;     // 담당자 이름
  private String customerName; // 현재 담당자일 경우 : 고객DB 이름
  private LocalDateTime assignedAt; // 현재 담당자일 경우 : 고객DB 생성일
  
  @JsonProperty("isCurrent")
  private boolean isCurrent;   // 지금 이 전화번호를 맡고 있는지 여부
}
package com.blue.customer.all.dto;

import lombok.Data;

@Data
public class SalesUpdateDto {
  private Long customerId;
  private String type;   // "INITIAL" or "UPSELL"
  private Long amount;   // 입력한 금액
  
  // DB에서 조회해올 때만 사용하는 필드
  // 변동사항이 있는지 확인하는 용도
  private Long initialPrice;
  private Long upsellPrice;
}
package com.blue.customer.center.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 센터DB 화면 한 행(본사 전용). 컬럼: 생성일/이름/전화/팀 */
@Data
public class CenterDbRowDto {
  private Long id;                 // customer_id 또는 duplicate_id
  private String origin;           // CUSTOMER / DUPLICATE
  
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime createdAt; // 생성일
  
  // 지점/팀
  private Long branchId;           // 지점 ID
  private String branchName;       // 지점명
  private Long centerId;           // 팀 ID
  private String centerName;       // 팀명
  
  // 나머지 컬럼
  private String staff;            // 프로(user_name)
  private String division;         // 최초/유효/중복
  private String category;         // (있으면)
  
  private String name;
  private String phone;
  private String source;
  private String content;
  private String memo;
  
  private String status;
  private String reservation;      // 화면 표시용 문자열 (없으면 null)
  
  private Integer initialPrice;    // 0이면 null
  private Integer upsellPrice;     // 0이면 null
}
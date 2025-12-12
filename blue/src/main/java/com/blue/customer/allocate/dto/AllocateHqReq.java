package com.blue.customer.allocate.dto;

import lombok.Data;
import java.util.List;

@Data
public class AllocateHqReq {
  private List<Long> customerIds;   // 분배할 customers.customer_id 목록
  private Long targetCenterId;      // 필수: 센터 드롭다운 선택값
  private Long targetUserId;        // 선택: 특정 직원 지정(없으면 센터장 자동 선택)
  
  // 팀장풀인지 아닌지 : 팀장에게 분배 시, 팀원(개인)처럼 확정 분배할지 여부
  private Boolean assignToManagerAsStaff;
}

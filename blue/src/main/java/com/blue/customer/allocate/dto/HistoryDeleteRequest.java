package com.blue.customer.allocate.dto;

import lombok.Data;

import java.util.List;

@Data
public class HistoryDeleteRequest {
  private List<Long> logIds; // 프론트에서 보내는 selectedIds
}
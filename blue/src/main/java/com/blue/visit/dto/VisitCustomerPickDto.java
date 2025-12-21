package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitCustomerPickDto {
  private Long customerId;
  private String customerName;
  private String customerPhone;
}
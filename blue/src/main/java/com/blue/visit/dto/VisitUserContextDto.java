package com.blue.visit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitUserContextDto {
  private Long userId;
  private String role;
  private String userName;
  private String isSuper;
}

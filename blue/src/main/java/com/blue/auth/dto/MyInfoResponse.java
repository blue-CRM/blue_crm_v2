package com.blue.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyInfoResponse {
  private Long userId;
  private String userName;
  private String userEmail;
  private String userPhone;
  private String userPassword;
  private String centerName; // centerId 대신 센터 이름
  private GrantsDto grants;
}
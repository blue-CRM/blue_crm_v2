package com.blue.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrantsDto {
  private String role; // 직급(권한)
  @JsonProperty("isSuper")
  private boolean isSuper; // 슈퍼계정 여부
  @JsonIgnore
  private boolean canAllocate; // 분배 권한
  @JsonIgnore
  private boolean canPhoneAccess; // 가시 권한
  
  // 세부 권한 맵(없으면 빈 맵)
  @JsonProperty("perms")
  public Map<String, Boolean> getPerms() {
    return Map.of(
        "allocate", canAllocate,
        "phoneAccess", canPhoneAccess
    );
  }
}
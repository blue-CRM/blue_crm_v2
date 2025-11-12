package com.blue.auth.dto;

import java.util.Map;

public record GrantsDto(
    String role, // 직급(권한)
    boolean isSuper, // 슈퍼계정 여부
    Map<String, Boolean> perms // 세부 권한 맵(없으면 빈 맵)
) {}
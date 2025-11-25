package com.blue.auth.controller;

import com.blue.auth.dto.*;
import com.blue.auth.service.IpWhitelistService;
import com.blue.auth.service.MyInfoService;
import com.blue.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoController {
  
  private final MyInfoService myInfoService;
  private final IpWhitelistService ipWhitelistService;
  
  // 내 정보 조회
  @GetMapping
  public ResponseEntity<MyInfoResponse> getMe(Authentication auth) {
    String email = auth.getName();
//    System.out.println(email);
    MyInfoResponse me = myInfoService.getMeByEmail(email);
    return ResponseEntity.ok(me);
  }
  
  // 전화번호 변경
  @PutMapping("/phone")
  public ResponseEntity<Void> updatePhone(@RequestBody String phone, Authentication auth) {
    String email = auth.getName();
//    System.out.println(phone);
    myInfoService.updatePhone(email, phone);
    return ResponseEntity.ok().build();
  }
  
  // 비밀번호 변경
  @PutMapping("/password")
  public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
    String email = auth.getName();
//    System.out.println(email);
    myInfoService.changePassword(email, req.getCurrentPassword(), req.getNewPassword());
    return ResponseEntity.ok().build();
  }
  
  // 스프레스 시트 조회
  @GetMapping("/sheet-settings")
  public ResponseEntity<SheetSettingsDto> getSheetSettings(Authentication auth) {
    String email = auth.getName();
    return ResponseEntity.ok(myInfoService.getSheetSettings(email));
  }
  
  // 스프레드 시트 수정
  @PutMapping("/sheet-settings")
  public ResponseEntity<Void> putSheetSettings(@RequestBody SheetSettingsDto dto, Authentication auth) {
    String email = auth.getName();
    myInfoService.saveSheetSettings(email, dto);
    return ResponseEntity.ok().build();
  }
  
  // 팀 목록 (특별계정 전용)
  @GetMapping("/centers")
  public ResponseEntity<List<CenterDto>> list(Authentication auth) {
    String email = auth.getName();
    return ResponseEntity.ok(myInfoService.listCenters(email));
  }
  
  // 팀 추가 (특별계정 전용)
  @PostMapping("/centers")
  public ResponseEntity<Void> add(@RequestBody CreateCenterRequest req, Authentication auth) {
    String email = auth.getName();
    myInfoService.addCenter(email, req.getCenterName());
    return ResponseEntity.ok().build();
  }
  
  // 팀 삭제 (특별계정 전용, 직원 존재 시 409)
  @DeleteMapping("/centers/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id, Authentication auth) {
    String email = auth.getName();
    myInfoService.removeCenter(email, id);
    return ResponseEntity.ok().build();
  }
  
  /** 접속 로그 엑셀 (기간 내 전체 사용자, login_at 기준) */
  @GetMapping("/logs/export")
  public ResponseEntity<byte[]> exportLoginLogs(Authentication auth,
                                                @RequestParam("from") String fromYmd,
                                                @RequestParam("to") String toYmd) {
    return myInfoService.exportLoginLogsExcel(auth.getName(), fromYmd, toYmd);
  }
  
  /** 위임 대상 조회 (userId 기준) */
  @GetMapping("/delegate/lookup")
  public ResponseEntity<MyInfoResponse> lookup(@RequestParam("email") String email,
                                               Authentication auth) {
    return ResponseEntity.ok(myInfoService.lookupDelegateTargetByEmail(auth.getName(), email));
  }
  
  /** 슈퍼 권한 위임 */
  @PostMapping("/delegate")
  public ResponseEntity<Void> delegate(@RequestBody DelegateRequest req, Authentication auth) {
    myInfoService.delegateSuper(auth.getName(), req.userId());
    return ResponseEntity.ok().build();
  }
  
  // POST 바디 바인딩용 경량 DTO (record → 불변, 보일러플레이트 없음)
  public static record DelegateRequest(Long userId) {}
  
  // IP 화이트리스트 관련
  // IP화이트리스트 목록 조회 (슈퍼 전용)
  @GetMapping("/ip-whitelist")
  public ResponseEntity<List<IpWhitelistDto>> getIpWhitelist(Authentication auth) {
    requireSuper(auth);
    return ResponseEntity.ok(ipWhitelistService.getAll());
  }
  
  // IP화이트리스트 신규 등록 (슈퍼 전용)
  @PostMapping("/ip-whitelist")
  public ResponseEntity<Void> addIp(@RequestBody IpWhitelistDto body,
                                    Authentication auth) {
    requireSuper(auth);
    ipWhitelistService.create(body);
    return ResponseEntity.ok().build();
  }
  
  // IP화이트리스트 수정 (슈퍼 전용)
  @PutMapping("/ip-whitelist/{ipId}")
  public ResponseEntity<Void> updateIp(@PathVariable Long ipId,
                                       @RequestBody IpWhitelistDto body,
                                       Authentication auth) {
    requireSuper(auth);
    ipWhitelistService.update(ipId, body);
    return ResponseEntity.ok().build();
  }
  
  // IP화이트리스트 삭제(비활성화, 슈퍼 전용)
  @DeleteMapping("/ip-whitelist/{ipId}")
  public ResponseEntity<Void> deleteIp(@PathVariable Long ipId,
                                       Authentication auth) {
    requireSuper(auth);
    ipWhitelistService.deactivate(ipId);
    return ResponseEntity.ok().build();
  }
  
  // TODO 슈퍼계정인지 검사하는건 서비스에서 하면 되는거 아니감 ㅋㅋ
  /** 슈퍼계정인지 검사 (아니면 403) */
  private void requireSuper(Authentication auth) {
    String email = auth.getName();
    MyInfoResponse me = myInfoService.getMeByEmail(email);
    
    if (me == null || me.getGrants() == null || !me.getGrants().isSuper()) {
      throw new AuthException("슈퍼계정만 사용할 수 있습니다.", HttpStatus.FORBIDDEN);
    }
  }
}
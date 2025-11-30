package com.blue.auth.mapper;

import com.blue.auth.dto.BranchDto;
import com.blue.auth.dto.CenterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgAdminMapper {
  
  // ===== BRANCHES =====
  List<BranchDto> findBranches(@Param("keyword") String keyword);
  
  boolean existsBranchName(@Param("branchName") String branchName);
  
  int insertBranch(BranchDto dto);
  
  int updateBranchName(BranchDto dto);
  
  int countCentersInBranch(@Param("branchId") Long branchId);
  
  int deleteBranch(@Param("branchId") Long branchId);
  
  // ===== CENTERS =====
  List<CenterDto> findCenters(@Param("keyword") String keyword);
  
  boolean existsCenterName(@Param("centerName") String centerName);
  
  int insertCenter(CenterDto dto);
  
  int countUsersInCenter(@Param("centerId") Long centerId);
  
  int updateCenter(@Param("centerId") Long centerId,
                   @Param("centerName") String centerName,
                   @Param("branchId") Long branchId);
  
  int deleteCenter(@Param("centerId") Long centerId);
}

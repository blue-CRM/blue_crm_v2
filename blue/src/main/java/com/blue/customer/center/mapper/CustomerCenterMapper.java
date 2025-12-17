package com.blue.customer.center.mapper;

import com.blue.customer.all.dto.UserContextDto;
import com.blue.customer.center.dto.BranchSimpleDto;
import com.blue.customer.center.dto.CenterDbRowDto;
import com.blue.customer.center.dto.CenterSimpleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerCenterMapper {
  
  UserContextDto findUserContextByEmail(@Param("email") String email);
  
  List<BranchSimpleDto> findBranchesExcludingHQ();
  
  List<CenterSimpleDto> findCentersByBranchId(@Param("branchId") Long branchId);
  
  Long findExpertIdByUserId(@Param("userId") Long userId);
  
  List<CenterDbRowDto> findForAdmin(@Param("offset") int offset,
                                    @Param("size") int size,
                                    @Param("keyword") String keyword,
                                    @Param("dateFrom") String dateFrom,
                                    @Param("dateTo") String dateTo,
                                    @Param("category") String category,
                                    @Param("division") String division,
                                    @Param("sort") String sort,
                                    @Param("expertName") String expertName,
                                    @Param("status") String status,
                                    @Param("branchId") Long branchId,
                                    @Param("centerId") Long centerId,
                                    @Param("visible") String visible);
  
  int countForAdmin(@Param("keyword") String keyword,
                    @Param("dateFrom") String dateFrom,
                    @Param("dateTo") String dateTo,
                    @Param("category") String category,
                    @Param("division") String division,
                    @Param("expertName") String expertName,
                    @Param("status") String status,
                    @Param("branchId") Long branchId,
                    @Param("centerId") Long centerId,
                    @Param("visible") String visible);
  
  List<CenterDbRowDto> findForCenterHead(@Param("offset") int offset,
                                         @Param("size") int size,
                                         @Param("keyword") String keyword,
                                         @Param("dateFrom") String dateFrom,
                                         @Param("dateTo") String dateTo,
                                         @Param("category") String category,
                                         @Param("division") String division,
                                         @Param("sort") String sort,
                                         @Param("expertName") String expertName,
                                         @Param("status") String status,
                                         @Param("branchId") Long branchId,
                                         @Param("centerId") Long centerId,
                                         @Param("visible") String visible,
                                         @Param("myCenterId") Long myCenterId);
  
  int countForCenterHead(@Param("keyword") String keyword,
                         @Param("dateFrom") String dateFrom,
                         @Param("dateTo") String dateTo,
                         @Param("category") String category,
                         @Param("division") String division,
                         @Param("expertName") String expertName,
                         @Param("status") String status,
                         @Param("branchId") Long branchId,
                         @Param("centerId") Long centerId,
                         @Param("visible") String visible,
                         @Param("myCenterId") Long myCenterId);
  
  List<CenterDbRowDto> findForExpert(@Param("offset") int offset,
                                     @Param("size") int size,
                                     @Param("keyword") String keyword,
                                     @Param("dateFrom") String dateFrom,
                                     @Param("dateTo") String dateTo,
                                     @Param("category") String category,
                                     @Param("division") String division,
                                     @Param("sort") String sort,
                                     @Param("status") String status,
                                     @Param("branchId") Long branchId,
                                     @Param("centerId") Long centerId,
                                     @Param("visible") String visible,
                                     @Param("myExpertId") Long myExpertId);
  
  int countForExpert(@Param("keyword") String keyword,
                     @Param("dateFrom") String dateFrom,
                     @Param("dateTo") String dateTo,
                     @Param("category") String category,
                     @Param("division") String division,
                     @Param("status") String status,
                     @Param("branchId") Long branchId,
                     @Param("centerId") Long centerId,
                     @Param("visible") String visible,
                     @Param("myExpertId") Long myExpertId);
}

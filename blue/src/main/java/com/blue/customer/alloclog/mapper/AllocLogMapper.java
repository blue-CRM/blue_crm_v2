package com.blue.customer.alloclog.mapper;

import com.blue.customer.alloclog.dto.AllocLogInsertDto;
import com.blue.customer.alloclog.dto.CustomerOwnerSnapshotDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AllocLogMapper {
  
  void insertLogs(@Param("logs") List<AllocLogInsertDto> logs);
  List<CustomerOwnerSnapshotDto> findOwnersByCustomerIds(@Param("ids") List<Long> ids);

}

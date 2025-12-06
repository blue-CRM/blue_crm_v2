package com.blue.auth.mapper;

import com.blue.auth.dto.IpWhitelistDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IpWhitelistMapper {
  
  int countActiveByIp(@Param("ip") String ip);
  
  List<IpWhitelistDto> findAll();
  
  int insert(IpWhitelistDto dto);
  
  int update(IpWhitelistDto dto);
  
  int deactivate(@Param("ipId") Long ipId);
  
  int deleteHard(@Param("ipId") Long ipId);
}

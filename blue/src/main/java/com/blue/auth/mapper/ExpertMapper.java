package com.blue.auth.mapper;

import com.blue.auth.dto.ExpertDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpertMapper {
  
  List<ExpertDto> findAll(@Param("keyword") String keyword);
  
  int insert(@Param("expertName") String expertName);
  
  int rename(@Param("expertId") Long expertId,
             @Param("expertName") String expertName);
  
  int delete(@Param("expertId") Long expertId);
  
  int countUsersUsing(@Param("expertId") Long expertId);
}
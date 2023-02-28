package com.stock.mx2.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.SiteTaskLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SiteTaskLogMapper  extends BaseMapper<SiteTaskLog> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(SiteTaskLog paramSiteTaskLog);

  SiteTaskLog selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(SiteTaskLog paramSiteTaskLog);

  int updateByPrimaryKey(SiteTaskLog paramSiteTaskLog);

  List taskList(@Param("keywork") String keywork,@Param("taskType") String paramString);
}

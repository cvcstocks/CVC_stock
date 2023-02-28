package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteFuturesSetting;
import java.util.List;

public interface SiteFuturesSettingMapper {
  int deleteByPrimaryKey(Integer paramInteger);
  
  int insert(SiteFuturesSetting paramSiteFuturesSetting);
  
  int insertSelective(SiteFuturesSetting paramSiteFuturesSetting);
  
  SiteFuturesSetting selectByPrimaryKey(Integer paramInteger);
  
  int updateByPrimaryKeySelective(SiteFuturesSetting paramSiteFuturesSetting);
  
  int updateByPrimaryKey(SiteFuturesSetting paramSiteFuturesSetting);
  
  List selectAllSiteSetting();
}

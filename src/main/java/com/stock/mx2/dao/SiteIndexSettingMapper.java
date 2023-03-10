package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteIndexSetting;
import java.util.List;

public interface SiteIndexSettingMapper {
  int deleteByPrimaryKey(Integer paramInteger);

  int insert(SiteIndexSetting paramSiteIndexSetting);

  int insertSelective(SiteIndexSetting paramSiteIndexSetting);

  SiteIndexSetting selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(SiteIndexSetting paramSiteIndexSetting);

  int updateByPrimaryKey(SiteIndexSetting paramSiteIndexSetting);

  List selectAllSiteIndexSetting();
}

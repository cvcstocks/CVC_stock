package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteInfo;
import java.util.List;

public interface SiteInfoMapper {
  int deleteByPrimaryKey(Integer paramInteger);

  int insert(SiteInfo paramSiteInfo);

  int insertSelective(SiteInfo paramSiteInfo);

  SiteInfo selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(SiteInfo paramSiteInfo);

  int updateByPrimaryKey(SiteInfo paramSiteInfo);

  List findAll();
}

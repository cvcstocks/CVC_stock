package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteBanner;
import java.util.List;

public interface SiteBannerMapper {
  int deleteByPrimaryKey(Integer paramInteger);

  int insert(SiteBanner paramSiteBanner);

  int insertSelective(SiteBanner paramSiteBanner);

  SiteBanner selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(SiteBanner paramSiteBanner);

  int updateByPrimaryKey(SiteBanner paramSiteBanner);

  List listByAdmin();

  List getBannerByMobile();

  List getBannerByPC();
}

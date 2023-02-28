package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteBanner;

public interface ISiteBannerService {
  ServerResponse add(SiteBanner paramSiteBanner);
  
  ServerResponse listByAdmin(int paramInt1, int paramInt2);
  
  ServerResponse update(SiteBanner paramSiteBanner);
  
  ServerResponse delete(Integer paramInteger);
  
  ServerResponse getBannerByPlat(String paramString);
}

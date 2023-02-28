package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteAdmin;

import javax.servlet.http.HttpServletRequest;

public interface ISiteAdminService {
  ServerResponse login(String paramString1, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest);

  ServerResponse<PageInfo> listByAdmin(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse authCharge(String paramString1, Integer paramInteger, String paramString2);

  ServerResponse updateLock(Integer paramInteger);

  ServerResponse add(SiteAdmin paramSiteAdmin);

  ServerResponse update(SiteAdmin paramSiteAdmin);

  SiteAdmin findAdminByName(String paramString);

  SiteAdmin findAdminByPhone(String paramString);

  ServerResponse count();

  ServerResponse deleteAdmin(Integer adminId);
}

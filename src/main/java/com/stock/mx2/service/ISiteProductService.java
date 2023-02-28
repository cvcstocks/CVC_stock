package com.stock.mx2.service;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteProduct;

public interface ISiteProductService {
  ServerResponse update(SiteProduct paramSiteProduct);

  SiteProduct getProductSetting();
}

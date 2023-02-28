package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;

public interface ISmsService {
  ServerResponse sendAliyunSMS(String paramString1, String paramString2);
}

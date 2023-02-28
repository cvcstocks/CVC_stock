package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IFileUploadService {
  ServerResponse upload(MultipartFile paramMultipartFile, String paramString);
}

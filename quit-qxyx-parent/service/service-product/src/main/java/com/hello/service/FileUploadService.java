package com.hello.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * 上传图片功能
     * @param file
     * @return
     */
    String fileUpload(MultipartFile file);
}

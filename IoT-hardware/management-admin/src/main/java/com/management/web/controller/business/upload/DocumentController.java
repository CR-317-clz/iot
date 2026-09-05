package com.management.web.controller.business.upload;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.management.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping
@CrossOrigin
public class DocumentController {

    @Value("${aliyun.oss.endpoint}")
    private String ENDPOINT;

    @Value("${aliyun.oss.access-key-id}")
    private String ACCESS_KEY_ID;

    @Value("${aliyun.oss.access-key-secret}")
    private String ACCESS_KEY_SECRET;

    @Value("${aliyun.oss.bucket-name}")
    private String BUCKET_NAME;

    /**
     * aliyun oss上传
     */
    @PostMapping("/upload_oss")
    public AjaxResult uploadOss(@RequestParam("file") MultipartFile file) {
        String publicUrl = "";
        if (file.isEmpty()) {
            return AjaxResult.error("上传文件不能为空");
        }

        // 生成 OSS 文件名：使用 UUID 保证唯一性
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String objectName = "IOT/" + UUID.randomUUID() + extension;

        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();

            // 初始化 OSS 客户端
            ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

            // 上传文件
            ossClient.putObject(BUCKET_NAME, objectName, inputStream);

            // 设置公共读
            ossClient.setObjectAcl(BUCKET_NAME, objectName, CannedAccessControlList.PublicRead);

            // 拼接公网 URL
            publicUrl = String.format("https://%s.%s/%s",
                    BUCKET_NAME,
                    ENDPOINT.replace("https://", ""),
                    objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件失败：{}", e.getMessage());
            return AjaxResult.error("上传文件失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            if (inputStream != null) {
                try { inputStream.close(); } catch (Exception ignored) {}
            }
        }
        return AjaxResult.success(publicUrl);
    }

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 图片
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            // 视频
            ".mp4", ".avi", ".mov", ".mkv", ".flv", ".wmv",
            // 文档
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    ));

    /**
     * 获取文件扩展名并校验合法性
     * @param filename 文件原始名
     * @return 合法扩展名（带点），不合法返回默认 ".jpg"
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase(); // 转小写
            // 去掉 URL 参数（防止 ?v=1.2）
            if (ext.contains("?")) {
                ext = ext.substring(0, ext.indexOf("?"));
            }
            // 简单长度校验
            if (ext.length() >= 2 && ext.length() <= 5 && ALLOWED_EXTENSIONS.contains(ext)) {
                return ext;
            }
        }
        // 默认扩展名
        return ".jpg";
    }

}

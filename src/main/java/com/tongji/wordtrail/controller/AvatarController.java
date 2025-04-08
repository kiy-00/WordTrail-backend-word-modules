package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import cn.hutool.core.io.FileUtil;

@RestController
@RequestMapping("/account")
public class AvatarController {

    private final AdminService adminService;

    @Autowired
    public AvatarController(AdminService adminService) {
        this.adminService = adminService;
    }

    private static final String ABSOLUTE_FILE_PATH = "D:program3\\Documents\\upload\\images\\";
    @PostMapping("/UploadAvatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        System.out.println(System.getProperty("user.dir"));
        String originName = file.getOriginalFilename();
        // 1、文件名加个时间戳
        String fileName = FileUtil.mainName(originName) + System.currentTimeMillis() + "." + FileUtil.extName(originName);
        System.out.println(fileName);

        // 2、文件存放的绝对路径 存放名称
        String storageName = ABSOLUTE_FILE_PATH + fileName;

        // 3. 文件上传
        File dest = new File(storageName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> response = new HashMap<>();

        String avatarUrl ="http://localhost:8282/images/" + fileName;
        // 存储到数据库中
        if (adminService.findByUsername(username, avatarUrl)){
            // 构造响应体
            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);


            response.put("code", 200);
            response.put("message", "头像上传成功");
            response.put("data", data);

            return ResponseEntity.ok(response);
        }
        else {
            response.put("code", 400);
            response.put("message", "保存到数据库失败");
            return ResponseEntity.badRequest().body(response);
        }



    }
    /*private final AdminService adminService;

    @Autowired
    public AvatarController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/UploadAvatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("code", 400);
            response.put("message", "上传的文件为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 确保路径存在
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String newFileName = UUID.randomUUID() + extension;
            File savedFile = new File(dir, newFileName);
            file.transferTo(savedFile); // 保存文件


            // 获取图片信息
            BufferedImage image = ImageIO.read(savedFile);
            int width = image.getWidth();
            int height = image.getHeight();
            long fileSize = savedFile.length();

            // 构造访问URL
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String AvatarUrl = baseUrl + "/" + uploadDir + newFileName;
            String avatarUrl = encodeAvatarUrl(AvatarUrl);
            System.out.println(avatarUrl);
            // 存储到数据库中
            if (adminService.findByUsername(username, avatarUrl)){
                // 构造响应体
                Map<String, Object> data = new HashMap<>();
                data.put("avatarUrl", avatarUrl);
                data.put("fileSize", fileSize);
                data.put("width", width);
                data.put("height", height);

                response.put("code", 200);
                response.put("message", "头像上传成功");
                response.put("data", data);

                return ResponseEntity.ok(response);
            }
            else {
                response.put("code", 400);
                response.put("message", "保存到数据库失败");
                return ResponseEntity.badRequest().body(response);
            }



        } catch (IOException e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public static String encodeAvatarUrl(String avatarUrl) {
        try {
            return URLEncoder.encode(avatarUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return avatarUrl; // 如果编码失败，返回原始 URL
        }
    }*/

}


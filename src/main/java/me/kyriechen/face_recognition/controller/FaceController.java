package me.kyriechen.face_recognition.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.arcsoft.face.FaceInfo;
import me.kyriechen.face_recognition.dto.*;
import me.kyriechen.face_recognition.enums.ErrorCodeEnum;
import me.kyriechen.face_recognition.service.FaceEngineService;
import me.kyriechen.face_recognition.utils.ImageUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;


@RestController
public class FaceController {

    public final static Logger logger = LoggerFactory.getLogger(FaceController.class);


    @Autowired
    FaceEngineService faceEngineService;
    @Value("${config.face-file-path}")
    private String faceFilePath;

    @RequestMapping(value = "/demo")
    public String demo() {
        return "demo";
    }

    @RequestMapping(value = "/faceSearchByImage")
    public Result<FaceSearchResDto> faceSearchByImage(MultipartFile file) throws Exception {
        File tmp = File.createTempFile("tmp.jpg", null);
        file.transferTo(tmp);
        FileInputStream inputFile = new FileInputStream(tmp);
        byte[] buffer = new byte[inputFile.available()];
        inputFile.read(buffer);
        inputFile.close();
        return faceSearch(Base64.encodeBase64String(buffer));
    }

    @RequestMapping(value = "/faceSearch", method = RequestMethod.POST)
    @ResponseBody
    public Result<FaceSearchResDto> faceSearch(@RequestBody String base64File) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(base64File));
        BufferedImage bufImage = ImageIO.read(inputStream);
        if (null == bufImage) return Results.newFailedResult(ErrorCodeEnum.NO_FACE_DETECTED);
        ImageInfo imageInfo = ImageUtil.bufferedImage2ImageInfo(bufImage);
        inputStream.close();
        //人脸特征获取
        byte[] bytes = faceEngineService.extractFaceFeature(imageInfo);
        if (bytes == null) {
            return Results.newFailedResult(ErrorCodeEnum.NO_FACE_DETECTED);
        }
        //人脸比对，获取比对结果
        List<FaceUserInfo> userFaceInfoList = faceEngineService.compareFaceFeature(bytes, 1);
        if (CollectionUtil.isNotEmpty(userFaceInfoList)) {
            FaceUserInfo faceUserInfo = userFaceInfoList.get(0);
            FaceSearchResDto faceSearchResDto = new FaceSearchResDto();
            BeanUtil.copyProperties(faceUserInfo, faceSearchResDto);
            List<ProcessInfo> processInfoList = faceEngineService.process(imageInfo);
            if (CollectionUtil.isNotEmpty(processInfoList)) {
                //人脸检测
                List<FaceInfo> faceInfoList = faceEngineService.detectFaces(imageInfo);
                int left = faceInfoList.get(0).getRect().getLeft();
                int top = faceInfoList.get(0).getRect().getTop();
                int width = faceInfoList.get(0).getRect().getRight() - left;
                int height = faceInfoList.get(0).getRect().getBottom() - top;
                Graphics2D graphics2D = bufImage.createGraphics();
                graphics2D.setColor(Color.RED);//红色
                BasicStroke stroke = new BasicStroke(5f);
                graphics2D.setStroke(stroke);
                graphics2D.drawRect(left, top, width, height);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufImage, "jpg", outputStream);
                byte[] bytes1 = outputStream.toByteArray();
                faceSearchResDto.setImage("data:image/jpeg;base64," + Base64Utils.encodeToString(bytes1));
                faceSearchResDto.setAge(processInfoList.get(0).getAge());
                faceSearchResDto.setGender(processInfoList.get(0).getGender().equals(1) ? "女" : "男");
            }
            Result<FaceSearchResDto> resDtoResult = Results.newSuccessResult(faceSearchResDto);
            logger.info(resDtoResult.toString());
            return resDtoResult;
        }
        logger.info("unknown people");
        base64ToFile(faceFilePath + "/other", base64File, System.currentTimeMillis() + ".jpg");
//        return Results.newFailedResult(ErrorCodeEnum.FACE_DOES_NOT_MATCH);
        return Results.newSuccessResult(FaceSearchResDto.builder().name("客人").build());
    }

    @RequestMapping(value = "/detectFaces", method = RequestMethod.POST)
    @ResponseBody
    public List<FaceInfo> detectFaces(String image) throws IOException {
        byte[] bytes = Base64Utils.decodeFromString(image.trim());
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ImageInfo imageInfo = ImageUtil.getRGBData(inputStream);
        inputStream.close();
        return faceEngineService.detectFaces(imageInfo);
    }

    @GetMapping("/refresh")
    public void refresh() {
        faceEngineService.loadFace();
    }

    private void base64ToFile(String destPath, String base64, String fileName) {
        File file = null;
        //创建文件目录
        String filePath = destPath;
        File dir = new File(filePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        BufferedOutputStream bos = null;
        java.io.FileOutputStream fos = null;
        try {
//            byte[] bytes = Base64.getDecoder().decode(base64);
            byte[] bytes = Base64.decodeBase64(base64);
            file = new File(filePath + "/" + fileName);
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

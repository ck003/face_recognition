package me.kyriechen.face_recognition.service;

import com.arcsoft.face.FaceInfo;
import me.kyriechen.face_recognition.dto.FaceUserInfo;
import me.kyriechen.face_recognition.dto.ImageInfo;
import me.kyriechen.face_recognition.dto.ProcessInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;


public interface FaceEngineService {

    void addFaceToCache(File file, String name) throws InterruptedException, FileNotFoundException;


    List<FaceInfo> detectFaces(ImageInfo imageInfo);

    List<ProcessInfo> process(ImageInfo imageInfo);

    /**
     * 人脸特征
     *
     * @param imageInfo
     * @return
     */
    byte[] extractFaceFeature(ImageInfo imageInfo) throws InterruptedException;

    /**
     * 人脸比对
     *
     * @param groupId
     * @param faceFeature
     * @return
     */
    List<FaceUserInfo> compareFaceFeature(byte[] faceFeature, Integer groupId) throws InterruptedException, ExecutionException;

    void loadFace();
}

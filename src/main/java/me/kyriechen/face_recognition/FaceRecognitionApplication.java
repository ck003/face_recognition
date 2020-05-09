package me.kyriechen.face_recognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FaceRecognitionApplication {


    public static void main(String[] args) {
        SpringApplication.run(FaceRecognitionApplication.class, args);
    }

}

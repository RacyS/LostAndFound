package com.example.lostandfound.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", System.getenv("CLOUDINARY_NAME"),
                "api_key", System.getenv("CLOUDINARY_API_KEY"),    // ดูในหน้า Dashboard (Go to API Keys)
                "api_secret", System.getenv("CLOUDINARY_API_SECRET") // ดูในหน้า Dashboard (Go to API Keys)
        ));
    }
}
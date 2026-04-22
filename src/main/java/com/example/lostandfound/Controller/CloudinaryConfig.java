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
                "cloud_name", "di05lzzcd",
                "api_key", "391838768798151",    // ดูในหน้า Dashboard (Go to API Keys)
                "api_secret", "gn6pdJKCm2x8W5gEuEPVlbwMaNw" // ดูในหน้า Dashboard (Go to API Keys)
        ));
    }
}
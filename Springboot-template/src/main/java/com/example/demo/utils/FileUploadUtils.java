package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Paths;

public class FileUploadUtils {
    public static final String realPath = String.valueOf(Paths.get(System.getProperty("user.dir"),
            "src","main", "resources","static", "uploads"));

}

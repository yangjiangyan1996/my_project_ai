package com.example.utils;

import com.alibaba.excel.EasyExcel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExcelUtils {
    public static <T> List<T> readExcel(MultipartFile file, Class<T> clazz) {
        try (InputStream inputStream = file.getInputStream()) {
            return EasyExcel.read(inputStream)
                    .head(clazz)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            throw new RuntimeException("读取Excel失败", e);
        }
    }
}

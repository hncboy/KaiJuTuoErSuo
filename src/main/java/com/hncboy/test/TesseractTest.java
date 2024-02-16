package com.hncboy.test;

import cn.hutool.core.util.StrUtil;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class TesseractTest {

    public static void main(String[] args) {
        ITesseract instance = new Tesseract();
        // 语言库的路径
        instance.setDatapath("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\tessdata");
        // 设置语言为中文， eng为英文，chi_sim为简体中文
        instance.setLanguage("eng");
        try {
            long startTime = System.currentTimeMillis();
            // 执行OCR操作
            Map<String, Integer> resultMap = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                String result = instance.doOCR(new File("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\cropped\\cropped_image_5_2.jpg"));
                result = StrUtil.trim(result);
                resultMap.put(result, resultMap.getOrDefault(result, 0) + 1);
            }

            System.out.println(resultMap);

            long endTime = System.currentTimeMillis();
            System.out.println("Time is：" + (endTime - startTime) + " 毫秒");
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
    }
}

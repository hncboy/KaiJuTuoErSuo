package com.hncboy.executor;

import com.hncboy.domain.RectangleInfo;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class TesseractExecutor {

    private final ITesseract instance;

    public TesseractExecutor() {
        instance = new Tesseract();
        instance.setDatapath("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\tessdata");
        instance.setLanguage("eng");
    }

    /**
     * 执行 OCR 操作
     *
     * @param path 图片路径
     * @return 识别结果
     */
    public String doOcr(String path) {
        try {
            File imageFile = new File(path);
            // 执行 OCR 操作
            return instance.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return null;
    }
}

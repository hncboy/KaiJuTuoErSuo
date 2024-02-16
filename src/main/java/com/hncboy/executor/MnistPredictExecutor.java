package com.hncboy.executor;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.hncboy.domain.RectangleInfo;
import com.hncboy.domain.ScreenCapInfo;
import com.hncboy.test.mnist.MnistPredictor;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hncboy
 * @date 2024/2/14
 * 数字预测执行器
 */
public class MnistPredictExecutor {

    private final TesseractExecutor tesseractExecutor;

    public MnistPredictExecutor(TesseractExecutor tesseractExecutor) {
        this.tesseractExecutor = tesseractExecutor;
    }

    /**
     * 预测方块数字
     *
     * @param locations 方块位置
     * @param screenCapInfo 截屏信息
     */
    public void predict(List<List<RectangleInfo>> locations, ScreenCapInfo screenCapInfo) {
        // 读取原始图像
        Mat originalMat = Imgcodecs.imread(screenCapInfo.getScreenCapComputerPath());

        // 开启多线程，不然预测速度太慢
        CountDownLatch countDownLatch = new CountDownLatch(160);
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        int[][] predictNumbers = new int[locations.size()][];
        for (int i = 0; i < locations.size(); i++) {
            predictNumbers[i] = new int[locations.get(i).size()];
            for (int j = 0; j < locations.get(i).size(); j++) {
                RectangleInfo rectangleInfo = locations.get(i).get(j);
                // 创建矩形对象
                Rect rect = new Rect(rectangleInfo.getX(), rectangleInfo.getY(), rectangleInfo.getWidth(), rectangleInfo.getHeight());
                // 切割图像
                Mat croppedImg = new Mat(originalMat, rect);
                // 切割图像的名称
                String croppedImgPath = StrUtil.format("{}\\cropped\\cropped_image_{}_{}.jpg", screenCapInfo.getScreenCapComputerDir(), i, j);
                // 保存切割后的图像
                Imgcodecs.imwrite(croppedImgPath, croppedImg);
                // ocr 识别，这里不支持多线程，只能放外面
                String tesseractResult = tesseractExecutor.doOcr(croppedImgPath);
                tesseractResult = StrUtil.trim(tesseractResult);
                String finalTesseractResult = tesseractResult;
                // 这里使用多线程
                executorService.execute(() -> {
                    int predictNumber = predictSingle(rectangleInfo, finalTesseractResult, croppedImgPath);
                    predictNumbers[rectangleInfo.getIndexI()][rectangleInfo.getIndexJ()] = predictNumber;
                    countDownLatch.countDown();
                    System.out.printf("还剩%s个数字等待预测\n", countDownLatch.getCount());
                });
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 这里预留修改手动数字
        System.out.println(Arrays.deepToString(predictNumbers));

        // 将预测结果填充到方块对象中
        for (int i = 0; i < predictNumbers.length; i++) {
            for (int j = 0; j < predictNumbers[i].length; j++) {
                locations.get(i).get(j).setNumber(predictNumbers[i][j]);
            }
        }
        System.out.println("预测完成");
    }

    /**
     * 预测单个方块数字
     *
     * @param rectangleInfo 方块信息
     * @param tesseractResult ocr 识别结果
     * @param croppedImgPath 切割图像路径
     * @return 预测结果
     */
    private int predictSingle(RectangleInfo rectangleInfo, String tesseractResult, String croppedImgPath) {
        // 两种预测方式都有误差，所以两种预测方式都要执行
        // 如果是数字并且是小于 10 的数字
        if (NumberUtil.isInteger(tesseractResult) && tesseractResult.length() == 1) {
            int result = NumberUtil.parseInt(tesseractResult);
            // 如果是数字，直接使用
            rectangleInfo.setUseTesseract(true);
            return result;
        }

        try {
            // 使用模型识别数字，这里速度比较慢
            int predictResult = predict(croppedImgPath);
            rectangleInfo.setUseTesseract(false);
            return predictResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int predict(String path) throws Exception {
        // 加载预训练好的模型
        MnistPredictor predictor = new MnistPredictor();

        // 加载图片
        BufferedImage img = ImageIO.read(new File(path));
        int width = img.getWidth();
        int height = img.getHeight();

        // 将RGB图片变为灰度图，并将每个像素存于二维数组
        int[][] grayArray = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = img.getRGB(j, i);
                int gray = (int) (0.2989 * ((rgb >> 16) & 0xff) + 0.5870 * ((rgb >> 8) & 0xff) + 0.1140 * (rgb & 0xff));
                // 二值化
                if (gray <= 120)
                    grayArray[i][j] = 255;
            }
        }

        // 对当前的二维数组resize，因为训练好的模型要求输入图像大小为28×28
        int[][] expandedSubArray = resizeImage(grayArray, 28, 28);

        // 保存，看看处理后的图像是什么样的，检查有没有处理错误
        save(expandedSubArray, "E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\mnist_predict.png");

        // 预测
        int prediction = predictor.predict(expandedSubArray);
        return prediction;
    }

    private static void save(int[][] data, String path) {
        int width = data[0].length;
        int height = data.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = data[y][x];
                int rgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgb);
            }
        }

        try {
            ImageIO.write(image, "png", new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[][] resizeImage(int[][] input, int newWidth, int newHeight) {
        int[][] output = new int[newWidth][newHeight];
        int height = input.length;
        int width = input[0].length;
        float widthRatio = (float) width / newWidth;
        float heightRatio = (float) height / newHeight;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int px = (int) (x * widthRatio);
                int py = (int) (y * heightRatio);
                float xDiff = (x * widthRatio) - px;
                float yDiff = (y * heightRatio) - py;
                int pixelTopLeft = input[py][px];
                int pixelTopRight = (px == width - 1) ? pixelTopLeft : input[py][px + 1];
                int pixelBottomLeft = (py == height - 1) ? pixelTopLeft : input[py + 1][px];
                int pixelBottomRight = (px == width - 1 || py == height - 1) ? pixelBottomLeft : input[py + 1][px + 1];
                float topAvg = pixelTopLeft + xDiff * (pixelTopRight - pixelTopLeft);
                float bottomAvg = pixelBottomLeft + xDiff * (pixelBottomRight - pixelBottomLeft);
                float avg = topAvg + yDiff * (bottomAvg - topAvg);
                if (avg <= 100) {
                    output[y][x] = 0;
                } else {
                    output[y][x] = 255;
                }
            }
        }
        return output;
    }
}

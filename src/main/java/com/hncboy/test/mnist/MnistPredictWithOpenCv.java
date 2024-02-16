package com.hncboy.test.mnist;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class MnistPredictWithOpenCv {

    public static void main(String[] args) throws Exception {
        // 加载OpenCV库
        URL url = ClassLoader.getSystemResource("lib/x64/opencv_java450.dll");
        System.load(url.getPath());
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 加载预训练好的模型
        MnistPredictor predictor = new MnistPredictor();

        // 灰度二值化图像
        Mat grayAndthreshMat = Imgcodecs.imread("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\cropped_image.jpg");

        // 调整图像大小为28x28，因为训练好的模型要求输入图像大小为28×28
        Mat resizedMat = new Mat();
        Imgproc.resize(grayAndthreshMat, resizedMat, new Size(28, 28));
        Imgcodecs.imwrite("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\mnist_predict.png", resizedMat);

        int height = resizedMat.height();
        int width = resizedMat.width();
        int[][] expandedSubArray = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] data = resizedMat.get(y, x);
                expandedSubArray[y][x] = (int) data[0];
            }
        }

        // 预测
        int prediction = predictor.predict(expandedSubArray);
        System.out.println("预测结果是：" + prediction);
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
                if (avg <= 100)
                    output[y][x] = 0;
                else
                    output[y][x] = 255;
//                output[y][x] = (int) avg;
            }
        }
        return output;
    }
}

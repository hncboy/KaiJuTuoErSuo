package com.hncboy.test.mnist;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class MnistPredictTest {

    public static void main(String[] args) throws Exception {
        // 加载预训练好的模型
        MnistPredictor predictor = new MnistPredictor();

        // 加载图片
        BufferedImage img = ImageIO.read(new File("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\cropped\\cropped_image_5_2.jpg"));
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

        for (int i = 0; i < 10; i++) {
            // 预测
            int prediction = predictor.predict(expandedSubArray);
            System.out.printf("第%s次预测：%s\n", i, prediction);
        }
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

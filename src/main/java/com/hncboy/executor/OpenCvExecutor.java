package com.hncboy.executor;

import com.hncboy.domain.RectangleInfo;
import com.hncboy.domain.ScreenCapInfo;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class OpenCvExecutor {

    /**
     * 截屏信息
     */
    private final ScreenCapInfo screenCapInfo;

    public OpenCvExecutor(ScreenCapInfo screenCapInfo) {
        this.screenCapInfo = screenCapInfo;
    }

    static {
        // 加载动态链接库
        URL url = ClassLoader.getSystemResource("lib/x64/opencv_java450.dll");
        System.load(url.getPath());
    }

    /**
     * 检测方块轮廓
     *
     * @return 所有方块位置
     */
    public List<List<RectangleInfo>> detect() {
        // 读取原始图像
        Mat originalMat = Imgcodecs.imread(screenCapInfo.getScreenCapComputerPath());

        // 转换为灰度图像：简化图像信息并减少处理的复杂性
        Mat grayMat = new Mat();
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\gray.png", grayMat);

        // 二值化处理：将灰度图像中的像素值转换为两个值之一（通常是0或255），以便进行更简单的图像分割和分析。
        Mat threshMat = new Mat();
        Imgproc.threshold(grayMat, threshMat, 127, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\thresh.png", threshMat);

        // 寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchyMat = new Mat();
        Imgproc.findContours(threshMat, contours, hierarchyMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // 初始化每行每列的未知信息
        List<List<RectangleInfo>> locations = new ArrayList<>();
        // 总共 16 行
        for (int i = 0; i < 16; i++) {
            List<RectangleInfo> row = new ArrayList<>();
            // 总共 10 列
            for (int j = 0; j < 10; j++) {
                RectangleInfo rectangleInfo = new RectangleInfo();
                rectangleInfo.setIndexI(i);
                rectangleInfo.setIndexJ(j);
                row.add(rectangleInfo);
            }
            locations.add(row);
        }

        // 方框个数，160 个是对的
        int count = 0;

        for (MatOfPoint contour : contours) {
            // 计算轮廓的边界框
            Rect rect = Imgproc.boundingRect(contour);
            // 边界框左上角 x 坐标
            int x = rect.x;
            // 边界框左上角 y 坐标
            int y = rect.y;
            // 边界框宽度
            int width = rect.width;
            // 边界框高度
            int height = rect.height;

            // 小方块的大小差不多 100*100 左右，有不规则轮廓，我们提供 10 左右的误差
            if (width < 90 || width > 110 || height < 90 || height > 110) {
                continue;
            }

            // 计算长宽比，长宽比在 0.95 到 1.05 之间，差不多接近正方形
            double ratio = (double) width / height;
            if (0.95 > ratio || ratio > 1.05) {
                continue;
            }

            // 这里会过滤出 161 个方块，有一个是设置方块

            // 内缩长度，使得坐标位于轮廓内部
            int shrinkLength = 10;

            // 框内坐标，坐标系是以左上角为原点，x 轴向右延伸，y 轴向下延伸
            // 左上角 x 坐标
            int x1 = x + shrinkLength;
            // 左上角 y 坐标
            int y1 = y + shrinkLength;
            double[] color = originalMat.get(y1, x1);
            // 如果颜色不是白色，说明这个方块是设置方块，这里判断要接近白色
            if (color[0] < 240 || color[1] < 240 || color[2] < 240) {
                continue;
            }
            // 计算索引，这里顺序不一定是正的
            int i1 = count / 10;
            int j1 = count % 10;

            // 将坐标放入 locations 中
            RectangleInfo rectangleInfo = locations.get(15 - i1).get(9 - j1);
            rectangleInfo.setX(x);
            rectangleInfo.setY(y);
            rectangleInfo.setHeight(height);
            rectangleInfo.setWidth(width);
            count++;
        }
        return locations;
    }
}

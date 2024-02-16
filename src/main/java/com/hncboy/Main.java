package com.hncboy;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.hncboy.domain.RectangleInfo;
import com.hncboy.domain.ScreenCapInfo;
import com.hncboy.executor.AdbExecutor;
import com.hncboy.executor.CoreExecutor;
import com.hncboy.executor.MnistPredictExecutor;
import com.hncboy.executor.OpenCvExecutor;
import com.hncboy.executor.ScreenCapExecutor;
import com.hncboy.executor.SwipeExecutor;
import com.hncboy.executor.TesseractExecutor;

import java.util.List;

/**
 * @author hncboy
 * @date 2024/2/14
 * 填写注释
 */
public class Main {


    public static void main(String[] args) {
        // 主计时器
        StopWatch mainStopWatch = new StopWatch("main");
        mainStopWatch.start();


        // 1.构建 adb 执行器
        String adbPath = "D:\\Tool\\android-platform-tools\\adb.exe";
        AdbExecutor adbExecutor = new AdbExecutor(adbPath);


        // 2.构建截屏执行器
        StopWatch screenCapStopWatch = new StopWatch("screenCap");
        screenCapStopWatch.start();
        ScreenCapExecutor screenCapExecutor = new ScreenCapExecutor(adbExecutor);
        // 执行截屏
        ScreenCapInfo screenCapInfo = screenCapExecutor.execute();
//        ScreenCapInfo screenCapInfo = new ScreenCapInfo();
//        screenCapInfo.setScreenCapComputerName("Screenshot_20240215_203642.png");
//        screenCapInfo.setScreenCapComputerDir("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop");
//        screenCapInfo.setScreenCapComputerPath("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop\\Screenshot_20240215_203642.png");
        screenCapStopWatch.stop();
        System.out.println("截屏耗时：" + screenCapStopWatch.getTotalTimeMillis() + "ms");


        // 3.检测方块轮廓点位
        StopWatch openCvStopWatch = new StopWatch("openCv");
        openCvStopWatch.start();
        OpenCvExecutor openCvExecutor = new OpenCvExecutor(screenCapInfo);
        List<List<RectangleInfo>> locations = openCvExecutor.detect();
        openCvStopWatch.stop();
        System.out.println("检测方块轮廓耗时：" + openCvStopWatch.getTotalTimeSeconds() + "s");


        // 4.预测方块数字
        StopWatch mnistPredictStopWatch = new StopWatch("mnistPredict");
        mnistPredictStopWatch.start();
        TesseractExecutor tesseractExecutor = new TesseractExecutor();
        MnistPredictExecutor mnistPredictExecutor = new MnistPredictExecutor(tesseractExecutor);
        mnistPredictExecutor.predict(locations, screenCapInfo);
        mnistPredictStopWatch.stop();
        System.out.println("预测方块数字耗时：" + mnistPredictStopWatch.getTotalTimeSeconds() + "s");


        // 5.执行核心查找消除点位算法
        StopWatch coreStopWatch = new StopWatch("corePredict");
        coreStopWatch.start();
        CoreExecutor coreExecutor = new CoreExecutor(locations);
        List<List<RectangleInfo>> rectangleInfoSteps = coreExecutor.execute();
        coreStopWatch.stop();
        System.out.println("核心查找消除点位算法耗时：" + coreStopWatch.getTotalTimeSeconds() + "s");
        int totalCleanCount = rectangleInfoSteps.stream().mapToInt(List::size).sum();
        if (totalCleanCount < 100) {
            System.out.println(StrUtil.format("预计消除方块数量为：{}个，不进行消除", totalCleanCount));
            return;
        }

        // 6.执行滑动操作
        StopWatch swipeStopWatch = new StopWatch("swipe");
        swipeStopWatch.start();
        SwipeExecutor swipeExecutor = new SwipeExecutor(adbExecutor, rectangleInfoSteps);
        swipeExecutor.doSwipe();
        swipeStopWatch.stop();
        System.out.println("滑动耗时：" + swipeStopWatch.getTotalTimeSeconds() + "s");
    }
}

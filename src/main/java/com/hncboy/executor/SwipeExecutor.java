package com.hncboy.executor;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hncboy.domain.RectangleInfo;

import java.util.List;

/**
 * @author hncboy
 * @date 2024/2/15
 * 填写注释
 */
public class SwipeExecutor {

    private final List<List<RectangleInfo>> rectangleInfoSteps;

    private final AdbExecutor adbExecutor;

    public SwipeExecutor(AdbExecutor adbExecutor, List<List<RectangleInfo>> rectangleInfoSteps) {
        this.rectangleInfoSteps = rectangleInfoSteps;
        this.adbExecutor = adbExecutor;
    }

    /**
     * 执行滑动操作
     */
    public void doSwipe() {
        for (int i = 0; i < rectangleInfoSteps.size(); i++) {
            List<RectangleInfo> rectangleInfoSerialSteps = rectangleInfoSteps.get(i);
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> swipePointPairs = calcTopPoint(rectangleInfoSerialSteps);

            // 左上角坐标
            Pair<Integer, Integer> leftTopPointPair = swipePointPairs.getKey();
            Integer x1 = leftTopPointPair.getKey();
            Integer y1 = leftTopPointPair.getValue();

            // 右下角坐标
            Pair<Integer, Integer> rightBottomPointPair = swipePointPairs.getValue();
            Integer x2 = rightBottomPointPair.getKey();
            Integer y2 = rightBottomPointPair.getValue();

            // 调用 adb 命令执行滑动操作
            adbExecutor.runCommand(new String[]{"shell", "input", "swipe", String.valueOf(x1), String.valueOf(y1), String.valueOf(x2), String.valueOf(y2), "250"});
            // 睡眠毫秒，防止滑动过快
            ThreadUtil.sleep(RandomUtil.randomInt(300, 500));
            System.out.println(StrUtil.format("第{}次滑动完成，还剩{}次", i + 1, rectangleInfoSteps.size() - i - 1));
        }
        System.out.println("滑动完成");
    }

    /**
     * 计算矩形顶点坐标
     *
     * @param rectangleInfoSerialSteps       移动步骤
     * @return 对角坐标
     */
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> calcTopPoint(List<RectangleInfo> rectangleInfoSerialSteps) {
        // 获取两个对角坐标
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (RectangleInfo rectangleInfoSerialStep : rectangleInfoSerialSteps) {
            int x = rectangleInfoSerialStep.getX();
            int y = rectangleInfoSerialStep.getY();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        // 为了保证在框内，向右移动
        minX += 30;
        minY += 30;
        maxX += 30;
        // 要往下点才能命中
        maxY += 80;

        return new Pair<>(new Pair<>(minX, minY), new Pair<>(maxX, maxY));
    }
}

package com.hncboy.executor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.hncboy.domain.RectangleInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hncboy
 * @date 2024/2/15
 * 核心执行器
 */
public class CoreExecutor {

    private final List<List<RectangleInfo>> locations;

    public CoreExecutor(List<List<RectangleInfo>> locations) {
        this.locations = locations;
    }

    /**
     * 执行核心算法
     *
     * @return 消除的点位列表
     */
    public List<List<RectangleInfo>> execute() {
        // 封装矩阵
        int[][] matrix = new int[16][10];
        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.get(i).size(); j++) {
                RectangleInfo rectangleInfo = locations.get(i).get(j);
                matrix[i][j] = rectangleInfo.getNumber();
            }
        }

        // 随机寻找符合条件的矩形点位
        List<List<Pair<Integer, Integer>>> totalPoints = randomFindRectangles(matrix);
        // 封装点位
        List<List<RectangleInfo>> result = new ArrayList<>();
        for (List<Pair<Integer, Integer>> serialPoints : totalPoints) {
            List<RectangleInfo> subResult = new ArrayList<>();
            for (Pair<Integer, Integer> serialPoint : serialPoints) {
                RectangleInfo rectangleInfo = locations.get(serialPoint.getKey()).get(serialPoint.getValue());
                subResult.add(rectangleInfo);
            }
            result.add(subResult);
        }

        return result;
    }

    /**
     * 随机寻找符合条件的矩形点位
     *
     * @param matrix 矩阵
     * @return 消除的点位列表
     */
    private List<List<Pair<Integer, Integer>>> randomFindRectangles(int[][] matrix) {
        // 记录每次的分数出现的次数
        Map<Integer, Integer> scoreCountMap = new HashMap<>();
        int maxScore = Integer.MIN_VALUE;
        List<List<Pair<Integer, Integer>>> maxScoreTotalPoints = new ArrayList<>();

        // 正确的下标顺序
        List<Pair<Integer, Integer>> rightIndexPairs = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 10; j++) {
                rightIndexPairs.add(new Pair<>(i, j));
            }
        }

        long currentTimeMillis = System.currentTimeMillis();

        // 执行次数
        int executeCount = 100000;
        for (int count = 0; count < executeCount; count++) {
            if (System.currentTimeMillis() - currentTimeMillis > 50000) {
                System.out.println("超过 50 秒直接结束，累计搜索次数：" + count);
                break;
            }

            // 拷贝一份矩阵
            int[][] copyMatrix = Arrays.stream(matrix).sequential().map(int[]::clone).toArray(int[][]::new);

            List<List<Pair<Integer, Integer>>> totalPoints = new ArrayList<>();
            List<List<Pair<Integer, Integer>>> currentSerialPoints;
            do {
                // 拷贝一份打乱顺序
                List<Pair<Integer, Integer>> indexPairs = new ArrayList<>(rightIndexPairs);
                Collections.shuffle(indexPairs);
                // 根据这份随机点位寻找矩形
                currentSerialPoints = findRectangles(copyMatrix, indexPairs);
                if (CollectionUtil.isNotEmpty(currentSerialPoints)) {
                    totalPoints.addAll(currentSerialPoints);
                }
                // 每次发生变化后，都再一次从头寻找
            } while (!currentSerialPoints.isEmpty());

            int totalCleanCount = totalPoints.stream().mapToInt(List::size).sum();
            // 记录每次的分数出现的次数
            scoreCountMap.put(totalCleanCount, scoreCountMap.getOrDefault(totalCleanCount, 0) + 1);

            if (totalCleanCount > maxScore) {
                maxScore = totalCleanCount;
                maxScoreTotalPoints = totalPoints;
                System.out.println(StrUtil.format("第{}轮消除数字个数打破记录，为{}", count, totalCleanCount));
            }
        }
        System.out.println("最高分数：" + maxScore);
        // 最低分数
        System.out.println("最低分数：" + scoreCountMap.keySet().stream().min(Integer::compareTo).orElse(0));
        // 计算平均分
        int allTotalScore = scoreCountMap.entrySet().stream().mapToInt(entry -> entry.getKey() * entry.getValue()).sum();
        System.out.println("平均分数：" + (double) allTotalScore / executeCount);

        return maxScoreTotalPoints;
    }

    /**
     * 查找符合条件的矩形点位列表
     *
     * @param matrix          矩阵
     * @param indexPairs     访问的点位列表
     * @return 消除的点位列表
     */
    private List<List<Pair<Integer, Integer>>> findRectangles(int[][] matrix, List<Pair<Integer, Integer>> indexPairs) {
        // 每一次消除的点位列表
        List<List<Pair<Integer, Integer>>> totalPoints = new ArrayList<>();

        for (Pair<Integer, Integer> indexPair : indexPairs) {
            int i = indexPair.getKey();
            int j = indexPair.getValue();

            // 如果当前位置的值为 0，直接跳过不作为初始点位
            if (matrix[i][j] == 0) {
                continue;
            }

            Map<Pair<Integer, Integer>, Integer> pointValueMap = new HashMap<>();
            // 递归查找数字和为 10 的点位
            if (!dfs(matrix, i, j, pointValueMap)) {
                continue;
            }

            // 再次判断矩形内所有点位的数字和是否为 10
            if (calcRectanglePointValue(matrix, pointValueMap.keySet()) != 10) {
                continue;
            }

            // 过滤掉值为 0 的点位
            List<Pair<Integer, Integer>> currentSerialPoints = pointValueMap.keySet()
                    .stream()
                    .filter(pointPair -> matrix[pointPair.getKey()][pointPair.getValue()] != 0)
                    .toList();

            // 添加本次消除的动作
            totalPoints.add(currentSerialPoints);

            // 清除矩形，将矩形内的值置为 0
            for (Pair<Integer, Integer> pointPair : pointValueMap.keySet()) {
                matrix[pointPair.getKey()][pointPair.getValue()] = 0;
            }
        }

        return totalPoints;
    }

    /**
     * 递归查找数字和为 10 的点位
     *
     * @param matrix         矩阵
     * @param i              当前点位的横坐标
     * @param j              当前点位的纵坐标
     * @param pointValueMap  点位和值的映射
     * @return 是否找到数字和为 10 的点位
     */
    private boolean dfs(int[][] matrix, int i, int j, Map<Pair<Integer, Integer>, Integer> pointValueMap) {
        if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length) {
            return false;
        }

        Pair<Integer, Integer> pointPair = new Pair<>(i, j);
        pointValueMap.put(pointPair, matrix[i][j]);

        int sum = pointValueMap.values().stream().mapToInt(Integer::intValue).sum();
        if (sum > 10) {
            pointValueMap.remove(pointPair);
            return false;
        }

        if (sum == 10) {
            return true;
        }

        // 只需要往两个方向
        boolean found;
        // 往右
        found = dfs(matrix, i, j + 1, pointValueMap);
        // 往下
        found = dfs(matrix, i + 1, j, pointValueMap) || found;
        if (!found) {
            pointValueMap.remove(pointPair);
        }
        return found;
    }

    /**
     * 计算矩形内的数字和
     *
     * @param matrix       矩阵
     * @param pointPairSet 矩形内的点位
     * @return 矩形内的数字和
     */
    private int calcRectanglePointValue(int[][] matrix, Set<Pair<Integer, Integer>> pointPairSet) {
        // 获取两个对角坐标
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Pair<Integer, Integer> pointPair : pointPairSet) {
            int x = pointPair.getKey();
            int y = pointPair.getValue();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        // 计算矩形内的数字和，因为可能这个矩形还有其他的数字，所以需要重新计算
        int sum = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                sum += matrix[x][y];
            }
        }
        return sum;
    }
}

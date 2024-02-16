package com.hncboy.test;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

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
 * 核心算法测试
 */
public class CoreTest {

    public static void main(String[] args) {
        int[][] matrix = {
                {3, 2, 3, 3, 5, 2, 8, 1, 2, 3},
                {3, 3, 8, 4, 2, 3, 8, 4, 9, 9},
                {8, 8, 2, 2, 3, 4, 5, 1, 4, 2},
                {6, 5, 6, 8, 1, 9, 4, 7, 6, 7},
                {1, 1, 5, 6, 2, 9, 1, 3, 3, 8},
                {3, 2, 1, 6, 6, 5, 9, 2, 8, 5},
                {3, 7, 2, 8, 4, 7, 7, 7, 2, 1},
                {4, 2, 6, 1, 1, 2, 3, 4, 8, 5},
                {5, 9, 5, 2, 9, 9, 3, 8, 3, 3},
                {5, 8, 2, 2, 7, 5, 7, 6, 4, 4},
                {4, 4, 1, 3, 5, 4, 5, 3, 6, 5},
                {1, 6, 9, 8, 9, 1, 8, 9, 6, 1},
                {6, 5, 7, 4, 4, 5, 8, 1, 2, 3},
                {2, 2, 6, 2, 8, 1, 2, 7, 8, 9},
                {4, 1, 5, 4, 5, 9, 8, 7, 5, 7},
                {1, 6, 8, 2, 5, 7, 7, 6, 6, 9}
        };

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

        // 执行次数
        int executeCount = 10000;
        for (int count = 0; count < executeCount; count++) {
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
    }

    public static List<List<Pair<Integer, Integer>>> findRectangles(int[][] matrix, List<Pair<Integer, Integer>> indexPairs) {
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

    public static boolean dfs(int[][] matrix, int i, int j, Map<Pair<Integer, Integer>, Integer> pointValueMap) {
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
    public static int calcRectanglePointValue(int[][] matrix, Set<Pair<Integer, Integer>> pointPairSet) {
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

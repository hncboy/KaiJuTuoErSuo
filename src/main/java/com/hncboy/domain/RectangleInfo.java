package com.hncboy.domain;

import lombok.Data;

/**
 * @author hncboy
 * @date 2024/2/14
 * 矩形信息
 */
@Data
public class RectangleInfo {

    /**
     * 左上角 x 坐标
     */
    private int x;

    /**
     * 左上角 y 坐标
     */
    private int y;

    /**
     * 高度
     */
    private int height;

    /**
     * 宽度
     */
    private int width;

    /**
     * 数字
     */
    private int number;

    /**
     * 索引下标 i
     */
    private int indexI;

    /**
     * 索引下标 j
     */
    private int indexJ;

    /**
     * 是否使用 tesseract
     */
    private boolean isUseTesseract;
}

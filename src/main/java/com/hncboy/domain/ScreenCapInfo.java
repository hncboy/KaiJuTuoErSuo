package com.hncboy.domain;

import lombok.Data;

/**
 * @author hncboy
 * @date 2024/2/14
 * 截屏信息
 */
@Data
public class ScreenCapInfo {

    /**
     * 截屏文件手机路径
     */
    private String screenCapMobilePath;

    /**
     * 截屏文件电脑路径
     */
    private String screenCapComputerPath;

    /**
     * 截屏文件手机名称
     */
    private String screenCapMobileName;

    /**
     * 截屏文件手机目录
     */
    private String screenCapMobileDir;

    /**
     * 截屏文件电脑名称
     */
    private String screenCapComputerName;

    /**
     * 截屏文件电脑目录
     */
    private String screenCapComputerDir;
}

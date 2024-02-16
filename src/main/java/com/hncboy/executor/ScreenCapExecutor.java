package com.hncboy.executor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.hncboy.domain.ScreenCapInfo;

import java.io.IOException;
import java.util.Objects;

/**
 * @author hncboy
 * @date 2024/2/14
 * 截屏执行器
 */
public class ScreenCapExecutor {

    private final AdbExecutor adbExecutor;

    /**
     * 截屏文件手机目录
     */
    private final String screenCapMobileDir;

    /**
     * 截屏文件电脑目录
     */
    private final String screenCapComputerDir;

    public ScreenCapExecutor(AdbExecutor adbExecutor) {
        this.adbExecutor = adbExecutor;
        this.screenCapMobileDir = "/storage/sdcard0/KaiJuYouErSuo";
        this.screenCapComputerDir = "E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\screenshop";
    }

    /**
     * 执行截屏
     */
    public ScreenCapInfo execute() {
        // 运行 adb 命令，用来验证是否安装成功
        adbExecutor.runCommand(null);
        // 截屏信息
        ScreenCapInfo screenCapInfo = mobileScreenCap();

        if (Objects.isNull(screenCapInfo)) {
            System.out.println("手机截屏失败");
        } else {
            System.out.println("手机截屏成功");

            // 将截屏文件保存到电脑
            boolean result = pullScreenCap(screenCapInfo);
            if (result) {
                screenCapInfo.setScreenCapComputerDir(screenCapComputerDir);
                // 手机和电脑名称一样
                screenCapInfo.setScreenCapComputerName(screenCapInfo.getScreenCapMobileName());
                screenCapInfo.setScreenCapComputerPath(screenCapComputerDir + "\\" + screenCapInfo.getScreenCapMobileName());
                System.out.println("手机截屏保存到电脑成功");
            } else {
                System.out.println("手机截屏保存到电脑失败");
            }
        }

        return screenCapInfo;
    }

    /**
     * 手机截屏并保存到手机文件夹
     *
     * @return 截屏信息
     */
    public ScreenCapInfo mobileScreenCap() {
        // 截屏文件名称
        String screenCapName = StrUtil.format("Screenshot_{}.png", DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss"));
        try {
            // 截屏文件路径
            String screenCapFilePath = screenCapMobileDir + "/" + screenCapName;

            // 创建截屏目录
            createDirectory(screenCapMobileDir);

            // 截屏并保存到自定义目录
            adbExecutor.runCommand(new String[]{"shell", "screencap", "-p", screenCapFilePath});

            // 将截屏文件路径和名称存储到结果数组中
            ScreenCapInfo screenCapInfo = new ScreenCapInfo();
            screenCapInfo.setScreenCapMobilePath(screenCapFilePath);
            screenCapInfo.setScreenCapMobileName(screenCapName);
            screenCapInfo.setScreenCapMobileDir(screenCapMobileDir);
            ;
            return screenCapInfo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建目录
     *
     * @param directory 目录
     * @throws IOException 创建目录失败
     */
    private void createDirectory(String directory) throws IOException {
        // 检查目录是否已存在
        String checkDirCommand = "if [ ! -d " + directory + " ]; then echo 'not exists'; fi";
        String checkDirResult = adbExecutor.runCommand(new String[]{"shell", checkDirCommand});
        if (Objects.requireNonNull(checkDirResult).trim().equals("not exists")) {
            // 如果目录不存在，则创建目录
            adbExecutor.runCommand(new String[]{"shell", "mkdir", "-p", directory});
        }
    }

    /**
     * 将截屏文件保存到电脑上
     *
     * @param screenCapInfo 截屏信息
     * @return true 表示成功，否则失败
     */
    public boolean pullScreenCap(ScreenCapInfo screenCapInfo) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{adbExecutor.getAdbPath(), "pull", screenCapInfo.getScreenCapMobilePath(), screenCapComputerDir});
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}

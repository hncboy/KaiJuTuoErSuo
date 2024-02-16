package com.hncboy.executor;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author hncboy
 * @date 2024/2/14
 * adb 执行器
 */
public class AdbExecutor {

    /**
     * adb 工具路径
     */
    @Getter
    private final String adbPath;

    public AdbExecutor(String adbPath) {
        this.adbPath = adbPath;
    }

    /**
     * 运行 adb 命令
     *
     * @param command 实际相关命令
     * @return 执行命令结果
     */
    public String runCommand(String[] command) {
        StringBuilder output = new StringBuilder();
        try {
            // 构造命令
            String[] fullCommand;
            if (Objects.isNull(command)) {
                // 如果命令为空，只包含 adbPath
                fullCommand = new String[]{adbPath};
            } else {
                // 如果命令不为空，将 adbPath 和 command 组合在一起
                fullCommand = new String[command.length + 1];
                fullCommand[0] = adbPath;
                System.arraycopy(command, 0, fullCommand, 1, command.length);
            }

            // 执行命令
            Process process = Runtime.getRuntime().exec(fullCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            // 读取命令输出
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            // 等待命令执行完成
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}

# 开局托儿所

## 作用
微信小程序开局托儿所自动消除辅助，适用于安卓手机

## 实现步骤
- 1.安装 ADB 工具，手机开启开发者调试模式连接电脑
- 2.进入手机游戏界面，运行程序
- 3.ADB 调用截图命令，将图片保存到手机目录，再复制到电脑
- 4.OpenCV 读取图片，灰度图片，二值化图片，识别轮廓，可以得到 160 个方块的坐标
- 5.根据 tess4j 和 deeplearning4j 一起识别数字，得到 160 个方块的数字，误差在 5 个数字以内，误差会影响消除路径的选择，不过影响分数比较小
- 6.根据随机坐标 dfs回溯算法找到分数最高的消除路径
- 7.根据路径，调用 ADB 命令，模拟滑动，实现自动消除
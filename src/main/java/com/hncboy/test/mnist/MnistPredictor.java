package com.hncboy.test.mnist;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;


/**
 * @author hncboy
 * @date 2024/2/14
 * 预测器
 */
public class MnistPredictor {

    private MultiLayerNetwork model;

    public MnistPredictor() {
        // 加载预训练好的LeNet模型
        File locationToSave = new File("E:\\Project\\IdeaProject\\KaiJuTuoErSuo\\KaiJuTuoErSuo\\src\\main\\resources\\model\\model.zip");
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int predict(int[][] inputImage) {
        // 将输入图像转换为 INDArray 格式，并进行归一化处理
        INDArray input = Nd4j.create(inputImage).reshape(1, 1, 28, 28).divi(255.0);
        // 对图像进行推理，并返回预测结果的索引
        INDArray output = model.output(input);
        return Nd4j.argMax(output, 1).getInt(0);
    }
}

// utils/AIUtils.java
package com.example.memorai.utils;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AIUtils {
    private Interpreter tflite;

    public AIUtils(Context context, String modelPath) {
        try {
            MappedByteBuffer modelBuffer = loadModelFile(context, modelPath);
            tflite = new Interpreter(modelBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        FileInputStream inputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Example inference method
    public float[] runInference(float[] inputData) {
        float[][] output = new float[1][10]; // Adjust output shape as needed.
        tflite.run(inputData, output);
        return output[0];
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}

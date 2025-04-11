package com.example.memorai.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class ImageClassifierHelper {
  private static final String TAG = "ImageClassifierHelper";
  private static final int MAX_RESULTS = 5;
  private static final int IMAGE_SIZE = 224;
  private static final int NUM_CLASSES = 1001;
  private static final int BATCH_SIZE = 1;
  private static final int PIXEL_SIZE = 3;

  private Interpreter interpreter;
  private final Context context;
  private final String modelPath = "mobilenet_v2_1.0_224.tflite";
  private final String labelPath = "labels.txt";
  private List<String> labels;
  private ByteBuffer inputBuffer;
  private GpuDelegate gpuDelegate;
  private NnApiDelegate nnApiDelegate;

  public ImageClassifierHelper(Context context) {
    this.context = context;
    try {
      setupInterpreter();
      loadLabels();
      createInputBuffer();
    } catch (IOException e) {
      Log.e(TAG, "Error initializing classifier", e);
    }
  }

  private void setupInterpreter() throws IOException {
    // 1. Load model từ file
    MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelPath);

    // 2. Tạo options mặc định (không có tối ưu hóa)
    Interpreter.Options options = new Interpreter.Options();

    // 3. Khởi tạo interpreterd
    interpreter = new Interpreter(modelBuffer, options);
  }

  private void loadLabels() throws IOException {
    labels = FileUtil.loadLabels(context, labelPath);
    if (labels == null || labels.isEmpty()) {
      throw new IOException("Failed to load labels or labels file is empty");
    }
  }

  private void createInputBuffer() {
    inputBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE * Float.BYTES);
    inputBuffer.order(ByteOrder.nativeOrder());
  }

  public List<String> classify(Bitmap bitmap) {
    if (interpreter == null) {
      Log.e(TAG, "Interpreter not initialized");
      return new ArrayList<>();
    }
    if (labels == null || labels.isEmpty()) {
      Log.e(TAG, "Labels not loaded");
      return new ArrayList<>();
    }

    try {
      Bitmap resizedBitmap = Bitmap.createScaledBitmap(
              bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
      preprocessImage(resizedBitmap);

      float[][] output = new float[1][NUM_CLASSES];
      long startTime = SystemClock.uptimeMillis();
      interpreter.run(inputBuffer, output);
      Log.d(TAG, "Inference time: " + (SystemClock.uptimeMillis() - startTime) + "ms");

      return getTopResults(output[0]);
    } catch (Exception e) {
      Log.e(TAG, "Classification error", e);
      return new ArrayList<>();
    }
  }

  private void preprocessImage(Bitmap bitmap) {
    inputBuffer.rewind();
    int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
    bitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

    // Normalize to [0,1] and put into buffer
    for (int pixel : pixels) {
      inputBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f); // R
      inputBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);  // G
      inputBuffer.putFloat((pixel & 0xFF) / 255.0f);         // B
    }
  }

  private List<String> getTopResults(float[] confidences) {
    PriorityQueue<Recognition> topResults = new PriorityQueue<>(
            MAX_RESULTS,
            (a, b) -> Float.compare(b.confidence, a.confidence)
    );

    for (int i = 0; i < confidences.length; i++) {
      if (i < labels.size() && confidences[i] > 0.02f) {
        topResults.offer(new Recognition(labels.get(i), confidences[i]));

        if (topResults.size() > MAX_RESULTS) {
          topResults.poll();
        }
      }
    }

    List<String> results = new ArrayList<>(MAX_RESULTS);
    while (!topResults.isEmpty()) {
      results.add(topResults.poll().label);
    }

    Collections.reverse(results);
    return results;
  }

  public void close() {
    if (interpreter != null) {
      interpreter.close();
      interpreter = null;
    }
    if (gpuDelegate != null) {
      gpuDelegate.close();
      gpuDelegate = null;
    }
    if (nnApiDelegate != null) {
      nnApiDelegate.close();
      nnApiDelegate = null;
    }
  }

  private static class Recognition {
    final String label;
    final float confidence;

    Recognition(String label, float confidence) {
      this.label = label;
      this.confidence = confidence;
    }
  }
}
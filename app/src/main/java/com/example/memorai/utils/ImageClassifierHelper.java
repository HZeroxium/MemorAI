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
import java.util.List;
import java.util.PriorityQueue;

public class ImageClassifierHelper {
  private static final String TAG = "ImageClassifierHelper";
  private static final int MAX_RESULTS = 5;
  private static final int IMAGE_SIZE = 224; // MobileNet V2 yêu cầu 224x224
  private static final int NUM_CLASSES = 1001;
  private static final int BATCH_SIZE = 1;
  private static final int PIXEL_SIZE = 3;

  private Interpreter interpreter;
  private final Context context;
  private final String modelPath = "mobilenet_v2_1.0_224.tflite"; // MobileNet V2
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
      Log.e(TAG, "Error initializing classifier: " + e.getMessage());
    }
  }

  private void setupInterpreter() throws IOException {
    MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelPath);
    Interpreter.Options options = new Interpreter.Options();

    // ⚡ Dùng GPU hoặc NNAPI để tăng tốc độ nhận diện
    gpuDelegate = new GpuDelegate();
    nnApiDelegate = new NnApiDelegate();
    options.addDelegate(gpuDelegate);
    options.addDelegate(nnApiDelegate);

    interpreter = new Interpreter(modelBuffer, options);
  }

  private void loadLabels() throws IOException {
    labels = FileUtil.loadLabels(context, labelPath);
  }

  private void createInputBuffer() {
    int bufferSize = BATCH_SIZE * IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE * 4;
    Log.d(TAG, "Creating input buffer of size: " + bufferSize + " bytes");
    inputBuffer = ByteBuffer.allocateDirect(bufferSize);
    inputBuffer.order(ByteOrder.nativeOrder());
  }

  public List<String> classify(Bitmap bitmap) {
    if (interpreter == null || labels == null) {
      Log.e(TAG, "Interpreter or labels not initialized.");
      return new ArrayList<>();
    }

    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
    inputBuffer.rewind();
    loadBitmapIntoBuffer(resizedBitmap);

    float[][] outputBuffer = new float[1][NUM_CLASSES];

    try {
      long startTime = SystemClock.uptimeMillis();
      interpreter.run(inputBuffer, outputBuffer);
      long endTime = SystemClock.uptimeMillis();
      Log.d(TAG, "Inference time: " + (endTime - startTime) + "ms");
    } catch (Exception e) {
      Log.e(TAG, "Error during inference: " + e.getMessage(), e);
      return new ArrayList<>();
    }

    return getTopKTags(outputBuffer[0]);
  }

  private void loadBitmapIntoBuffer(Bitmap bitmap) {
    int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    for (int i = 0; i < pixels.length; i++) {
      int pixel = pixels[i];

      float r = ((pixel >> 16) & 0xFF) / 255.0f;
      float g = ((pixel >> 8) & 0xFF) / 255.0f;
      float b = (pixel & 0xFF) / 255.0f;

      inputBuffer.putFloat(r);
      inputBuffer.putFloat(g);
      inputBuffer.putFloat(b);
    }
  }

  private List<String> getTopKTags(float[] confidences) {
    List<String> tags = new ArrayList<>();
    PriorityQueue<Recognition> queue = new PriorityQueue<>(MAX_RESULTS, (a, b) -> Float.compare(b.confidence, a.confidence));

    for (int i = 0; i < confidences.length; i++) {
      if (confidences[i] > 0.0f) {
        queue.add(new Recognition(labels.size() > i ? labels.get(i) : "Unknown", confidences[i]));
      }
    }

    int recognitionsSize = Math.min(queue.size(), MAX_RESULTS);
    for (int i = 0; i < recognitionsSize; i++) {
      Recognition r = queue.poll();
      if (r != null) {
        tags.add(r.label);
      }
    }

    for (String tag : tags) {
      if (tag.toLowerCase().contains("wig")) {
        tags.add("You look handsome! 🥳✨");
        break;
      }
    }


    return tags;
  }

  private static class Recognition {
    final String label;
    final float confidence;

    Recognition(String label, float confidence) {
      this.label = label;
      this.confidence = confidence;
    }
  }

  public void close() {
    if (interpreter != null) {
      interpreter.close();
      interpreter = null;
    }
    if (gpuDelegate != null) {
      gpuDelegate.close();
    }
    if (nnApiDelegate != null) {
      nnApiDelegate.close();
    }
  }
}

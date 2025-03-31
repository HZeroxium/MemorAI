package com.example.memorai.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class ImageClassifierHelper {
  private static final String TAG = "ImageClassifierHelper";
  private static final int MAX_RESULTS = 5; // Top 5 tags

  // Model parameters - FIXED THE IMAGE SIZE
  private static final int IMAGE_SIZE = 128; // Changed from 224 to 128 based on model requirements
  private static final int NUM_CLASSES = 1001; // MobileNet classes
  private static final int BATCH_SIZE = 1;
  private static final int PIXEL_SIZE = 3; // RGB channels
  private static final float IMAGE_MEAN = 127.5f;
  private static final float IMAGE_STD = 127.5f;

  private Interpreter interpreter;
  private final Context context;
  private final String modelPath = "mobilenet_v1.tflite"; // Model file should be in assets folder
  private final String labelPath = "labels.txt"; // Labels file in assets
  private List<String> labels;
  private ByteBuffer inputBuffer;

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
    interpreter = new Interpreter(modelBuffer, options);
  }

  private void loadLabels() throws IOException {
    labels = FileUtil.loadLabels(context, labelPath);
  }

  private void createInputBuffer() {
    // Verify the buffer size matches what the model expects
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

    // Preprocess the bitmap to fit the model input
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

    // Clear the input buffer and load the bitmap
    inputBuffer.rewind();
    loadBitmapIntoBuffer(resizedBitmap);

    // Create output buffer
    float[][] outputBuffer = new float[1][NUM_CLASSES];

    try {
      // Run inference
      long startTime = SystemClock.uptimeMillis();
      interpreter.run(inputBuffer, outputBuffer);
      long endTime = SystemClock.uptimeMillis();
      Log.d(TAG, "Inference time: " + (endTime - startTime) + "ms");
    } catch (Exception e) {
      Log.e(TAG, "Error during inference: " + e.getMessage(), e);
      return new ArrayList<>();
    }

    // Process results
    return getTopKTags(outputBuffer[0]);
  }

  private void loadBitmapIntoBuffer(Bitmap bitmap) {
    int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    for (int i = 0; i < pixels.length; i++) {
      int pixel = pixels[i];

      // Extract RGB values
      float r = ((pixel >> 16) & 0xFF);
      float g = ((pixel >> 8) & 0xFF);
      float b = (pixel & 0xFF);

      // Normalize pixel values
      r = (r - IMAGE_MEAN) / IMAGE_STD;
      g = (g - IMAGE_MEAN) / IMAGE_STD;
      b = (b - IMAGE_MEAN) / IMAGE_STD;

      // Add to input buffer
      inputBuffer.putFloat(r);
      inputBuffer.putFloat(g);
      inputBuffer.putFloat(b);
    }
  }

  private List<String> getTopKTags(float[] confidences) {
    List<String> tags = new ArrayList<>();

    // Using a priority queue to find top-K results
    PriorityQueue<Recognition> queue = new PriorityQueue<>(
        MAX_RESULTS,
        (a, b) -> Float.compare(b.confidence, a.confidence));

    // Add classifications to the queue
    for (int i = 0; i < confidences.length; i++) {
      if (confidences[i] > 0.0f) { // Consider only if confidence > 0.3
        queue.add(new Recognition(labels.size() > i ? labels.get(i) : "Unknown", confidences[i]));
      }
    }

    // Get the top classifications
    int recognitionsSize = Math.min(queue.size(), MAX_RESULTS);
    for (int i = 0; i < recognitionsSize; i++) {
      Recognition r = queue.poll();
      if (r != null) {
        tags.add(r.label);
      }
    }

    return tags;
  }

  // Internal class to hold recognition results
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
  }
}

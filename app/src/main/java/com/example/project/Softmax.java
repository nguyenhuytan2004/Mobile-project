package com.example.project;

import android.util.Log;

import java.util.Arrays;

public class Softmax {
    private int inputSize;
    private int numClasses;
    private double[][] weights;
    private static final double learningRate = 0.03;

    private static final String[] keywordImportant = {"thi", "thăm", "khám", "trường", "công ty"};
    private static final String[] keywordEmergency = {"nhanh", "khẩn", "gấp"};

    public Softmax(int inputSize_, int numClasses_) {
        inputSize = inputSize_;
        numClasses = numClasses_;
        weights = new double[numClasses][inputSize];
    }

    public void setWeight(int classId, int featureIndex, double weight) {
        if (classId >= 0 && classId < weights.length && featureIndex >= 0 && featureIndex < weights[0].length) {
            weights[classId][featureIndex] = weight;
        }
    }

    public double[] predictProba(double[] x) {
        double[] logits = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            for (int j = 0; j < inputSize; j++) {
                logits[i] += weights[i][j] * x[j];
            }
        }
        return softmax(logits);
    }

    public int predict(double[] x) {
        double[] probs = predictProba(x);
        int maxIdx = 0;
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > probs[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    public void trainOnline(double[] x, int yTrue) {
        double[] probs = predictProba(x);
        for (int i = 0; i < numClasses; i++) {
            double error = probs[i] - (i == yTrue ? 1 : 0);
            for (int j = 0; j < inputSize; j++) {
                weights[i][j] -= learningRate * error * x[j];
            }
        }
    }

    private double[] softmax(double[] z) {
        double max = Arrays.stream(z).max().orElse(0);
        double sum = 0.0;
        double[] exp = new double[z.length];

        for (int i = 0; i < z.length; i++) {
            exp[i] = Math.exp(z[i] - max);
            sum += exp[i];
        }

        for (int i = 0; i < exp.length; i++) {
            exp[i] /= sum;
        }

        return exp;
    }

    public void printWeights() {
        Log.d("SoftmaxWeights", "\n=== Weights ===");
        for (int i = 0; i < numClasses; i++) {
            Log.d("SoftmaxWeights", "Class " + i + ": " + Arrays.toString(weights[i]));
        }
    }

    // --- Static Hashtag Checkers ---
    public static boolean checkHashtagImportance(String content) {
        content = content.toLowerCase();
        return content.contains("#important") || content.contains("#importance");
    }

    public static boolean checkHashtagTravel(String content) {
        return content.toLowerCase().contains("#travel");
    }

    public static boolean checkHashtagJob(String content) {
        return content.toLowerCase().contains("#job");
    }

    public static boolean checkHashtagEmail(String content) {
        return content.toLowerCase().contains("#email");
    }

    // --- Static Keyword Checkers ---
    public static boolean checkKeywordImportance(String text) {
        text = text.toLowerCase();
        for (String keyword : keywordImportant) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    public static boolean checkKeywordEmergency(String text) {
        text = text.toLowerCase();
        for (String keyword : keywordEmergency) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}

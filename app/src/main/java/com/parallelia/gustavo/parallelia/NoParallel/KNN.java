package com.parallelia.gustavo.parallelia.NoParallel;

import android.graphics.Bitmap;

import com.parallelia.gustavo.parallelia.controller.exception.KNNException;
import com.parallelia.gustavo.parallelia.model.KNN_Vector;

import java.util.ArrayList;

/**
 * Created by gustavo on 15/01/15.
 */
public class KNN {
    //atributtes
    private int max_k;
    private ArrayList<KNN_Vector> samples;
    private int var_count;

    public KNN() {
        this.var_count = 0;
        this.max_k = 32;
        this.samples = new ArrayList<KNN_Vector>();
    }

    public KNN(int max_k) {
        this.max_k = max_k;
        this.var_count = 0;
        this.samples = new ArrayList<KNN_Vector>();
    }

    /**
     * Method to return the total number of train samples
     *
     * @return
     */
    public int TotalSamples() {
        return samples.size();
    }

    /**
     * Method to return the total features used
     *
     * @return
     */
    public int TotalFeatures() {
        return var_count;
    }

    /**
     * Method to train the KNN classifer
     *
     * @param images
     * @param labels
     * @return
     */
    public Boolean train(Bitmap[] images, float[] labels) {
        if (images.length != labels.length) {
            return false;
        }

        for (int i = 0; i < labels.length; i++) {
            KNN_Vector kv = new KNN_Vector(images[i], labels[i]);
            samples.add(kv);
        }

        var_count = samples.get(0).getEigenvector().length;

        return true;
    }

    public float find_nearest(int k, ArrayList<KNN_Vector> test_data,String[] results) throws KNNException {
        if (samples.size() <= 0) {
            throw new KNNException("The KNN classifer is not ready for find neighbord!");
        }

        if (k < 1 || k > max_k) {
            throw new KNNException("k must be within 1 and max_k range.");
        }

        int k1 = 0, k2 = 0;
        results = new String[test_data.size()];

        for (int s = 0; s < 4; s++) {
            KNN_Vector test = test_data.get(s);
            int dd[] = new int[k];
            float nr[] = new float[results.length+k*this.var_count];
            for (int i = 0; i < samples.size(); i++) {
                KNN_Vector sample = samples.get(i);
                for (int j = 0; j < this.var_count; j++) {
                    int sum = 0, t, ii, ii1;
                    int[] pixels_train = sample.getEigenvector();

                    for (t = 0; t <= this.var_count - 4; t += 4) {
                        double t0 = test.getEigenvector()[t] - pixels_train[t], t1 = test.getEigenvector()[t + 1] - pixels_train[t + 1];
                        double t2 = test.getEigenvector()[t + 2] - pixels_train[t + 2], t3 = test.getEigenvector()[t + 3] - pixels_train[t + 3];
                        sum += t0 * t0 + t1 * t1 + t2 * t2 + t3 * t3;
                    }

                    for (; t < this.var_count; t++) {
                        double t0 = test.getEigenvector()[t] - pixels_train[t];
                        sum += t0 * t0;
                    }

                    for (ii = k1 - 1; ii >= 0; ii--) {
                        if (sum > dd[ii])
                            break;
                    }
                    if (ii < k - 1) {
                        for (ii1 = k2 - 1; ii1 > ii; ii1--) {
                            dd[(ii1 + 1)] = dd[ii1];
                            nr[(ii1 + 1)] = nr[ii1];
                        }

                        dd[(ii + 1)] = sum;
                        nr[(ii + 1)] = sample.getLabel();//pixels_train[pixels_train.length - 1];
                    }
                    k1 = (k1 + 1) < k ? (k1 + 1) : k;
                    k2 = k1 < (k - 1) ? k1 : (k - 1);
                }
            }

            int prev_start = 0, best_count = 0, cur_count;
            float best_val = 0;

            for (int j = k1 - 1; j > 0; j--) {
                boolean swap_f1 = false;
                for (int j1 = 0; j1 < j; j1++) {
                    if (nr[j1] > nr[(j1 + 1)]) {
                        float t;
                        t = nr[j1];
                        nr[j1] = nr[(j1 + 1)];
                        nr[(j1 + 1)] = t;
                        swap_f1 = true;
                    }
                }
                if (!swap_f1)
                    break;
            }

            for (int j = 1; j <= k1; j++) {
                if (j == k1 || nr[j] != nr[(j - 1)]) {
                    cur_count = j - prev_start;
                    if (best_count < cur_count) {
                        best_count = cur_count;
                        best_val = nr[(j - 1)];
                    }
                    prev_start = j;
                }
            }
            System.out.println(best_val);
            results[s] = String.valueOf(best_val);
        }
        String text = "RS:" + results[0] + " - " + results[3];
        System.out.println(text);
        return 0;
    }
}

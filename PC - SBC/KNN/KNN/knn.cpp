#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
#include <algorithm>  
#include <vector>
#include "knn.hpp"
#include "KNNVector.hpp"

KNN::KNN() {
	var_count = 0;
    max_k = 32;
    samples.clear();
}

KNN::KNN(int max_k) {
	var_count = 0;
    max_k = max_k;
    samples.clear();
}

int KNN::totalSamples() {
    return samples.size();
}

int KNN::totalFeatures() {
    return var_count;
}

bool KNN::train(Mat trainSet, Mat trainLabels) {
    if (trainSet.rows != trainLabels.rows) {
        return false;
    }

    samples.reserve(trainLabels.rows);

    for (int i = 0; i < trainLabels.rows; i++) {
    	Mat letter = trainSet.rowRange(i,i+1);
        float label = trainLabels.at<float>(i,0);
        KNNVector kv = KNNVector(letter, label);
        samples.push_back(kv);
    }

    var_count = trainSet.cols;

    return true;
}

vector<float> KNN::find_nearest(int k, Mat testSet) {
	if (samples.size() <= 0) {
        cout<<"The KNN classifer is not ready for find neighbord!"<<endl;
        exit(-1);
    }

    if (k < 1 || k > max_k) {
        cout<<"k must be within 1 and max_k range."<<endl;
        exit(-1);
    }

    vector<float> results;

    float nr[testSet.rows][k];
    float dd[testSet.rows][k];

    int k1 = 0, k2 = 0;
    for (int s = 0; s < totalSamples(); s++) {
        KNNVector vector = samples.at(s);
        Mat pixels_train = vector.getEigenvector();

        for (int i = 0; i < testSet.rows; i++) {
            Mat test = testSet.rowRange(i,i+1);
            int ii, ii1;
            double sum = 0;

            int t;
            for (t = 0; t <= totalFeatures() - 4; t += 4) {
                double t0 = test.at<float>(0, t) - pixels_train.at<float>(0, t), t1 = test.at<float>(0, t + 1) - pixels_train.at<float>(0, t + 1);
                double t2 = test.at<float>(0, t + 2) - pixels_train.at<float>(0, t + 2), t3 = test.at<float>(0, t + 3) - pixels_train.at<float>(0, t + 3);
                sum += t0 * t0 + t1 * t1 + t2 * t2 + t3 * t3;
            }
            
            for (; t < totalFeatures(); t++) {
                double t0 = test.at<float>(0, t) - pixels_train.at<float>(0, t);
                sum += t0 * t0;
            }

            for (ii = k1 - 1; ii >= 0; ii--)
                if ((float) sum > dd[i][ii])
                    break;
            if (ii >= k - 1)
                continue;

            if (ii < k - 1) {
                for (ii1 = k2 - 1; ii1 > ii; ii1--) {
                    dd[i][(ii1 + 1)] = dd[i][ii1];
                    nr[i][(ii1 + 1)] = nr[i][ii1];
                }

                dd[i][(ii + 1)] = sum;
                nr[i][(ii + 1)] = vector.getLabel();//pixels_train[pixels_train.length - 1];
            }
        }

        k1 = (k1 + 1) < k ? (k1 + 1) : k;
        k2 = k1 < (k - 1) ? k1 : (k - 1);
    }

    k1 = min(k, totalSamples());

    for(int i = 0; i < testSet.rows; i++) {
        int prev_start = 0, best_count = 0, cur_count;
        float best_val;

        for (int j = k1 - 1; j > 0; j--) {
            bool swap_f1 = false;
            for (int j1 = 0; j1 < j; j1++) {
                if (nr[i][j1] > nr[i][(j1 + 1)]) {
                    float t;
                    t = nr[i][j1];
                    nr[i][j1] = nr[i][(j1 + 1)];
                    nr[i][(j1 + 1)] = t;
                    swap_f1 = true;
                }
            }
            if (!swap_f1)
                break;
        }

        best_val = 0;
        for (int j = 1; j <= k1; j++) {
            if (j == k1 || nr[i][j] != nr[i][(j - 1)]) {
                cur_count = j - prev_start;
                if (best_count < cur_count) {
                    best_count = cur_count;
                    best_val = nr[i][(j - 1)];
                }
                prev_start = j;
            }
        }

        results.push_back(best_val);
    }

    return results;
}
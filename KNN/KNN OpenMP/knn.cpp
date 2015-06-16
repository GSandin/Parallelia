#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
#include <algorithm>  
#include <vector>
#include <omp.h>
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
    results.reserve(testSet.rows);

    #pragma omp parallel shared(results)
    {
        #pragma omp for schedule(static, 600) 
        for (int t = 0; t < testSet.rows; t++)  {
            Mat test = testSet.rowRange(t,t+1);
            int dd[k], k1 = 0, k2 = 0;
            float nr[k], best_val = 0;
            for (int s = 0; s < totalSamples(); s++) {
                KNNVector vector = samples.at(s);
                Mat pixels_train = vector.getEigenvector();
                //KNN_Vector sample = samples.get(i);
                float sum = 0;
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

                int ii, ii1;
                for (ii = k1 - 1; ii >= 0; ii--)
                    if ((int)sum > dd[ii])
                        break;

                if (ii < k - 1) {
                    for (ii1 = k2 - 1; ii1 > ii; ii1--) {
                        dd[(ii1 + 1)] = dd[ii1];
                        nr[(ii1 + 1)] = nr[ii1];
                    }

                    dd[(ii + 1)] = (int)sum;
                    nr[(ii + 1)] = vector.getLabel();//pixels_train[pixels_train.length - 1];
                }

                k1 = (k1 + 1) < k ? (k1 + 1) : k;
                k2 = k1 < (k - 1) ? k1 : (k - 1);
            }

            int prev_start = 0, best_count = 0, cur_count;

            for (int j = k1 - 1; j > 0; j--) {
                bool swap_f1 = false;
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

            best_val=0;
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
            results[t] = best_val;
        }
    }
    return results;
}
#ifndef KNN_H
#define KNN_H

#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
#include <vector>
#include "KNNVector.hpp"


class KNN {
	public:
		KNN();
		KNN(int max_k);
		int totalSamples();
		int totalFeatures();
		bool train(Mat trainSet, Mat trainLabels);
		vector<float> find_nearest(int k, Mat testSet);
	private:
		int max_k;
	    vector<KNNVector> samples;
	    int var_count;
};

#endif
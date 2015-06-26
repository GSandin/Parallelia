#ifndef KNNVECTOR_H
#define KNNVECTOR_H

#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>


using namespace cv;
using namespace std;

class KNNVector {
	public:
		KNNVector();
		KNNVector(Mat letter, float label);
        float getLabel();
        void setLabel(float label);
		Mat getEigenvector();
		void setEigenvector(Mat letter);
	private:
		float letterLabel;
	    Mat infoLetter;
};

#endif
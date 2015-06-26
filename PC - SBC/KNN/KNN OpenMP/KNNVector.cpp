#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
#include "KNNVector.hpp"

KNNVector::KNNVector() {
	Mat infoLetter;
	letterLabel = 0;
}

KNNVector::KNNVector(Mat letter, float label) {
    letterLabel = label;
    infoLetter = letter;
}


float KNNVector::getLabel() {
    return letterLabel;
}

void KNNVector::setLabel(float label) {
    letterLabel = label;
}

Mat KNNVector::getEigenvector() {
    return infoLetter;
}

void KNNVector::setEigenvector(Mat letter) {
	infoLetter = letter;
}
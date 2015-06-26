#ifndef main_h
#define main_h

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
#include <ctime>
#include <omp.h>
#include <sys/timeb.h>
using namespace cv;
using namespace std;

#include "knn.hpp"




void average(Mat results, Mat labels_test);

int main(int argc, const char** argv){

	int K=5;

	const double startTime = omp_get_wtime();

	Mat img = imread("digits.png",CV_LOAD_IMAGE_COLOR);

	if(img.empty()){
		cout<< "image cannot be loaded..!!" << endl;
		return -1;
	}

	Mat imgb = Mat(img.rows,img.cols,CV_32F);

	cvtColor(img,imgb,COLOR_BGRA2GRAY);

	cout<<"Digits image convert to 1 channel"<<endl;

	Mat imgtrain = imgb.colRange(0,imgb.cols/2).rowRange(0,imgb.rows);
	Mat imgtest = imgb.colRange(imgb.cols/2,imgb.cols).rowRange(0,imgb.rows);

    Mat train = Mat(0,400,CV_32F);
	Mat test = Mat(0,400,CV_32F);

	Mat trainClasses = Mat(0,1,CV_32F);
	Mat testClasses = Mat(0,1,CV_32F);

	float a=0;

    for(int i=0;i<imgtrain.rows;i+=20){
    	for(int j=0;j<imgtrain.cols;j+=20){
			Mat aux = imgtrain.colRange(j,j+20).rowRange(i,i+20).t();
			train.push_back(aux.reshape(1,1));
			trainClasses.push_back(a);
			testClasses.push_back(a);
			aux = imgtest.colRange(j,j+20).rowRange(i,i+20).t();
			test.push_back(aux.reshape(1,1));
		}
		if((i+20)%100==0){
			a++;
		}
    }

	cout<<"Obtained train and test data"<<endl;

	Mat train_final,test_final;
	train.convertTo(train_final,CV_32F);
	test.convertTo(test_final,CV_32F);
	
	Mat idx;

	//CvKNearest knn( train_final, trainClasses );
	KNN knn = KNN();
	knn.train(train_final, trainClasses);

	cout<<"KNN trained"<<endl;

	Mat results;
	Mat neighbors;
	Mat neighborsResponse;

	float* temp = &(knn.find_nearest(K, test_final))[0];
	//knn.find_nearest(test_final,K,results,neighbors,neighborsResponse);
	results = Mat(test.rows, 0, CV_32FC1, temp);

	const double endTime = omp_get_wtime();
    printf("Duration = %lf seconds\n", (endTime - startTime));

	cout<<"KNN tested"<<endl;
	
	average(results, testClasses);
	/*namedWindow("train", CV_WINDOW_AUTOSIZE);
	namedWindow("test", CV_WINDOW_AUTOSIZE);

	imshow("train", train);
	imshow("test", test);

	vector<int> compression_params;
    compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
    compression_params.push_back(9);

        	imwrite("alpha.png", train, compression_params);

	waitKey(0);

	destroyAllWindows();*/
	return 0;
}

void average(Mat results, Mat labels_test) {
	float correct=0.0, total = results.rows;

	for (int i = 0; i<results.rows; i++) {
		if (results.at<float>(0,i) == labels_test.at<float>(0,i)) {
			correct++;
		}
	}

	float average = (correct/total)*100;
	printf("Average: %f\n",average);
}

#endif

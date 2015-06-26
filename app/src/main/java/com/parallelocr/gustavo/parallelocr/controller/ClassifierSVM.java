package com.parallelocr.gustavo.parallelocr.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import com.parallelocr.gustavo.parallelocr.NoParallel.KNN;
import com.parallelocr.gustavo.parallelocr.NoParallel.SVM;
import com.parallelocr.gustavo.parallelocr.R;
import com.parallelocr.gustavo.parallelocr.controller.exception.KNNException;
import com.parallelocr.gustavo.parallelocr.controller.exception.SVMException;
import com.parallelocr.gustavo.parallelocr.model.KNN.KNNVector;
import com.parallelocr.gustavo.parallelocr.model.SVM.ParamGrid;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMParams;
import com.parallelocr.gustavo.parallelocr.model.SVM.TermCriteria;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ScreenSlideKNNFragment;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ScreenSlideSVMFragment;

import java.util.ArrayList;

/**
 * Created by gustavo on 20/06/15.
 */
public class ClassifierSVM extends AsyncTask<Context,Void,Void> {

    private ScreenSlideSVMFragment fragment;

    public ClassifierSVM(ScreenSlideSVMFragment fragment){
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(Context... context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap img = BitmapFactory.decodeResource(context[0].getResources(), R.drawable.digits, options);
        int a=0, b=0;
        Bitmap images_data[] = new Bitmap[50*50];
        float responses[] = new float[50*50];

        for(int i=0;i<img.getHeight();i+=20){
            for(int j=0;j<img.getWidth()/2;j+=20){
                images_data[a] = Bitmap.createBitmap(img, j, i, 20, 20);
                responses[a] = (float)b;
                a++;
            }
            if((i+20) % 100 == 0){
                b++;
            }
        }

        SVM cl = new SVM();

        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 1000,
                ParamGrid.FLT_EPSILON);
        SVMParams params = new SVMParams(SVM.C_SVC, SVM.LINEAR, 0., 5.383, 0., 2.67, 0., 0. , null, criteria);

        try {
            if (cl.train(converter2list(images_data), converter2list(responses), null, null, params)) {
                System.out.println("SVM entrenado");
                ArrayList<KNNVector> images_test = new ArrayList<KNNVector>(50 * 50);

                for (int i = 0; i < img.getHeight(); i += 20) {
                    for (int j = img.getWidth() / 2; j < img.getWidth(); j += 20) {
                        KNNVector kv = new KNNVector(Bitmap.createBitmap(img, j, i, 20, 20), -1);
                        images_test.add(kv);
                    }
                }

                /*try {
                    int avg = verify(cl.find_nearest(5, images_test));
                    System.out.println(avg*100/2500);
                } catch (KNNException e) {
                    e.printStackTrace();
                }*/

            }
        } catch (SVMException ex) {
            Toast.makeText(context[0],
                    ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            System.out.println(ex);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        fragment.initTimer();
    }

    @Override
    protected void onPostExecute(Void result) {
        fragment.finishTimer();
    }

    /**
     * Method to verify the result
     * @param result
     * @return
     */
    public static int verify(float[] result){
        int a=0;
        float b = 0;

        for(int i=0;i<2500;i++){
            if(result[i]==b){
                a++;
            }
            if(i % 250 == 0 && i != 0){
                b++;
            }
        }
        return a;
    }

    private ArrayList<ArrayList<Float>> converter2list(Bitmap[] images) {
        ArrayList<ArrayList<Float>> lists = new ArrayList<ArrayList<Float>>(images.length);
        int[] temp = new int[images[0].getHeight()*images[0].getWidth()];
        for (int i = 0; i < images.length; i++) {
            ArrayList<Float> imagesConv = new ArrayList<Float>(images[i].getHeight()*images[i].getWidth()+1);
            images[i].getPixels(temp, 0, images[i].getWidth(), 0, 0, images[i].getWidth(), images[i].getHeight());
            for (int j = 0; j < images[i].getHeight()*images[i].getWidth(); j++) {
                imagesConv.add((float)temp[j]);
            }
            lists.add(imagesConv);
        }
        return lists;
    }

    private ArrayList<Float> converter2list(float[] floats) {
        ArrayList<Float> list = new ArrayList<Float>();
        for (int i = 0; i < floats.length; i++) {
            list.add(floats[i]);
        }
        return list;
    }
}

package com.parallelocr.gustavo.parallelocr.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.parallelocr.gustavo.parallelocr.Parallel.KNNP;
import com.parallelocr.gustavo.parallelocr.R;
import com.parallelocr.gustavo.parallelocr.controller.exception.KNNException;
import com.parallelocr.gustavo.parallelocr.model.KNN_Vector;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ScreenSlideKNNFragment;

import java.util.ArrayList;

/**
 * Created by gustavo on 8/02/15.
 */
public class ClassifierKNNP extends AsyncTask<Context,Void,Void> {

    private ScreenSlideKNNFragment fragment;

    public ClassifierKNNP(ScreenSlideKNNFragment fragment){
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
            if(i % 100 == 0 && i != 0){
                b++;
            }
        }

        KNNP cl = new KNNP();

        if(cl.train(images_data,responses)){
            System.out.println("KNN entrenado");
            ArrayList<KNN_Vector> images_test = new ArrayList<KNN_Vector>(50*50);

            for(int i=0;i<img.getHeight();i+=20){
                for(int j=img.getWidth()/2;j<img.getWidth();j+=20){
                    KNN_Vector kv = new KNN_Vector(Bitmap.createBitmap(img, j, i, 20, 20),-1);
                    images_test.add(kv);
                }
            }

            try {
                int avg = verify(cl.find_nearest(5, images_test, context[0]));
                System.out.println(avg*100/2500);
            } catch (KNNException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {fragment.initTimer();}

    @Override
    protected void onPostExecute(Void result) {fragment.finishTimer();}

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
}

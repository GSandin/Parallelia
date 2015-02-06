package com.parallelia.gustavo.parallelia.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.parallelia.gustavo.parallelia.NoParallel.KNN;
import com.parallelia.gustavo.parallelia.R;
import com.parallelia.gustavo.parallelia.controller.exception.KNNException;
import com.parallelia.gustavo.parallelia.model.KNN_Vector;

import java.util.ArrayList;

/**
 * Created by gustavo on 2/02/15.
 */
public class Classifier extends AsyncTask<Context,Void,Void> {

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
            if(i % 100 == 0){
                b++;
            }
        }

        KNN cl = new KNN();

        if(cl.train(images_data,responses)){
            System.out.println("KNN entrenado");
            ArrayList<KNN_Vector> images_test = new ArrayList<KNN_Vector>(50*50);
            String results[] = new String[50*50];

            for(int i=0;i<img.getHeight();i+=20){
                for(int j=img.getWidth()/2;j<img.getWidth();j+=20){
                    KNN_Vector kv = new KNN_Vector(Bitmap.createBitmap(img, j, i, 20, 20),-1);
                    images_test.add(kv);
                }
            }

            try {
                cl.find_nearest(5, images_test, results);
            } catch (KNNException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}

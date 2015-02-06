package com.parallelia.gustavo.parallelia.model;

import android.graphics.Bitmap;

/**
 * Created by gustavo on 16/01/15.
 */
public class KNN_Vector {
    //atributos
    private float label;
    private int[] eigenvector;

    public KNN_Vector(Bitmap image, float label){
        this.label = label;
        this.eigenvector = new int[image.getWidth()*image.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        image.getPixels(this.eigenvector, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        //this.setEigenvector(eigenvector);
    }


    public float getLabel() {
        return label;
    }

    public void setLabel(float label) {
        this.label = label;
    }

    public int[] getEigenvector() {
        return eigenvector;
    }

    public void setEigenvector(Bitmap image) {
        this.eigenvector = new int[image.getWidth()*image.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        image.getPixels(this.eigenvector, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        //this.eigenvector = eigenvector;
    }
}

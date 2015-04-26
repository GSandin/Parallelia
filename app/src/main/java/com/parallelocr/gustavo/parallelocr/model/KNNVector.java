package com.parallelocr.gustavo.parallelocr.model;

import android.graphics.Bitmap;

import com.parallelocr.gustavo.parallelocr.controller.Utils.ImgProc;

/**
 * Created by gustavo on 16/01/15.
 */
public class KNNVector {
    //atributos
    private float label;
    private int[] eigenvector;

    public KNNVector(Bitmap image, float label){
        this.label = label;
        this.eigenvector = new int[image.getWidth()*image.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        image.getPixels(this.eigenvector, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        this.eigenvector = ImgProc.toGrayscale(this.eigenvector);
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

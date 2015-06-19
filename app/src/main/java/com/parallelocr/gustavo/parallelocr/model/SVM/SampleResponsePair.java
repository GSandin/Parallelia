package com.parallelocr.gustavo.parallelocr.model.SVM;

import java.util.ArrayList;

/**
 * Created by gustavo on 19/06/15.
 */
public class SampleResponsePair {
    private ArrayList<Float> sample;
    private ArrayList<Float> mask;
    private float response;
    private int index;


    public ArrayList<Float> getSample() {
        return sample;
    }

    public void setSample(ArrayList<Float> sample) {
        this.sample = sample;
    }

    public ArrayList<Float> getMask() {
        return mask;
    }

    public void setMask(ArrayList<Float> mask) {
        this.mask = mask;
    }

    public float getResponse() {
        return response;
    }

    public void setResponse(float response) {
        this.response = response;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

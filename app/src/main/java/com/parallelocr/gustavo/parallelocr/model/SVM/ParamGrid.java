package com.parallelocr.gustavo.parallelocr.model.SVM;

import com.parallelocr.gustavo.parallelocr.controller.exception.ParamGridException;

/**
 * Created by gustavo on 26/04/15.
 */
public class ParamGrid {

    public static final int SVM_C = 0;
    public static final int SVM_COEF = 4;
    public static final int SVM_DEGREE = 5;
    public static final int SVM_GAMMA = 1;
    public static final int SVM_NU = 3;
    public static final int SVM_P = 2;

    private static final double DBL_EPSILON = 2.2204460492503131e-16;
    private static final double FLT_EPSILON = 1.1920929e-07F;

    private double min_val;
    private double max_val;
    private int step;

    public ParamGrid() {
        min_val = max_val = step =0;
    }

    public boolean check() throws ParamGridException{
        boolean ok = false;

        if( min_val > max_val )
            throw new ParamGridException("Lower bound of the grid must be less then the upper one" );
        if( min_val < DBL_EPSILON )
            throw new ParamGridException("Lower bound of the grid must be positive" );
        if( step < 1. + FLT_EPSILON )
            throw new ParamGridException("Grid step must greater then 1" );

        ok = true;

        return ok;
    }

    public double getMin_val() {
        return min_val;
    }

    public void setMin_val(double min_val) {
        this.min_val = min_val;
    }

    public double getMax_val() {
        return max_val;
    }

    public void setMax_val(double max_val) {
        this.max_val = max_val;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}

package com.parallelocr.gustavo.parallelocr.model;

import com.parallelocr.gustavo.parallelocr.NoParallel.SVM;
/**
 * Created by gustavo on 26/04/15.
 */
public class SVMKernel {

    private SVMParams params;
    private int calc_func;
    public static final float FLT_MAX = 3.40282347E+38F;

    public SVMKernel(SVMParams params, int calc_func) {
        this.params = params;
        this.calc_func = calc_func;

    }

    /**
     * Method to calc the result depence the type
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     */
    public void calc( int vcount, int var_count, float[][] vecs, float[] another,
                         float[] results) {

        float max_val = (float)(FLT_MAX*1e-3);
        int j;

        switch(params.getKernel_type()) {
            case SVM.RBF:
                calc_rbf(vcount, var_count, vecs, another, results);
                break;
            case SVM.POLY:
                calc_poly(vcount, var_count, vecs, another, results);
                break;
            case SVM.SIGMOID:
                calc_sigmoid(vcount, var_count, vecs, another, results);
                break;
            default:
                calc_linear(vcount, var_count, vecs, another, results);
                break;
        }

        for( j = 0; j < vcount; j++ )
        {
            if( results[j] > max_val )
                results[j] = max_val;
        }

    }

    /**
     * Method to calc the result for non rbf base
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     * @param alpha
     * @param beta
     */
    private void calc_non_rbf_base( int vcount, int var_count, float[][] vecs, float[] another,
                                    float[] results, double alpha, double beta ) {
        int j, k;
        for( j = 0; j < vcount; j++ )
        {
            float[] sample = vecs[j];
            double s = 0;
            for( k = 0; k <= var_count - 4; k += 4 )
                s += sample[k]*another[k] + sample[k+1]*another[k+1] +
                        sample[k+2]*another[k+2] + sample[k+3]*another[k+3];
            for( ; k < var_count; k++ )
                s += sample[k]*another[k];
            results[j] = (float)(s*alpha + beta);
        }
    }

    /**
     * Method to calc the result for linear
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     */
    private void calc_linear( int vcount, int var_count, float[][] vecs, float[] another,
                              float[] results ) {
        calc_non_rbf_base( vcount, var_count, vecs, another, results, 1, 0 );
    }

    /**
     * Method to calc the result for polynomial
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     */
    private void calc_poly( int vcount, int var_count, float[][] vecs, float[] another,
                            float[] results ) {
        calc_non_rbf_base( vcount, var_count, vecs, another, results, params.getGamma(), params.getCoef0() );
        if( vcount > 0 )
            pow(results, params.getDegree());
    }

    /**
     * method to calc the result for signmoid
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     */
    private void calc_sigmoid( int vcount, int var_count, float[][] vecs, float[] another, float[] results ) {
        int j;
        calc_non_rbf_base( vcount, var_count, vecs, another, results, -2*params.getGamma(),
                -2*params.getCoef0() );
        //TODO: speedup this
        for( j = 0; j < vcount; j++ )
        {
            float t = results[j];
            double e = Math.exp(-Math.abs(t));
            if( t > 0 )
                results[j] = (float)((1. - e)/(1. + e));
            else
                results[j] = (float)((e - 1.)/(e + 1.));
        }
    }


    /**
     * Method to calc the result for rbf
     * @param vcount
     * @param var_count
     * @param vecs
     * @param another
     * @param results
     */
    private void calc_rbf( int vcount, int var_count, float[][] vecs, float[] another,
                           float[] results ) {
        double gamma = -params.getGamma();
        int j, k;

        for( j = 0; j < vcount; j++ )
        {
            float[] sample = vecs[j];
            double s = 0;

            for( k = 0; k <= var_count - 4; k += 4 )
            {
                double t0 = sample[k] - another[k];
                double t1 = sample[k+1] - another[k+1];

                s += t0*t0 + t1*t1;

                t0 = sample[k+2] - another[k+2];
                t1 = sample[k+3] - another[k+3];

                s += t0*t0 + t1*t1;
            }

            for( ; k < var_count; k++ )
            {
                double t0 = sample[k] - another[k];
                s += t0*t0;
            }
            results[j] = (float)(s*gamma);
        }

        if( vcount > 0 )
            exp( results );
    }

    /**
     * Method to aplicate funtcion pow in array
     * @param results
     * @param degree
     */
    private void pow(float[] results, double degree) {
        for (int i = 0; i < results.length; i++) {
            results[i] = (float)Math.pow(results[i], degree);
        }
    }

    /**
     * Method to aplicate function exp in array
     * @param results
     */
    private void exp(float[] results) {
        for (int i = 0; i < results.length; i++) {
            results[i] = (float)Math.exp(results[i]);
        }
    }
}

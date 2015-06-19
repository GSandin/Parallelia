package com.parallelocr.gustavo.parallelocr.model.SVM;

/**
 * Created by gustavo on 14/06/15.
 */
public class SVMDecisionFunc {
    private double rho;
    private int sv_count;
    private double[] alpha;
    private int[] sv_index;

    public SVMDecisionFunc(double rho, int sv_count, double[] alpha, int[] sv_index) {
        this.setRho(rho);
        this.setSv_count(sv_count);
        this.setAlpha(alpha);
        this.setSv_index(sv_index);
    }

    public SVMDecisionFunc() {

    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public int getSv_count() {
        return sv_count;
    }

    public void setSv_count(int sv_count) {
        this.sv_count = sv_count;
    }

    public double[] getAlpha() {
        return alpha;
    }

    public void setAlpha(double[] alpha) {
        this.alpha = alpha;
    }

    public int[] getSv_index() {
        return sv_index;
    }

    public void setSv_index(int[] sv_index) {
        this.sv_index = sv_index;
    }
}

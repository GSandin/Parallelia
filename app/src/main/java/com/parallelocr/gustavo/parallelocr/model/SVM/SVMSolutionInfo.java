package com.parallelocr.gustavo.parallelocr.model.SVM;

/**
 * Created by gustavo on 14/06/15.
 */
public class SVMSolutionInfo {
    private double obj;
    private double rho;
    private double upper_bound_p;
    private double upper_bound_n;
    private double r;

    public SVMSolutionInfo(double obj, double rho, double upper_bound_p, double upper_bound_n, double r) {
        this.setObj(obj);
        this.setRho(rho);
        this.setUpper_bound_p(upper_bound_p);
        this.setUpper_bound_n(upper_bound_n);
        this.setR(r);
    }

    public SVMSolutionInfo() {

    }

    public double getObj() {
        return obj;
    }

    public void setObj(double obj) {
        this.obj = obj;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public double getUpper_bound_p() {
        return upper_bound_p;
    }

    public void setUpper_bound_p(double upper_bound_p) {
        this.upper_bound_p = upper_bound_p;
    }

    public double getUpper_bound_n() {
        return upper_bound_n;
    }

    public void setUpper_bound_n(double upper_bound_n) {
        this.upper_bound_n = upper_bound_n;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }
}

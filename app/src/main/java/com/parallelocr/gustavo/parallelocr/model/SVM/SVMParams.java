package com.parallelocr.gustavo.parallelocr.model.SVM;

import com.parallelocr.gustavo.parallelocr.NoParallel.SVM;

/**
 * Created by gustavo on 19/04/15.
 */
public class SVMParams {
    //artibutes
    private double C;
    private double coef0;
    private double degree;
    private double gamma;
    private int kernel_type;
    private double nu;
    private double p;
    private int svm_type;
    private TermCriteria term_crit;
    private float[] class_weight;

    //Constructors
    public SVMParams(){
        this.C = 1;
        this.coef0 = 0;
        this.degree = 0;
        this.gamma = 1;
        this.kernel_type = SVM.RBF;
        this.nu = 0;
        this.p = 0;
        this.svm_type = SVM.C_SVC;
        this.term_crit = new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,1000,Math.E);
    }

    public SVMParams(double C, double coef0, double degree, double gamma, int kernel_type, double nu,
                     double p, int svm_type, TermCriteria term_crit){
        this.C = C;
        this.coef0 = coef0;
        this.degree = degree;
        this.gamma = gamma;
        this.kernel_type = kernel_type;
        this.nu = nu;
        this.p = p;
        this.svm_type = svm_type;
        this.term_crit = term_crit;
    }

    /**
     *
     * @return C
     */
    public double getC() {
        return C;
    }

    /**
     *
     * @param c
     */
    public void setC(double c) {
        C = c;
    }

    /**
     *
     * @return
     */
    public double getCoef0() {
        return coef0;
    }

    /**
     *
     * @param coef0
     */
    public void setCoef0(double coef0) {
        this.coef0 = coef0;
    }

    /**
     *
     * @return
     */
    public double getDegree() {
        return degree;
    }

    /**
     *
     * @param degree
     */
    public void setDegree(double degree) {
        this.degree = degree;
    }

    /**
     *
     * @return
     */
    public double getGamma() {
        return gamma;
    }

    /**
     *
     * @param gamma
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     *
     * @return
     */
    public int getKernel_type() {
        return kernel_type;
    }

    /**
     *
     * @param kernel_type
     */
    public void setKernel_type(int kernel_type) {
        this.kernel_type = kernel_type;
    }

    /**
     *
     * @return
     */
    public double getNu() {
        return nu;
    }

    /**
     *
     * @param nu
     */
    public void setNu(double nu) {
        this.nu = nu;
    }

    /**
     *
     * @return
     */
    public double getP() {
        return p;
    }

    /**
     *
     * @param p
     */
    public void setP(double p) {
        this.p = p;
    }

    /**
     *
     * @return
     */
    public int getSvm_type() {
        return svm_type;
    }

    /**
     *
     * @param svm_type
     */
    public void setSvm_type(int svm_type) {
        this.svm_type = svm_type;
    }

    /**
     *
     * @return
     */
    public TermCriteria getTerm_crit() {
        return term_crit;
    }

    /**
     *
     * @param term_crit
     */
    public void setTerm_crit(TermCriteria term_crit) {
        this.term_crit = term_crit;
    }

    public float[] getClass_weight() {
        return class_weight;
    }

    public void setClass_weight(float[] class_weight) {
        this.class_weight = class_weight;
    }
}

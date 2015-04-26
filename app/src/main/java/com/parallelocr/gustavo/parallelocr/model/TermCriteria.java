package com.parallelocr.gustavo.parallelocr.model;

/**
 * Created by gustavo on 19/04/15.
 */
public class TermCriteria {
    //Atributes
    public static final int COUNT=1;
    public static final int MAX_ITER=COUNT;
    public static final int EPS=2;
    private double epsilon;
    private int maxCount;
    private int type;

    //Constructor
    public TermCriteria(int type, int maxCount, double epsilon){
        this.type = type;
        this.maxCount = maxCount;
        this.epsilon = epsilon;
    }

    /**
     *
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     *
     * @param epsilon
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     *
     * @return
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     *
     * @param maxCount
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }
}

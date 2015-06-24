package com.parallelocr.gustavo.parallelocr.model.SVM;

import java.util.ArrayList;

/**
 * Created by gustavo on 14/06/15.
 */
public class SVMKernelRow {
    private SVMKernelRow prev;
    private SVMKernelRow next;
    private ArrayList<Float> data;

    public SVMKernelRow(SVMKernelRow prev, SVMKernelRow next, ArrayList<Float> data) {
        this.setPrev(prev);
        this.setNext(next);
        this.setData(data);
    }

    public SVMKernelRow() {

    }

    public SVMKernelRow getPrev() {
        return prev;
    }

    public void setPrev(SVMKernelRow prev) {
        this.prev = prev;
    }

    public SVMKernelRow getNext() {
        return next;
    }

    public void setNext(SVMKernelRow next) {
        this.next = next;
    }

    public ArrayList<Float> getData() {
        return data;
    }

    public void setData(ArrayList<Float> data) {
        this.data = data;
    }
}

package com.parallelocr.gustavo.parallelocr.Parallel;

import android.content.Context;
import android.graphics.Bitmap;
//import android.renderscript.Allocation;
import android.support.v8.renderscript.*;

import com.parallelocr.gustavo.parallelocr.controller.exception.KNNException;
import com.parallelocr.gustavo.parallelocr.model.KNN_Vector;

import java.util.ArrayList;

/**
 * Created by gustavo on 6/02/15.
 */
public class KNNP {
    // RenderScript-specific properties:
    // RS context
    private RenderScript rs;
    // "Glue" class that wraps access to the script.
    // The IDE generates the class automatically based on the rs file, the class is located in the 'gen'
    // folder.
    private ScriptC_knn script;
    // Allocations - memory abstractions that RenderScript kernels operate on.
    private Allocation allocationIn;
    private Allocation allocationOut;

    //atributtes
    private int max_k;
    private ArrayList<KNN_Vector> samples;
    private int var_count;

    public KNNP() {
        this.var_count = 0;
        this.max_k = 32;
        this.samples = new ArrayList<KNN_Vector>();
    }

    public KNNP(int max_k) {
        this.max_k = max_k;
        this.var_count = 0;
        this.samples = new ArrayList<KNN_Vector>();
    }

    /**
     * Method to return the total number of train samples
     *
     * @return
     */
    public int TotalSamples() {
        return samples.size();
    }

    /**
     * Method to return the total features used
     *
     * @return
     */
    public int TotalFeatures() {
        return var_count;
    }

    /**
     * Method to train the KNN classifer
     *
     * @param images
     * @param labels
     * @return
     */
    public Boolean train(Bitmap[] images, float[] labels) {
        if (images.length != labels.length) {
            return false;
        }

        for (int i = 0; i < labels.length; i++) {
            KNN_Vector kv = new KNN_Vector(images[i], labels[i]);
            samples.add(kv);
        }

        var_count = samples.get(0).getEigenvector().length;

        return true;
    }

    public float[] find_nearest(int k, ArrayList<KNN_Vector> test_data, Context context) throws KNNException {
        if (samples.size() <= 0) {
            throw new KNNException("The KNN classifer is not ready for find neighbord!");
        }

        if (k < 1 || k > max_k) {
            throw new KNNException("k must be within 1 and max_k range.");
        }

        float results[] = new float[test_data.size()];
        //renderscript
        rs = RenderScript.create(context);
        script = new ScriptC_knn(rs);

        //create allocations
        //Type t = new Type.Builder(rs, Element.I32(rs)).setX(5).create();
        allocationIn = Allocation.createSized(rs, Element.I32(rs),test_data.size());
        //allocationIn = Allocation.createTyped(rs, t);
        allocationIn.copyFrom(initVector(test_data.size()));

        allocationOut = Allocation.createSized(rs, Element.F32(rs), test_data.size());
        allocationOut.copyFrom(results);

        Allocation samples_a = Allocation.createSized(rs,Element.I32(rs),this.var_count*this.samples.size(), Allocation.USAGE_SCRIPT);
        samples_a.copy1DRangeFrom(0, samplestoAllocation().length, samplestoAllocation());
        //samples_a.copyFrom(samplestoAllocation());

        Allocation test_data_a = Allocation.createSized(rs, Element.I32(rs), this.var_count * test_data.size(), Allocation.USAGE_SCRIPT);
        test_data_a.copy1DRangeFrom(0, test_datatoAllocation(test_data).length, test_datatoAllocation(test_data));
        //test_data_a.copyFrom(test_datatoAllocation(test_data));

        Allocation tags = Allocation.createSized(rs,Element.F32(rs),test_data.size(), Allocation.USAGE_SCRIPT);
        tags.copy1DRangeFrom(0, tagstoAllocation().length, tagstoAllocation());
        //tags.copyFrom(tagstoAllocation());

        //set globals variables
        script.set_k(k);
        script.set_len_results(test_data.size());
        script.set_len_samples(samples.size());
        script.set_var_count(this.var_count);
        script.set_samples(samples_a);
        script.set_tags(tags);
        script.set_test_data(test_data_a);

        //run parallel knn
        script.forEach_knn(allocationIn, allocationOut);
        //recolect results
        allocationOut.copyTo(results);
        rs.finish();

        return results;
    }

    /**
     * Method to init vector to renderscript
     * @param n
     * @return
     */
    private int[] initVector(int n){
        int vector[] = new int[n];

        for(int i=0;i<n;i++){
            vector[i]=i;
        }

        return vector;
    }

    /**
     * Method to tranform the samples to data for renderscript
     * @return
     */
    private int[] samplestoAllocation(){
        int samples[] = new int[this.var_count*this.samples.size()];
        int a=0;

        for(int i=0;i<this.samples.size();i++){
            KNN_Vector kn =this.samples.get(i);
            for(int j=0;j<this.var_count;j++){
                samples[a] = kn.getEigenvector()[j];
                a++;
            }
        }

        return samples;
    }

    /**
     * Method to transform the test data to data for renderscript
     * @param test_data
     * @return
     */
    private int[] test_datatoAllocation(ArrayList<KNN_Vector> test_data){
        int td[] = new int[this.var_count*test_data.size()];
        int a=0;

        for(int i=0;i<test_data.size();i++){
            KNN_Vector kn =test_data.get(i);
            for(int j=0;j<this.var_count;j++){
                td[a] = kn.getEigenvector()[j];
                a++;
            }
        }

        return td;
    }

    /**
     * Method to tranform the tags to dara for renderscript
     * @return
     */
    private float[] tagstoAllocation(){
        float tags[] = new float[this.samples.size()];

        for(int i=0;i<this.samples.size();i++){
            tags[i] = this.samples.get(i).getLabel();
        }

        return tags;
    }

}

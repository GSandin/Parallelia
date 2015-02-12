package com.parallelia.gustavo.parallelia.Parallel;

import android.content.Context;
import android.graphics.Bitmap;
//import android.renderscript.Allocation;
import android.support.v8.renderscript.*;
import android.widget.Toast;

import com.parallelia.gustavo.parallelia.controller.exception.KNNException;
import com.parallelia.gustavo.parallelia.model.KNN_Vector;

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

    public float find_nearest(int k, ArrayList<KNN_Vector> test_data,String[] results, Context context) throws KNNException {
        if (samples.size() <= 0) {
            throw new KNNException("The KNN classifer is not ready for find neighbord!");
        }

        if (k < 1 || k > max_k) {
            throw new KNNException("k must be within 1 and max_k range.");
        }

        float temp_result[] = new float[5];

        //results = new String[test_data.size()];
        //renderscript
        rs = RenderScript.create(context);
        script = new ScriptC_knn(rs);

        //create allocations
        //Type t = new Type.Builder(rs, Element.I32(rs)).setX(5).create();
        allocationIn = Allocation.createSized(rs, Element.I32(rs),5);
        //allocationIn = Allocation.createTyped(rs, t);
        allocationIn.copyFrom(initVector(5));

        allocationOut = Allocation.createSized(rs, Element.F32(rs), 5);

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
        allocationOut.copyTo(temp_result);

        String text = "RS:" + temp_result[0] + " - " + temp_result[3];
        //int duration = Toast.LENGTH_SHORT;

        //Toast toast = Toast.makeText(context, text, duration);
        //toast.show();
        System.out.println(text);
        /*for (int s = 0; s < test_data.size(); s++) {
            KNN_Vector test = test_data.get(s);
            System.out.println(test.getLabel());
            int dd[] = new int[k];
            float nr[] = new float[results.length+k*this.var_count];
            for (int i = 0; i < samples.size(); i++) {
                KNN_Vector sample = samples.get(i);
                for (int j = 0; j < this.var_count; j++) {
                    int sum = 0, t, ii, ii1;
                    int[] pixels_train = sample.getEigenvector();

                    for (t = 0; t <= this.var_count - 4; t += 4) {
                        double t0 = test.getEigenvector()[t] - pixels_train[t], t1 = test.getEigenvector()[t + 1] - pixels_train[t + 1];
                        double t2 = test.getEigenvector()[t + 2] - pixels_train[t + 2], t3 = test.getEigenvector()[t + 3] - pixels_train[t + 3];
                        sum += t0 * t0 + t1 * t1 + t2 * t2 + t3 * t3;
                    }

                    for (; t < this.var_count; t++) {
                        double t0 = test.getEigenvector()[t] - pixels_train[t];
                        sum += t0 * t0;
                    }

                    for (ii = k1 - 1; ii >= 0; ii--) {
                        if (sum > dd[ii])
                            break;
                    }
                    if (ii < k - 1) {
                        for (ii1 = k2 - 1; ii1 > ii; ii1--) {
                            dd[(ii1 + 1)] = dd[ii1];
                            nr[(ii1 + 1)] = nr[ii1];
                        }

                        dd[(ii + 1)] = sum;
                        nr[(ii + 1)] = sample.getLabel();//pixels_train[pixels_train.length - 1];
                    }
                    k1 = (k1 + 1) < k ? (k1 + 1) : k;
                    k2 = k1 < (k - 1) ? k1 : (k - 1);
                }
            }

            int prev_start = 0, best_count = 0, cur_count;
            float best_val = 0;

            for (int j = k1 - 1; j > 0; j--) {
                boolean swap_f1 = false;
                for (int j1 = 0; j1 < j; j1++) {
                    if (nr[j1] > nr[(j1 + 1)]) {
                        float t;
                        t = nr[j1];
                        nr[j1] = nr[(j1 + 1)];
                        nr[(j1 + 1)] = t;
                        swap_f1 = true;
                    }
                }
                if (!swap_f1)
                    break;
            }

            for (int j = 1; j <= k1; j++) {
                if (j == k1 || nr[j] != nr[(j - 1)]) {
                    cur_count = j - prev_start;
                    if (best_count < cur_count) {
                        best_count = cur_count;
                        best_val = nr[(j - 1)];
                    }
                    prev_start = j;
                }
            }
            System.out.println(best_val);
            results[s] = String.valueOf(best_val);
        }*/
        return 0;
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

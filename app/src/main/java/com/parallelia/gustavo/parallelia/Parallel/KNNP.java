package com.parallelia.gustavo.parallelia.Parallel;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

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

        float temp_result[] = new float[samples.size()];

        //results = new String[test_data.size()];
        //renderscript
        rs = RenderScript.create(context);
        script = new ScriptC_knn(rs);

        //create allocations
        allocationIn = Allocation.createSized(rs, Element.I32(rs),this.var_count);
        allocationIn.copyTo(testdatatoAllocation(test_data));

        allocationOut = Allocation.createSized(rs, Element.U8(rs),test_data.size());

        Allocation samples_a = Allocation.createSized(rs,Element.F32(rs),this.var_count*this.samples.size());
        samples_a.copyTo(samplestoAllocationO());

        Allocation tags = Allocation.createSized(rs,Element.I32(rs),test_data.size());
        tags.copyTo(tagstoAllocation());

        //set globals variables
        script.set_k(k);
        script.set_len_results(test_data.size());
        script.set_len_samples(samples.size());
        script.set_var_count(this.var_count);
        script.bind_samples(samples_a);
        script.bind_tags(tags);

        //run parallel knn
        script.forEach_root(allocationIn, allocationOut);
        //recolect results
        allocationOut.copyTo(temp_result);

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

    private int[] samplestoAllocationO(){
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

    private float[] tagstoAllocation(){
        float tags[] = new float[this.samples.size()];

        for(int i=0;i<this.samples.size();i++){
            tags[i] = this.samples.get(i).getLabel();
        }

        return tags;
    }

    private int[] testdatatoAllocation(ArrayList<KNN_Vector> test_data){
        int td[] = new int[this.var_count*test_data.size()];
        int a = 0;

        for(int i=0;i<test_data.size();i++){
            KNN_Vector kn = test_data.get(i);
            for(int j=0;j<this.var_count;j++){
                td[a] = kn.getEigenvector()[j];
                a++;
            }
        }

        return td;
    }
}

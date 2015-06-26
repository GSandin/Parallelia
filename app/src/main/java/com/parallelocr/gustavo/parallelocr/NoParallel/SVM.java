package com.parallelocr.gustavo.parallelocr.NoParallel;

import com.parallelocr.gustavo.parallelocr.controller.exception.SVMException;
import com.parallelocr.gustavo.parallelocr.model.SVM.ParamGrid;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMDecisionFunc;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernel;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMParams;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMSolutionInfo;
import com.parallelocr.gustavo.parallelocr.model.SVM.SampleResponsePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gustavo on 5/04/15.
 */
public class SVM {
    //atributes
    public static final int	C = 0;
    public static final int	C_SVC = 100;
    public static final int	COEF = 4;
    public static final int	DEGREE = 5;
    public static final int	EPS_SVR = 103;
    public static final int	GAMMA = 1;
    public static final int	LINEAR = 0;
    public static final int	NU = 3;
    public static final int	NU_SVC = 101;
    public static final int	NU_SVR = 104;
    public static final int	ONE_CLASS = 102;
    public static final int	P = 2;
    public static final int	POLY = 1;
    public static final int	RBF = 2;
    public static final int	SIGMOID	= 3;
    public static final double DBL_EPSILON = 2.2204460492503131E-16;

    SVMParams params;
    int var_all;
    float[][] sv;
    int sv_total;
    ArrayList var_idx;
    ArrayList<Float> class_labels;
    ArrayList<Float> class_weights;
    ArrayList<SVMDecisionFunc> decision_func;
    ArrayList<Float> storage;
    SVMSolver solver;
    SVMKernel kernel;


    public SVM() {
        clear();
    }

    public int getSupportVectorCount() {
        return sv_total;
    }

    public void createKernel() {
        kernel = new SVMKernel(params, 0);
    }

    public void createSolver() {
        solver = new SVMSolver();
    }

    public boolean setParams(SVMParams params) throws SVMException {
        boolean ok = false;

        int kernel_type, svm_type;

        this.params = params;

        kernel_type = params.getKernel_type();
        svm_type = params.getSvm_type();

        if( kernel_type != LINEAR && kernel_type != POLY &&
                kernel_type != SIGMOID && kernel_type != RBF )
            throw new SVMException("Unknown/unsupported kernel type");

        if( kernel_type == LINEAR )
            params.setGamma(1);
        else if( params.getGamma() <= 0 )
            throw new SVMException("gamma parameter of the kernel must be positive" );

        if( kernel_type != SIGMOID && kernel_type != POLY )
            params.setCoef0(0);
        else if( params.getCoef0() < 0 )
            throw new SVMException("The kernel parameter <coef0> must be positive or zero" );

        if( kernel_type != POLY )
            params.setDegree(0);
        else if( params.getDegree() <= 0 )
            throw new SVMException("The kernel parameter <degree> must be positive" );

        if( svm_type != C_SVC && svm_type != NU_SVC &&
                svm_type != ONE_CLASS && svm_type != EPS_SVR &&
                svm_type != NU_SVR )
            throw new SVMException("Unknown/unsupported SVM type" );

        if( svm_type == ONE_CLASS || svm_type == NU_SVC )
            params.setC(0);
        else if( params.getC() <= 0 )
            throw new SVMException("The parameter C must be positive" );

        if( svm_type == C_SVC || svm_type == EPS_SVR )
            params.setNu(0);
        else if( params.getNu() <= 0 || params.getNu() >= 1 )
            throw new SVMException("The parameter nu must be between 0 and 1" );

        if( svm_type != EPS_SVR )
            params.setP(0);
        else if( params.getP() <= 0 )
            throw new SVMException("The parameter p must be positive" );

        if( svm_type != C_SVC )
            params.setClass_weight(null);

        params.getTerm_crit().setEpsilon(Math.max(params.getTerm_crit().getEpsilon(), DBL_EPSILON));
        ok = true;

        return ok;
    }

    public float[] getSupportVector(int i) {
        if (sv != null && i > 0) {
            return sv[i];
        } else {
            return null;
        }
    }

    public void optimizeLinearSvm() {
        // we optimize only linear SVM: compress all the support vectors into one.
        if( params.getKernel_type() != LINEAR )
            return;

        int class_count = class_labels != null ? class_labels.size() :
                params.getSvm_type() == SVM.ONE_CLASS ? 1 : 0;

        int i, df_count = class_count > 1 ? class_count*(class_count-1)/2 : 1;
        ArrayList<SVMDecisionFunc> df = decision_func;

        for( i = 0; i < df_count; i++ )
        {
            int sv_count = df.get(i).getSv_count();
            if( sv_count != 1 )
                break;
        }

        // if every decision functions uses a single support vector;
        // it's already compressed. skip it then.
        if( i == df_count )
            return;

        int var_count = get_var_count();

        double[] v = new double[var_count];
        float[][] new_sv = new float[df_count][];

        for( i = 0; i < df_count; i++ )
        {
            new_sv[i] = new float[var_count];
            float[] dst = new_sv[i];
            int j, k, sv_count = df.get(i).getSv_count();
            for( j = 0; j < sv_count; j++ )
            {
                float[] src = class_count > 1 && df.get(i).getSv_index().length != 0 ?
                        sv[df.get(i).getSv_index()[j]] : sv[j];
                double a = df.get(i).getAlpha()[j];
                for( k = 0; k < var_count; k++ )
                    v[k] += src[k]*a;
            }
            for( k = 0; k < var_count; k++ )
                dst[k] = (float)v[k];
            df.get(i).setSv_count(1);
            df.get(i).getAlpha()[0] = 1.;
            if( class_count > 1 && df.get(i).getSv_index().length != 0 )
                df.get(i).getSv_index()[0] = i;
        }

        sv = new_sv;
        sv_total = df_count;
    }

    public ParamGrid getDefaultFrid( int param_id ) throws SVMException {
        ParamGrid grid = new ParamGrid();
        if( param_id == SVM.C )
        {
            grid.setMin_val(0.1);
            grid.setMax_val(500);
            grid.setStep(5); // total iterations = 5
        }
        else if( param_id == SVM.GAMMA )
        {
            grid.setMin_val(1e-5);
            grid.setMax_val(0.6);
            grid.setStep(15); // total iterations = 4
        }
        else if( param_id == SVM.P )
        {
            grid.setMin_val(0.01);
            grid.setMax_val(100);
            grid.setStep(7); // total iterations = 4
        }
        else if( param_id == SVM.NU )
        {
            grid.setMin_val(0.01);
            grid.setMax_val(0.2);
            grid.setStep(3); // total iterations = 3
        }
        else if( param_id == SVM.COEF )
        {
            grid.setMin_val(0.1);
            grid.setMax_val(300);
            grid.setStep(14); // total iterations = 3
        }
        else if( param_id == SVM.DEGREE )
        {
            grid.setMin_val(0.01);
            grid.setMax_val(4);
            grid.setStep(7); // total iterations = 3
        }
        else
            throw new SVMException( "Invalid type of parameter ");
        return grid;
    }

    // switching function
    public boolean train1( int sample_count, int var_count, ArrayList<ArrayList<Float>> samples,
                        ArrayList<Float>_responses, double Cp, double Cn,
                        ArrayList<Float> _storage, ArrayList<Double> alpha, SVMDecisionFunc decision ) {
        boolean ok = false;

        SVMSolutionInfo si = new SVMSolutionInfo();
        int svm_type = params.getSvm_type();

        si.setRho(0.);

        try {
            ok = svm_type == C_SVC ? solver.solveCSvc(sample_count, var_count, samples, _responses,
                    Cp, Cn, _storage, kernel, alpha, si) : svm_type == NU_SVC ? solver.solveNuSvc(sample_count,
                    var_count, samples, _responses, _storage, kernel, alpha, si) : svm_type ==
                    ONE_CLASS ? solver.solveOneClass(sample_count, var_count, samples, _storage, kernel, alpha, si) :
                    svm_type == EPS_SVR ? solver.solveEpsSvr(sample_count, var_count, samples, _responses,
                    _storage, kernel, alpha, si): svm_type == NU_SVR ? solver.solveNuSvr(sample_count,
                    var_count, samples, _responses, _storage, kernel, alpha, si) : false;
        } catch (SVMException ex) {
            System.out.println(ex);
        }
        decision.setRho(si.getRho());

        return ok;
    }


    public boolean do_train( int svm_type, int sample_count, int var_count, ArrayList<ArrayList<Float>> samples,
                             ArrayList<Float> responses, ArrayList<Float> temp_storage, ArrayList<Double> alpha ) throws SVMException {
        boolean ok = false;

        int sample_size = var_count;
        int i, j, k;

        storage = new ArrayList<Float>();

        if( svm_type == ONE_CLASS || svm_type == EPS_SVR || svm_type == NU_SVR )
        {
            int sv_count = 0;

            SVMDecisionFunc df = decision_func.get(0);

            df.setRho(0.);
            if( !train1( sample_count, var_count, samples, svm_type == ONE_CLASS ? null :
                    responses, 0, 0, temp_storage, alpha, df ))
                return false;

            for( i = 0; i < sample_count; i++ ) {
                if (Math.abs(alpha.get(i)) > 0) {
                    sv_count += Math.abs(alpha.get(i));
                }
            }

            df.setSv_count(sv_count);
            sv_total = df.getSv_count();

            df.setAlpha(new double[sv_count]);
            sv = new float[sv_count][];

            for( i = k = 0; i < sample_count; i++ )
            {
                if(Math.abs(alpha.get(i)) > 0 )
                {
                    sv[k] = new float[sample_size];
                    sv[k] = converter2floats(samples.get(i));
                    df.getAlpha()[k++] = alpha.get(i);
                }
            }
        }
        else
        {
            int class_count = 9;
            System.out.println(class_count);
            ArrayList<Integer>  sv_tab;
            ArrayList<ArrayList<Float>> temp_samples;
            int[] class_ranges;
            ArrayList<Float> temp_y;

            if( svm_type == C_SVC && params.getClass_weight() != null )
            {
                float[] cw = converter2floats(params.getClass_weight());

                class_weights = converter2list(cw);
                scale(class_weights, params.getC());
            }

            decision_func = new ArrayList<SVMDecisionFunc>(class_count*(class_count-1)/2);
            ArrayList<SVMDecisionFunc> df = new ArrayList<SVMDecisionFunc>();
            for (int l = 0; l < class_count*(class_count-1)/2; l++) {
                df.add(new SVMDecisionFunc());
            }

            sv_tab = new ArrayList<Integer>();

            class_ranges = new int[class_count + 1];
            temp_samples = new ArrayList<ArrayList<Float>>();
            temp_y = new ArrayList<Float>();
            System.out.println(sample_count);
            for (int n = 0; n < sample_count; n++) {
                temp_samples.add(new ArrayList<Float>());
                temp_y.add((float)0.);
            }

            class_ranges[class_count] = 0;
            sortSamplesByClasses( samples, responses, class_ranges, null );
            //check that while cross-validation there were the samples from all the classes
            if( class_ranges[class_count] <= 0 )
                throw new SVMException("While cross-validation one or more of the classes have " +
                        "been fell out of the sample. Try to enlarge <CvSVMParams::k_fold>" );

            if( svm_type == NU_SVC )
            {
                // check if nu is feasible
                for(i = 0; i < class_count; i++ )
                {
                    int ci = class_ranges[i+1] - class_ranges[i];
                    for( j = i+1; j< class_count; j++ )
                    {
                        int cj = class_ranges[j+1] - class_ranges[j];
                        if( params.getNu()*(ci + cj)*0.5 > Math.min( ci, cj ) )
                        {
                            return false; // exit immediately; will release the model and return NULL pointer
                        }
                    }
                }
            }


            // train n*(n-1)/2 classifiers
            for( i = 0; i < class_count; i++ )
            {
                for( j = i+1; j < class_count; j++ )
                {
                    int si = class_ranges[i], ci = class_ranges[i+1] - si;
                    int sj = class_ranges[j], cj = class_ranges[i+1] - sj;
                    double Cp = params.getC(), Cn = Cp;
                    int k1 = 0, sv_count = 0;

                    for( k = 0; k < ci; k++ ) {
                        temp_samples.set(k, samples.get(si + k));
                        temp_y.set(k, (float) 1);
                    }

                    for( k = 0; k < cj; k++ ) {
                        temp_samples.set(ci + k, samples.get(sj + k));
                        temp_y.set(ci + k, (float)-1);
                    }

                    if( class_weights != null && !class_weights.isEmpty()) {
                        Cp = class_weights.get(j);
                        Cn = class_weights.get(j);
                    }

                    System.out.println("hola");
                    if( !train1(ci + cj, var_count, temp_samples, temp_y,
                            Cp, Cn, temp_storage, alpha, df.get(j))) {
                        return false;
                    }

                    for( k = 0; k < ci + cj; k++ ) {
                        if (Math.abs(alpha.get(k)) > 0) {
                            sv_count += Math.abs(alpha.get(k));
                        }
                    }

                    df.get(j).setSv_count(sv_count);

                    df.get(j).setAlpha(new double[sv_count]);;
                    df.get(j).setSv_index(new int[sv_count]);

                    for( k = 0; k < ci; k++ )
                    {
                        if( Math.abs(alpha.get(k)) > 0 )
                        {
                            sv_tab.set(si + k, 1);
                            df.get(j).getSv_index()[k1] = si + k;
                            df.get(j).getAlpha()[k1++] = alpha.get(k);
                        }
                    }

                    for( k = 0; k < cj; k++ )
                    {
                        if( Math.abs(alpha.get(ci + k)) > 0 )
                        {
                            sv_tab.set(sj + k, 1);
                            df.get(j).getSv_index()[k1] = sj + k;
                            df.get(j).getAlpha()[k1++] = alpha.get(ci + k);
                        }
                    }
                }
            }

            // allocate support vectors and initialize sv_tab
            for( i = 0, k = 0; i < sample_count; i++ )
            {
                if( sv_tab.get(i) != null )
                    sv_tab.set(i, ++k);
            }

            sv_total = k;
            sv = new float[sv_total][];

            for( i = 0, k = 0; i < sample_count; i++ )
            {
                if( sv_tab.get(i) != null )
                {
                    sv[k] = new float[sample_size];
                    sv[k] = converter2floats(samples.get(i));
                    k++;
                }
            }

            df = decision_func;

            // set sv pointers
            for( i = 0; i < class_count; i++ )
            {
                for( j = i+1; j < class_count; j++ )
                {
                    for( k = 0; k < df.get(j).getSv_count(); k++ )
                    {
                        df.get(k).getSv_index()[k] = sv_tab.get(df.get(k).getSv_index()[k]-1);
                    }
                }
            }
        }

        optimize_linear_svm();
        ok = true;

        return ok;
    }

    public void optimize_linear_svm()
    {
        // we optimize only linear SVM: compress all the support vectors into one.
        if( params.getKernel_type() != LINEAR )
            return;

        int class_count = class_labels != null ? class_labels.size() :
                params.getSvm_type() == ONE_CLASS ? 1 : 0;

        int i, df_count = class_count > 1 ? class_count*(class_count-1)/2 : 1;
        ArrayList<SVMDecisionFunc> df = decision_func;

        for( i = 0; i < df_count; i++ )
        {
            int sv_count = df.get(i).getSv_count();
            if( sv_count != 1 )
                break;
        }

        // if every decision functions uses a single support vector;
        // it's already compressed. skip it then.
        if( i == df_count )
            return;

        int var_count = get_var_count();
        double[] v = new double[var_count];
        float[][] new_sv = new float [df_count][];

        for( i = 0; i < df_count; i++ )
        {
            new_sv[i] = new float[var_count];
            float[] dst = converter2doubles(v);
            int j, k, sv_count = df.get(i).getSv_count();
            for( j = 0; j < sv_count; j++ )
            {
                float[] src = class_count > 1 && df.get(i).getSv_index() != null ? sv[df.get(i).getSv_index()[j]] : sv[j];
                double a = df.get(i).getAlpha()[j];
                for( k = 0; k < var_count; k++ )
                    v[k] += src[k]*a;
            }
            for( k = 0; k < var_count; k++ )
                dst[k] = (float)v[k];
            df.get(i).setSv_count(1);
            df.get(i).getAlpha()[0] = 1.;
            if( class_count > 1 && df.get(i).getSv_index() != null )
                df.get(i).getSv_index()[0] = i;
        }

        sv = new_sv;
        sv_total = df_count;
    }


    public boolean train( ArrayList<ArrayList<Float>> _train_data, ArrayList<Float> _responses,
                          ArrayList<Float> _var_idx, ArrayList<Float> _sample_idx, SVMParams _params ) throws SVMException {
        boolean ok = false;
        ArrayList<Float> responses = _responses;
        ArrayList<ArrayList<Float>> samples = _train_data;

        int svm_type, sample_count, var_count, sample_size;
        int block_size = 1 << 16;
        ArrayList<Double> alpha;

        clear();
        setParams(_params);

        svm_type = _params.getSvm_type();

        sample_size = samples.size();
        sample_count = samples.size();
        var_count = samples.get(0).size();


        // make the storage block size large enough to fit all
        // the temporary vectors and output support vectors.
        block_size = Math.max(block_size, sample_count);
        block_size = Math.max(block_size, sample_count * 2 + 1024 );
        block_size = Math.max(block_size, sample_size * 2 + 1024);

        storage = new ArrayList<Float>();
        for (int i = 0; i < block_size; i++) {
            storage.add((float)0.);
        }
        alpha = new ArrayList<Double>();
        for (int i = 0; i < sample_count; i++) {
            alpha.add(0.);
        }
        class_labels = new ArrayList<Float>();
        class_labels.add((float) 0);
        class_labels.add((float) 1);
        class_labels.add((float) 2);
        class_labels.add((float) 3);
        class_labels.add((float) 4);
        class_labels.add((float) 5);
        class_labels.add((float) 6);
        class_labels.add((float) 7);
        class_labels.add((float) 8);
        class_labels.add((float) 9);

        createKernel();
        createSolver();

        if( !do_train( svm_type, sample_count, var_count, samples, responses, storage, alpha ))
            return false;

        ok = true; // model has been trained successfully

        solver = new SVMSolver();

        if( !ok )
            clear();

        return ok;
    }

    // privaate methods ***************************************************************************
    private void clear() {
        this.sv_total = 0;
        kernel = null;
        solver = null;
        var_all = 0;
        sv = new float[1][1];
        kernel = new SVMKernel();
        solver = new SVMSolver();
        storage = new ArrayList<Float>();
        class_labels = new ArrayList<Float>(9);
        var_idx = new ArrayList();
        class_weights = new ArrayList();
    }

    private int get_var_count() {
        if (var_idx.size() > 0) {
            return var_idx.size();
        } else {
            return var_all;
        }
    }

    private float[] converter2doubles(double[] doubles) {
        float[] floats = new float[doubles.length];

        for (int i = 0; i < doubles.length; i++) {
            floats[i] = (float)doubles[i];
        }

        return floats;
    }

    private float[] converter2floats(ArrayList<Float> floats) {
        float[] floatsArray = new float[floats.size()];

        for (int i = 0; i < floats.size(); i++) {
            floatsArray[i] = floats.get(i);
        }

        return floatsArray;
    }

    private ArrayList<Float> converter2list(float[] floats) {
        ArrayList<Float> list = new ArrayList<Float>();
        for (int i = 0; i < floats.length; i++) {
            list.set(i, floats[i]);
        }
        return list;
    }

    private void scale(ArrayList<Float> class_weights, double c) {
        for (int i = 0; i < class_weights.size(); i++) {
            class_weights.set(i, class_weights.get(i) + (float)c);
        }
    }

    private void sortSamplesByClasses( ArrayList<ArrayList<Float>> samples, ArrayList<Float> classes,
                                       int[] class_ranges, ArrayList<ArrayList<Float>> mask) throws SVMException {

        ArrayList<SampleResponsePair> pairs;

        int i, k = 0, sample_count;
        if( samples == null || classes == null || class_ranges == null )
            throw new SVMException("INTERNAL ERROR: some of the args are NULL pointers" );
        if( classes.size() <= 1 )
            throw new SVMException("classes array must be a single row of integers" );
        sample_count = classes.size();

        pairs = new ArrayList<SampleResponsePair>();

        for( i = 0; i < sample_count; i++ ) {
            SampleResponsePair srp = new SampleResponsePair();
            srp.setSample(samples.get(i));
            srp.setMask(((mask != null) ? (mask.get(i)) : null));
            srp.setResponse(classes.get(i));
            srp.setIndex(i);
            pairs.add(srp);
        }

        Collections.sort(pairs, new Comparator<SampleResponsePair>() {
            @Override
            public int compare(SampleResponsePair response1, SampleResponsePair response2) {

                if(response1.getResponse() > response2.getResponse()) {
                    return 1;
                } else if(response1.getResponse() < response2.getResponse()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        pairs.get(sample_count-1).setResponse(-1);
        class_ranges[0] = 0;
        for( i = 0; i < sample_count-1; i++ ) {
            samples.set(i, pairs.get(i).getSample());
            if (mask != null)
                mask.set(i, pairs.get(i).getMask());
            classes.set(i, pairs.get(i).getResponse());
            if( pairs.get(i).getResponse() != pairs.get(i+1).getResponse() )
                class_ranges[k++] = i + 1;
        }

    }

}

package com.parallelocr.gustavo.parallelocr.NoParallel;

import com.parallelocr.gustavo.parallelocr.controller.exception.SVMException;
import com.parallelocr.gustavo.parallelocr.model.SVM.MemStorage;
import com.parallelocr.gustavo.parallelocr.model.SVM.ParamGrid;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMDecisionFunc;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernel;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMParams;
import com.parallelocr.gustavo.parallelocr.model.SVM.TermCriteria;

import java.util.ArrayList;

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
    ArrayList<ArrayList<Float>> class_labels;
    ArrayList class_weights;
    ArrayList<SVMDecisionFunc> decision_func;
    MemStorage storage;
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

    // privaate methods ***************************************************************************
    private void clear() {
        this.sv_total = 0;
        kernel = null;
        solver = null;
        var_all = 0;
        sv = new float[1][1];
        kernel = new SVMKernel();
        solver = new SVMSolver();
        storage = new MemStorage();
        class_labels = new ArrayList<ArrayList<Float>>();
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
}

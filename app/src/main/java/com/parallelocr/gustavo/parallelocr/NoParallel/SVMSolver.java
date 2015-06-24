package com.parallelocr.gustavo.parallelocr.NoParallel;

import com.parallelocr.gustavo.parallelocr.controller.exception.SVMException;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernel;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernelRow;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMParams;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMSolutionInfo;

import java.util.ArrayList;

/**
 * Created by gustavo on 26/04/15.
 */
public class SVMSolver {
    public static final double FLT_EPSILON = 1.19209290E-07F;
    private static final int GET_RHO = 0;
    private static final int GET_RHO_NU_SBM = 1;
    private static final int SELECT_SET = 0;
    private static final int SELECT_SET_NU_SBM = 1;
    private static final int GET_ROW_BASE = 0;
    private static final int GET_ROW_SVC = 1;
    private static final int GET_ROW_SVR = 2;
    private static final int GET_ROW_ONE = 3;

    int sample_count;
    int var_count;
    int cache_size;
    int cache_line_size;
    int alpha_count;
    int max_iter;
    int selectCalcRho;
    int selectGetRow;
    int selectWorking;
    ArrayList<ArrayList<Float>> buf;
    double[] C = new double[2];  // C[0] == Cn, C[1] == Cp
    double eps;
    ArrayList<ArrayList<Float>> samples;
    ArrayList<Double> G;
    ArrayList<Double> alpha;
    ArrayList<Float> alpha_status;
    ArrayList<Float> y;
    ArrayList<Double> b;
    SVMParams params;
    ArrayList<Float> storage;
    SVMKernelRow lru_list;
    ArrayList<SVMKernelRow> rows;
    SVMKernel kernel;

    public SVMSolver() {
        this.storage = new ArrayList<Float>();
        this.buf = new ArrayList<ArrayList<Float>>(2);
        lru_list = new SVMKernelRow();
        clear();
    }

    public boolean create(int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                          ArrayList<Float> _y, int _alpha_count, ArrayList<Double> _alpha, double _Cp,
                          double _Cn, ArrayList<Float> _storage, SVMKernel _kernel, int _get_row,
                          int _select_working_set, int _calc_rho) throws SVMException {

        boolean ok;
        int i, svm_type, rows_hdr_size;

        clear();

        sample_count = _sample_count;
        var_count = _var_count;
        samples = _samples;
        y = _y;
        alpha_count = _alpha_count;
        alpha = _alpha;
        kernel = _kernel;

        C[0] = _Cn;
        C[1] = _Cp;

        eps = kernel.getParams().getTerm_crit().getEpsilon();
        max_iter = kernel.getParams().getTerm_crit().getMaxCount();
        //storage = cvCreateChildMemStorage( _storage );

        b = new ArrayList<Double>(alpha_count);
        alpha_status = new ArrayList<Float>(alpha_count);
        G = new ArrayList<Double>(alpha_count);
        for( i = 0; i < 2; i++ )
            buf.set(i, new ArrayList<Float>(sample_count));
        svm_type = kernel.getParams().getSvm_type();

        this.selectWorking = _select_working_set;

        this.selectCalcRho = _calc_rho;

        this.selectGetRow = _get_row;

        cache_line_size = sample_count;
        // cache size = max(num_of_samples^2*sizeof(Qfloat)*0.25, 64Kb)
        // (assuming that for large training sets ~25% of Q matrix is used)
        cache_size = Math.max(cache_line_size * sample_count / 4, (40 << 20));

        // the size of Q matrix row headers
        rows_hdr_size = sample_count;
        if( rows_hdr_size > storage.size() )
            throw new SVMException("Too small storage block size");

        lru_list.setPrev(lru_list);
        lru_list.setNext(lru_list);

        rows = new ArrayList<SVMKernelRow>();
        System.out.println("gfasgs " + rows_hdr_size);
        for (int j = 0; j < rows_hdr_size; j++) {
            rows.add(new SVMKernelRow());
        }
        //ini rows

        ok = true;

        return ok;
    }


    public boolean solveGeneric( SVMSolutionInfo si ) {
        int iter = 0;
        int i, j=0, k;

        // 1. initialize gradient and alpha status
        for( i = 0; i < alpha_count; i++ )
        {
            update_alpha_status(i);
            G.set(i, b.get(i));
            if( Math.abs(G.get(i)) > 1e200 )
                return false;
        }

        for( i = 0; i < alpha_count; i++ )
        {
            if( !is_lower_bound(i) )
            {
                ArrayList<Float> Q_i = getRow(i, buf.get(0));
                double alpha_i = alpha.get(i);

                for( j = 0; j < alpha_count; j++ )
                    G.set(j, G.get(j) + alpha_i * Q_i.get(j));
            }
        }

        // 2. optimization loop
        for(;;)
        {
            ArrayList<Float> Q_i, Q_j;
            double C_i, C_j;
            double old_alpha_i, old_alpha_j, alpha_i, alpha_j;
            double delta_alpha_i, delta_alpha_j;

            for( i = 0; i < alpha_count; i++ )
            {
                if( Math.abs(G.get(i)) > 1e+300 )
                    return false;

                if( Math.abs(alpha.get(i)) > 1e16 )
                    return false;
            }

            if( this.selectWorkingSet(i, j) || iter++ >= max_iter )
                break;

            Q_i = getRow(i, buf.get(0));
            Q_j = getRow(j, buf.get(1));

            C_i = get_C(i);
            C_j = get_C(j);

            alpha_i = old_alpha_i = alpha.get(i);
            alpha_j = old_alpha_j = alpha.get(j);

            if( y.get(i) != y.get(j) )
            {
                double denom = Q_i.get(i) + Q_j.get(j) + 2 * Q_i.get(j);
                double delta = (-G.get(i) - G.get(j)) / Math.max(Math.abs(denom), FLT_EPSILON);
                double diff = alpha_i - alpha_j;
                alpha_i += delta;
                alpha_j += delta;

                if( diff > 0 && alpha_j < 0 )
                {
                    alpha_j = 0;
                    alpha_i = diff;
                }
                else if( diff <= 0 && alpha_i < 0 )
                {
                    alpha_i = 0;
                    alpha_j = -diff;
                }

                if( diff > C_i - C_j && alpha_i > C_i )
                {
                    alpha_i = C_i;
                    alpha_j = C_i - diff;
                }
                else if( diff <= C_i - C_j && alpha_j > C_j )
                {
                    alpha_j = C_j;
                    alpha_i = C_j + diff;
                }
            }
            else
            {
                double denom = Q_i.get(i) + Q_j.get(j) - 2 * Q_i.get(j);
                double delta = (G.get(i) - G.get(j)) / Math.max(Math.abs(denom), FLT_EPSILON);
                double sum = alpha_i + alpha_j;
                alpha_i -= delta;
                alpha_j += delta;

                if( sum > C_i && alpha_i > C_i )
                {
                    alpha_i = C_i;
                    alpha_j = sum - C_i;
                }
                else if( sum <= C_i && alpha_j < 0)
                {
                    alpha_j = 0;
                    alpha_i = sum;
                }

                if( sum > C_j && alpha_j > C_j )
                {
                    alpha_j = C_j;
                    alpha_i = sum - C_j;
                }
                else if( sum <= C_j && alpha_i < 0 )
                {
                    alpha_i = 0;
                    alpha_j = sum;
                }
            }

            // update alpha
            alpha.set(i, alpha_i);
            alpha.set(j, alpha_j);
            update_alpha_status(i);
            update_alpha_status(j);

            // update G
            delta_alpha_i = alpha_i - old_alpha_i;
            delta_alpha_j = alpha_j - old_alpha_j;

            for( k = 0; k < alpha_count; k++ )
                G.set(k, G.get(k) + Q_i.get(k) * delta_alpha_i + Q_j.get(k) * delta_alpha_j);
        }

        // calculate rho
        this.selecCalcRho(si.getRho(), si.getR());

        // calculate objective value
        si.setObj(0);

        for( i = 0; i < alpha_count; i++ )
            si.setObj(si.getObj() + alpha.get(i) * (G.get(i) + b.get(i)));

        si.setObj(si.getObj() * 0.5);

        si.setUpper_bound_p(C[1]);
        si.setUpper_bound_n(C[0]);

        return true;
    }

    private void update_alpha_status(int i) {
        if ((alpha.get(i) >= get_C(i))) {
            alpha_status.set(i, (float)1);
        } else if (alpha.get(i) <= 0) {
            alpha_status.set(i, (float)1);
        } else {
            alpha_status.set(i, (float)0);
        }
    }

    private double get_C(int i) {
        return ( C[((y.get(i) > 0) ? 1 : 0) ] );
    }


    public boolean solveCSvc(int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                             ArrayList<Float> _y, double _Cp, double _Cn, ArrayList<Float> _storage,
                             SVMKernel _kernel, ArrayList<Double> _alpha, SVMSolutionInfo _si ) throws SVMException {
        int i;

        if( !create( _sample_count, _var_count, _samples, _y, _sample_count, _alpha, _Cp, _Cn,
                _storage, _kernel,  this.selectGetRow, this.selectWorking, this.selectCalcRho )) {
            return false;
        }

        for( i = 0; i < sample_count; i++ )
        {
            alpha.set(i, 0.);
            b.set(i, -1.);
        }

        if( !solveGeneric(_si))
            return false;

        for( i = 0; i < sample_count; i++ )
            alpha.set(i, alpha.get(i) * y.get(i));

        return true;
    }


    public boolean solveNuSvc( int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                               ArrayList<Float> _y, ArrayList<Float> _storage, SVMKernel _kernel,
                               ArrayList<Double> _alpha, SVMSolutionInfo _si ) throws SVMException {
        int i;
        double sum_pos, sum_neg, inv_r;

        if( !create( _sample_count, _var_count, _samples, _y, _sample_count, _alpha, 1., 1.,
                _storage, _kernel, this.selectGetRow, this.selectWorking, this.selectCalcRho ))
        return false;

        sum_pos = kernel.getParams().getNu() * sample_count * 0.5;
        sum_neg = kernel.getParams().getNu() * sample_count * 0.5;

        for( i = 0; i < sample_count; i++ )
        {
            if( y.get(i) > 0 )
            {
                alpha.set(i, Math.min(1.0, sum_pos));
                sum_pos -= alpha.get(i);
            }
            else
            {
                alpha.set(i, Math.min(1.0, sum_neg));
                sum_neg -= alpha.get(i);
            }
            b.set(i, 0.);
        }

        if( !solveGeneric( _si ))
            return false;

        inv_r = 1./_si.getR();

        for( i = 0; i < sample_count; i++ )
            alpha.set(i, alpha.get(i) * y.get(i) * inv_r);

        _si.setRho( _si.getRho() * inv_r);
        _si.setObj(_si.getObj() * (inv_r * inv_r));
        _si.setUpper_bound_p(inv_r);
        _si.setUpper_bound_n(inv_r);

        return true;
    }


    public boolean solveOneClass( int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                                  ArrayList<Float> _storage, SVMKernel _kernel, ArrayList<Double> _alpha,
                                  SVMSolutionInfo _si ) throws SVMException {
        int i, n;
        double nu = _kernel.getParams().getNu();

        if( !create( _sample_count, _var_count, _samples, null, _sample_count, _alpha, 1., 1., _storage,
                _kernel, this.selectGetRow, this.selectWorking, this.selectCalcRho ))
            return false;

        y = converter2char(storage);
        //y = (schar*)cvMemStorageAlloc( storage, sample_count*sizeof(y[0]) );
        n = (int)(Math.round( nu*sample_count ));

        for( i = 0; i < sample_count; i++ )
        {
            y.set(i, (float)1);
            b.set(i, 0.);
            alpha.set(i, (i < n ? 1. : 0.));
        }

        if( n < sample_count )
            alpha.set(n, nu * sample_count - n);
        else
            alpha.set(n-1, nu * sample_count - (n-1));

        return solveGeneric(_si);
    }


    public boolean solveEpsSvr( int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                                ArrayList<Float> _y, ArrayList<Float> _storage, SVMKernel _kernel,
                                ArrayList<Double> _alpha, SVMSolutionInfo _si ) throws SVMException {
        int i;
        double p = _kernel.getParams().getP(), kernel_param_c = _kernel.getParams().getC();

        if( !create( _sample_count, _var_count, _samples, null,
                _sample_count*2, null, kernel_param_c, kernel_param_c, _storage, _kernel, this.selectGetRow,
                this.selectWorking, this.selectCalcRho ))
            return false;

        y = converter2char(storage);
        alpha = converter2double(storage);
        // /y = (schar*)cvMemStorageAlloc( storage, sample_count*2*sizeof(y[0]) );
        //alpha = (double*)cvMemStorageAlloc( storage, alpha_count*sizeof(alpha[0]) );

        for( i = 0; i < sample_count; i++ )
        {
            alpha.set(i, 0.);
            b.set(i, p - _y.get(i));
            y.set(i, (float)1);

            alpha.set(i+sample_count, 0.);
            b.set(i+sample_count, p + _y.get(i));
            y.set(i+sample_count, (float)-1);
        }

        if( !solveGeneric(_si))
            return false;

        for( i = 0; i < sample_count; i++ )
            _alpha.set(i, alpha.get(i) - alpha.get(i+sample_count));

        return true;
    }


    public boolean solveNuSvr( int _sample_count, int _var_count, ArrayList<ArrayList<Float>> _samples,
                               ArrayList<Float> _y, ArrayList<Float> _storage, SVMKernel _kernel,
                               ArrayList<Double> _alpha, SVMSolutionInfo _si ) throws SVMException {
        int i;
        double kernel_param_c = _kernel.getParams().getC(), sum;

        if( !create( _sample_count, _var_count, _samples, null, _sample_count*2, null, 1., 1., _storage,
                _kernel, this.selectGetRow, this.selectWorking, this.selectCalcRho ))
            return false;

        y = converter2char(storage);
        alpha = converter2double(storage);
        //y = (schar*)cvMemStorageAlloc( storage, sample_count*2*sizeof(y[0]) );
        //alpha = (double*)cvMemStorageAlloc( storage, alpha_count*sizeof(alpha[0]) );
        sum = kernel_param_c * _kernel.getParams().getNu() * sample_count * 0.5;

        for( i = 0; i < sample_count; i++ )
        {
            alpha.set(i, Math.min(sum, kernel_param_c));
            alpha.set(i + sample_count,  Math.min(sum, kernel_param_c));
            sum -= alpha.get(i);

            b.set(i, (double)-_y.get(i));
            y.set(i, (float) 1);

            b.set(i + sample_count, (double)_y.get(i));
            y.set(i + sample_count, (float) -1);
        }

        if( !solveGeneric(_si))
            return false;

        for( i = 0; i < sample_count; i++ )
            _alpha.set(i, alpha.get(i) - alpha.get(i+sample_count));

        return true;
    }

    public void selecCalcRho(double rho, double r) {
        switch (this.selectCalcRho) {
            case GET_RHO:
                calcRho(rho, r);
                break;
            case GET_RHO_NU_SBM:
                calcRhoNuSvm(rho, r);
                break;
        }
    }

    public void selectWorkinSet(int out_i, int out_j) {
        switch (this.selectWorking) {
            case SELECT_SET:
                selectWorkingSet(out_i, out_i);
                break;
            case SELECT_SET_NU_SBM:
                selectWorkingSetNuSvm(out_i, out_i);
                break;
        }
    }

    public ArrayList<Float> selectGetRow(int i, ArrayList<Float> row, ArrayList<Float> dst, boolean existed) {
        ArrayList<Float> rowFinal = new ArrayList<>();
        switch (this.selectGetRow) {
            case GET_ROW_BASE:
                rowFinal = getRowBase(i, existed);
                break;
            case GET_ROW_SVC:
                rowFinal= getRowSvc(i, row, existed);
                break;
            case GET_ROW_SVR:
                rowFinal = getRowSvr(i, row, dst);
                break;
            case GET_ROW_ONE:
                rowFinal = getRowOneClass(row);
        }
        return rowFinal;
    }

    // private methods ****************************************************************************

    private ArrayList<Float> converter2char(ArrayList<Float> floats) {
        ArrayList<Float> Floats = new ArrayList<Float>();
        for (int i = 0; i < floats.size(); i++) {
            Floats.add(floats.get(i));
        }
        return Floats;
    }

    private ArrayList<Double> converter2double(ArrayList<Float> floats) {
        ArrayList<Double> doubles = new ArrayList<Double>();
        for (int i = 0; i < floats.size(); i++) {
            doubles.add(floats.get(i).doubleValue());
        }
        return doubles;
    }

    private void clear() {
        G = new ArrayList<Double>();
        alpha = new ArrayList<Double>();
        y = new ArrayList<Float>();
        b = new ArrayList<Double>();
        buf.add(new ArrayList<Float>());
        buf.add(new ArrayList<Float>());
        kernel = null;
        rows = new ArrayList<SVMKernelRow>();
        samples = new ArrayList<>();
    }

    private boolean selectWorkingSet(int out_i, int out_j) {
        double Gmax1 = Double.MAX_VALUE;        // max { -grad(f)_i * d | y_i*d = +1 }
        int Gmax1_idx = -1;

        double Gmax2 = Double.MIN_VALUE;        // max { -grad(f)_i * d | y_i*d = -1 }
        int Gmax2_idx = -1;

        int i;

        for(i=0;i<alpha_count;i++)

        {
            double t;

            if (y.get(i) > 0)    // y = +1
            {
                if (!is_upper_bound(i) && (t = -G.get(i)) > Gmax1)  // d = +1
                {
                    Gmax1 = t;
                    Gmax1_idx = i;
                }
                if (!is_lower_bound(i) && (t = G.get(i)) > Gmax2)  // d = -1
                {
                    Gmax2 = t;
                    Gmax2_idx = i;
                }
            } else        // y = -1
            {
                if (!is_upper_bound(i) && (t = -G.get(i)) > Gmax2)  // d = +1
                {
                    Gmax2 = t;
                    Gmax2_idx = i;
                }
                if (!is_lower_bound(i) && (t = G.get(i)) > Gmax1)  // d = -1
                {
                    Gmax1 = t;
                    Gmax1_idx = i;
                }
            }
        }

        out_i=Gmax1_idx;
        out_j=Gmax2_idx;

        return Gmax1+Gmax2 < eps;
    }

    private boolean selectWorkingSetNuSvm(int out_i, int out_j) {
        double Gmax1 = Double.MIN_VALUE;    // max { -grad(f)_i * d | y_i = +1, d = +1 }
        int Gmax1_idx = -1;

        double Gmax2 = Double.MIN_VALUE;    // max { -grad(f)_i * d | y_i = +1, d = -1 }
        int Gmax2_idx = -1;

        double Gmax3 = Double.MIN_VALUE;    // max { -grad(f)_i * d | y_i = -1, d = +1 }
        int Gmax3_idx = -1;

        double Gmax4 = Double.MIN_VALUE;    // max { -grad(f)_i * d | y_i = -1, d = -1 }
        int Gmax4_idx = -1;

        int i;

        for( i = 0; i < alpha_count; i++ )
        {
            double t;

            if( y.get(i) > 0 )    // y == +1
            {
                if( !is_upper_bound(i) && (t = -G.get(i)) > Gmax1 )  // d = +1
                {
                    Gmax1 = t;
                    Gmax1_idx = i;
                }
                if( !is_lower_bound(i) && (t = G.get(i)) > Gmax2 )  // d = -1
                {
                    Gmax2 = t;
                    Gmax2_idx = i;
                }
            }
            else        // y == -1
            {
                if( !is_upper_bound(i) && (t = -G.get(i)) > Gmax3 )  // d = +1
                {
                    Gmax3 = t;
                    Gmax3_idx = i;
                }
                if( !is_lower_bound(i) && (t = G.get(i)) > Gmax4 )  // d = -1
                {
                    Gmax4 = t;
                    Gmax4_idx = i;
                }
            }
        }

        if( Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) < eps )
            return true;

        if( Gmax1 + Gmax2 > Gmax3 + Gmax4 )
        {
            out_i = Gmax1_idx;
            out_j = Gmax2_idx;
        }
        else
        {
            out_i = Gmax3_idx;
            out_j = Gmax4_idx;
        }
        return false;
    }

    private void calcRho(double rho, double r) {
        int i, nr_free = 0;
        double ub = Double.MAX_VALUE, lb = Double.MIN_VALUE, sum_free = 0;

        for( i = 0; i < alpha_count; i++ )
        {
            double yG = y.get(i) * G.get(i);

            if( is_lower_bound(i) )
            {
                if( y.get(i) > 0 )
                    ub = Math.min(ub, yG);
                else
                    lb = Math.max(lb, yG);
            }
            else if( is_upper_bound(i) )
            {
                if( y.get(i) < 0)
                    ub = Math.min(ub, yG);
                else
                    lb = Math.max(lb, yG);
            }
            else
            {
                ++nr_free;
                sum_free += yG;
            }
        }

        rho = nr_free > 0 ? sum_free/nr_free : (ub + lb)*0.5;
        r = 0;
    }

    private void calcRhoNuSvm(double rho, double r) {
        int nr_free1 = 0, nr_free2 = 0;
        double ub1 = Double.MAX_VALUE, ub2 = Double.MAX_VALUE;
        double lb1 = Double.MIN_VALUE, lb2 = Double.MIN_VALUE;
        double sum_free1 = 0, sum_free2 = 0;
        double r1, r2;

        int i;

        for( i = 0; i < alpha_count; i++ )
        {
            double G_i = G.get(i);
            if( y.get(i) > 0 )
            {
                if( is_lower_bound(i) )
                    ub1 = Math.min( ub1, G_i );
                else if( is_upper_bound(i) )
                    lb1 = Math.max( lb1, G_i );
                else
                {
                    ++nr_free1;
                    sum_free1 += G_i;
                }
            }
            else
            {
                if( is_lower_bound(i) )
                    ub2 = Math.min(ub2, G_i );
                else if( is_upper_bound(i) )
                    lb2 = Math.max(lb2, G_i );
                else
                {
                    ++nr_free2;
                    sum_free2 += G_i;
                }
            }
        }

        r1 = nr_free1 > 0 ? sum_free1/nr_free1 : (ub1 + lb1)*0.5;
        r2 = nr_free2 > 0 ? sum_free2/nr_free2 : (ub2 + lb2)*0.5;

        rho = (r1 - r2)*0.5;
        r = (r1 + r2)*0.5;
    }

    private ArrayList<Float> getRowBase(int i, boolean _existed )
    {
        int i1 = i < sample_count ? i : i - sample_count;
        System.out.println(i);
        SVMKernelRow row = rows.get(i1);
        boolean existed = row.getData() != null;
        ArrayList<Float> data;

        if( existed || cache_size <= 0 )
        {
            SVMKernelRow del_row = existed ? row : lru_list.getPrev();
            data = del_row.getData();

            // delete row from the LRU list
            del_row.setData(new ArrayList<Float>());
            del_row.getPrev().setPrev(del_row.getPrev());
            del_row.getNext().setNext(del_row.getNext());
        }
        else
        {
            data = storage;
            cache_size -= cache_line_size;
        }

        // insert row into the LRU list
        row.setData(data);
        row.setPrev(lru_list);
        row.setNext(lru_list.getNext());
        row.getPrev().setNext(row);
        row.getNext().setPrev(row);

        if( !existed )
        {
            kernel.calc(sample_count, var_count, samples, samples.get(i1), row.getData());
        }

        if( _existed )
            _existed = existed;

        return row.getData();
    }


    private ArrayList<Float> getRowSvc( int i, ArrayList<Float> row, boolean existed )
    {
        if( !existed )
        {
            ArrayList<Float> _y = y;
            int j, len = sample_count;

            if( _y.get(i) > 0 )
            {
                for( j = 0; j < len; j++ )
                    row.add(j, _y.get(j) * row.get(j));
            }
            else
            {
                for( j = 0; j < len; j++ )
                    row.add(j, -_y.get(j) * row.get(j));
            }
        }
        return row;
    }


    private ArrayList<Float> getRowOneClass(ArrayList<Float> row) {
        return row;
    }


    private ArrayList<Float> getRowSvr( int i, ArrayList<Float> row, ArrayList<Float> dst){
        int j, len = sample_count;
        ArrayList<Float> dst_pos = dst;
        ArrayList<Float> dst_neg = getNextRow(dst);
        if( i >= len )
        {
            ArrayList<Float> temp = dst_neg;
            dst_neg = dst_pos;
            dst_pos = temp;
        }

        for( j = 0; j < len; j++ )
        {
            float t = row.get(j);
            dst_pos.add(j ,t);
            dst_neg.add(j, -t);
        }
        return dst_pos;
    }

    private ArrayList<Float> getNextRow(ArrayList<Float> dst) {
        for (int i = 0; i < buf.size(); i++) {
            if (!buf.get(i).equals(dst)) {
                return buf.get(i);
            }
        }
        return null;
    }


    private ArrayList<Float> getRow( int i, ArrayList<Float> dst )
    {
        boolean existed = false;
        ArrayList<Float> row = getRowBase( i, existed );
        return selectGetRow( i, row, dst, existed );
    }

    private boolean is_upper_bound(int i) {
        return (alpha_status.get(i) > 0);
    }

    private boolean is_lower_bound(int i) {
        return (alpha_status.get(i)< 0);
    }
}

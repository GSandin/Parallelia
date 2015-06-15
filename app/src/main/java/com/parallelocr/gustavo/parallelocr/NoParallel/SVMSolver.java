package com.parallelocr.gustavo.parallelocr.NoParallel;

import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernel;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMKernelRow;
import com.parallelocr.gustavo.parallelocr.model.SVM.SVMParams;

import java.util.ArrayList;

/**
 * Created by gustavo on 26/04/15.
 */
public class SVMSolver {
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
    float[] buf = new float[2];
    double[] C = new double[2];  // C[0] == Cn, C[1] == Cp
    double eps;
    ArrayList<ArrayList<Float>> samples;
    ArrayList<Double> G;
    ArrayList<Double> alpha;
    ArrayList<Character> alpha_status;
    ArrayList<Character> y;
    ArrayList<Double> b;
    SVMParams params;
    ArrayList<Float> storage;
    SVMKernelRow lru_list;
    ArrayList<SVMKernelRow> rows;
    SVMKernel kernel;

    public SVMSolver() {
        this.storage = new MemStorage();
        clear();
    }

    // private methods ****************************************************************************

    private void clear() {
        G = new ArrayList<Double>();
        alpha = new ArrayList<Double>();
        y = new ArrayList<Character>();
        b = new ArrayList<Double>();
        buf[0] = buf[1] = 0;
        kernel = null;
        rows = new ArrayList<SVMKernelRow>();
        samples = new ArrayList<>();
    }

    private void selectWorkinSet(int out_i, int out_j) {
        switch (this.selectWorking) {
            case SELECT_SET:
                selectWorkingSet(out_i, out_i);
                break;
            case SELECT_SET_NU_SBM:
                selectWorkingSetNuSvm(out_i, out_i);
                break;
        }
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

    private void selecCalcRho(double rho, double r) {
        switch (this.selectCalcRho) {
            case GET_RHO:
                calcRho(rho, r);
                break;
            case GET_RHO_NU_SBM:
                calcRhoNuSvm(rho, r);
                break;
        }
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

    private ArrayList<Float> selectGetRow(int i, ArrayList<Float> row, ArrayList<Float> dst, boolean existed) {
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

    private ArrayList<Float> getRowBase(int i, boolean _existed )
    {
        int i1 = i < sample_count ? i : i - sample_count;
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
            ArrayList<Character> _y = y;
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
        ArrayList<Float> dst_neg = dst + len;
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

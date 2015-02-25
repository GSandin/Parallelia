package com.parallelocr.gustavo.parallelocr.controller.Utils;

import android.graphics.Color;

/**
 * Created by gustavo on 2/02/15.
 */
public class ImgProc {
    public static int[] toGrayscale(int[] in) {
        int [] out = new int[in.length];

        for(int i = 0; i < in.length; i++){
            out[i] = Color.red(in[i]);
        }

        return out;
    }
}

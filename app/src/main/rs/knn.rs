#pragma version(1)
#pragma rs java_package_name(com.parallelia.gustavo.parallelia.Parallel)

rs_allocation test_data;
rs_allocation samples;
rs_allocation tags;

int k;
int len_results;
int len_samples;
int var_count;

float __attribute__((kernel)) knn(int32_t in, uint32_t x, uint32_t y)
{
    int k1 = 0, k2 = 0,a=0;
    //for remove because each gpu compute a test_data
    //for (int s = 0; s < test_data.size(); s++) {
    int test[var_count];
    for(int i=in;i<var_count;i++){
        test[a] = rsGetElementAt_int(test_data,i);
        a++;
    }
    int dd[k];
    float nr[len_results+k*var_count];
    for (int i = 0; i < len_samples; i++) {
        int pixels_train[var_count];
        a = 0;
        for(int j=i;j<var_count;j++){
            pixels_train[a] = rsGetElementAt_int(samples,j);
            a++;
        }
        //KNN_Vector sample = samples.get(i);
        for (int j = 0; j < var_count; j++) {
            int sum = 0, t, ii, ii1;

            for (t = 0; t <= var_count - 4; t += 4) {
                double t0 = test[t] - pixels_train[t], t1 = test[t + 1] - pixels_train[t + 1];
                double t2 = test[t + 2] - pixels_train[t + 2], t3 = test[t + 3] - pixels_train[t + 3];
                sum += t0 * t0 + t1 * t1 + t2 * t2 + t3 * t3;
            }

            for (; t < var_count; t++) {
                double t0 = test[t] - pixels_train[t];
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
                nr[(ii + 1)] = rsGetElementAt_float(tags,i);//pixels_train[pixels_train.length - 1];
            }
            k1 = (k1 + 1) < k ? (k1 + 1) : k;
            k2 = k1 < (k - 1) ? k1 : (k - 1);
        }
    }

    int prev_start = 0, best_count = 0, cur_count;
    float best_val = 0;

    for (int j = k1 - 1; j > 0; j--) {
        bool swap_f1 = false;
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
    return best_val;
    //}
}
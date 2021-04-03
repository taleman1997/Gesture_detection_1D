/**
 * @File:      LEVD(Local Extreme Value Detection)
 * @Author:    Jianing Li (S1997612)
 * @Date:      18032021
 * @Description:    This is the file of LEVD. The function is to
 *                  calculate the static component of the record signal and
 *                  remove it. Just use the dynamic component to calculate
 *                  the distance.
 *                  The code is tested on the smart phone.
 * Test equipment:  1. HUAWEI MAIMANG 6       Android Version: 8.0.0
 *                  2. OPPO R11               Android Version: 8.1.0
 */
package com.example.multithread;

import java.util.ArrayList;
import static java.lang.Math.abs;

public class LEVD {

    /**the input length*/
    int L;

    /**the threshold index*/
    int Q;

    /**the buffer of dynamic vector*/
    public double[] dynamic_vec;

    /**the buffer of static vector*/
    public double[] static_vec;

    /**the mean value of the input signal*/
    double mean;

    /**the standard deviation of the input signal*/
    double std;

    /**the last extreme value of last batch*/
    double previous_last_extreme = 0;

    /**the second last extreme value of last batch*/
    double previous_second_last_extreme = 0;

    /**static vector updated*/
    double static_vec_update;

    /**previous last extreme is max*/
    boolean previous_last_extreme_is_max;

    /**the peak max value buffer*/
    ArrayList<Double> peak_max;

    /**the peak min value buffer*/
    ArrayList<Double> peak_min;

    /**the peak max value location buffer*/
    ArrayList<Integer> peak_max_loc;

    /**the peak min value location buffer*/
    ArrayList<Integer> peak_min_loc;

    /**extreme value buffer*/
    ArrayList<Double> extreme_value;

    /**
     * This is the structure function of the LEVD
     * @param input_length
     * @param threshold_index
     * @param input_mean
     * @param input_std
     */
    public LEVD (int input_length, int threshold_index, double input_mean, double input_std){
        mean = input_mean;
        std = input_std;
        L = input_length;
        Q = threshold_index;
        peak_max = new ArrayList<>();
        peak_min = new ArrayList<>();
        peak_max_loc = new ArrayList<>();
        peak_min_loc = new ArrayList<>();
        extreme_value = new ArrayList<>();
        dynamic_vec = new double[L];
        static_vec = new double[L];
        static_vec[0] = mean;
        static_vec_update = mean;
    }

    /**
     * This method clear the buffer
     */
    public void reset(){
        peak_max.clear();
        peak_min.clear();
        peak_min_loc.clear();
        peak_max_loc.clear();
        extreme_value.clear();
        for (int i = 0; i < L; i++) {
            static_vec[i] = 0;
            dynamic_vec[i] = 0;
        }
        static_vec[0] = static_vec_update;
    }

    /**
     * This method calculate the dynamic component of the signal
     * @param input
     */
    public void calculate_dynamic_vec(double[] input){
        double threshold_value = Q * std;
        boolean input_is_max;
        boolean input_is_min;
        boolean latest_extreme_is_max = false;
        boolean latest_extreme_is_min;

        for (int i = 1; i < L-1; i++) {
            // find the max peaks
            if ((input[i] > input[i - 1]) && (input[i] > input[i + 1])) {
                peak_max.add(input[i]);
                peak_max_loc.add(i);
            }
            // find the min peaks
            else if ((input[i] < input[i - 1]) && (input[i] < input[i + 1])) {
                peak_min.add(input[i]);
                peak_min_loc.add(i);
            }
        }

        //if there is no peaks
        if ((peak_max.isEmpty()) || (peak_min.isEmpty())) {
            for (int i = 1; i < L; i++) {
                static_vec[i] = static_vec[0];
            }
            //System.out.println("no peaks");
        }

        //settle down the first two elements in the extreme value buffer
        else {
            if (static_vec[0] == mean) {
                int first_index = peak_max_loc.get(0) > peak_min_loc.get(0) ? peak_min_loc.get(0) : peak_max_loc.get(0);
                int second_index = peak_max_loc.get(0) < peak_min_loc.get(0) ? peak_min_loc.get(0) : peak_max_loc.get(0);
                extreme_value.add(input[first_index]);
                extreme_value.add(input[second_index]);
            } else {
                extreme_value.add(previous_second_last_extreme);
                extreme_value.add(previous_last_extreme);
            }

            /**
             * Compare input[n] with the last extreme point E[n]
             */
            for (int i = 1; i < L; i++) {
                input_is_max = peak_max_loc.contains(i);
                input_is_min = peak_min_loc.contains(i);

                if (extreme_value.get(extreme_value.size() - 1) == previous_last_extreme) {
                    latest_extreme_is_max = previous_last_extreme_is_max;
                    latest_extreme_is_min = !previous_last_extreme_is_max;
                } else {
                    latest_extreme_is_max = peak_max.contains(extreme_value.get(extreme_value.size() - 1));
                    latest_extreme_is_min = peak_min.contains(extreme_value.get(extreme_value.size() - 1));
                }

                if ((input_is_max) && (latest_extreme_is_max) && (input[i] > extreme_value.get(extreme_value.size() - 1))) {
                    extreme_value.set(extreme_value.size() - 1, input[i]);
                }
                if ((input_is_min) && (latest_extreme_is_min) && (input[i] < extreme_value.get(extreme_value.size() - 1))) {
                    extreme_value.set(extreme_value.size() - 1, input[i]);
                }
                if ((input_is_max) && (latest_extreme_is_min) && (abs(extreme_value.get(extreme_value.size() - 1) - input[i]) > threshold_value)) {
                    extreme_value.add(input[i]);
                }
                if ((input_is_min) && (latest_extreme_is_max) && (abs(extreme_value.get(extreme_value.size() - 1) - input[i]) > threshold_value)) {
                    extreme_value.add(input[i]);
                }

                /**Update the static component estimation using exponential moving average*/
                static_vec[i] = static_vec[i - 1] * 0.9 + 0.05 * (extreme_value.get(extreme_value.size() - 1) + extreme_value.get(extreme_value.size() - 2));
            }

            previous_last_extreme = extreme_value.get(extreme_value.size() - 1);
            previous_second_last_extreme = extreme_value.get(extreme_value.size() - 2);
            static_vec_update = static_vec[L - 1];
            previous_last_extreme_is_max = latest_extreme_is_max;
        }

        /**Calculate the dynamic conponent*/
        for (int i = 0; i < L; i++) {
            dynamic_vec[i] = input[i] - static_vec[i];
        }
    }

}



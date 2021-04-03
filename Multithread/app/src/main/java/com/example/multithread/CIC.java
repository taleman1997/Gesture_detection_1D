/**
 * @File:      CIC
 * @Author:    Jianing Li (S1997612)
 * @Date:      18032021
 * @Description:    This is the file of CIC filter. The function is to
 *                  build the CIC filter to do the decimation and remove
 *                  high frequency component.
 *                  The code is tested on the smart phone.
 * Test equipment:  1. HUAWEI MAIMANG 6       Android Version: 8.0.0
 *                  2. OPPO R11               Android Version: 8.1.0
 */


package com.example.multithread;

import java.util.ArrayList;

/**
 * @Description:    This is the tool class for CIC. This class is only defined
 *                  for 1 section of CIC filter. For 2 or more sections, the
 *                  FIR filter is necessary to flat the frequency response curve.
 * @See:            CIC: https://github.com/EsonJohn/CIC-filter
 */
public class CIC {

    /**the decimation ratio*/
    private int R;

    /**the number of section*/
    private int N;

    /**the differential delay*/
    private int D;

    /**the input length*/
    private int L;

    /**the integrator output*/
    private double intOut;

    /**comb out buffer*/
    public double[] comb_out;

    /**delay buffer*/
    ArrayList<Double> delay_buffer;

    /**
     * This is the method for CIC structure function.
     * The size of comb_out and delay_buffer is defined here.
     * @param decimation_ratio
     * @param section_number
     * @param differeitial_delay
     * @param input_signal_length
     */
    public CIC(int decimation_ratio, int section_number, int differeitial_delay, int input_signal_length){
        R = decimation_ratio;
        N = section_number;
        D = differeitial_delay;
        L = input_signal_length;
        comb_out = new double[L / R];
        for (int i = 0; i < L / R; i++) {
            comb_out[i] = 0;
        }
        delay_buffer = new ArrayList<>(D);
        for (int i = 0; i < D; i++) {
            delay_buffer.add(0.0);
        }
    }

    /**This method set the comb_out as zero*/
    public void reset(){ for (int i = 0; i < L / R; i++) { comb_out[i] = 0; } }


    /**
     * This method is the filter method. Input the signal and output the filtered signal
     * This method does the decimation and filter the high frequency.
     * The transfer function is as below:
     * Y[n] = X[n] + X[n-1] - X[n-D] - X[n-n-D]
     * @param input_sig
     */
    public void filter(double[] input_sig){
        int index = 0;

        for (int i = 0; i < L; i++) {
            //do the integration
            intOut += input_sig[i];

            //decimation and comb
            if ((i+1) % R == 0){
                comb_out[index] = intOut - delay_buffer.get(0);
                index++;
                delay_buffer.remove(0);
                delay_buffer.add(intOut);
            }
        }
    }


}



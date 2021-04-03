/**
 * @File:      QI
 * @Author:    Jianing Li (S1997612)
 * @Date:      18032021
 * @Description:    This is the file of QI. The function is to
 *                  calculate the In-phase and quadrature component
 *                  The code is tested on the smart phone.
 * Test equipment:  1. HUAWEI MAIMANG 6       Android Version: 8.0.0
 *                  2. OPPO R11               Android Version: 8.1.0
 */
package com.example.multithread;

import static java.lang.Math.PI;

public class QI_modulation {

    //the length of the signal
    private int L;
    //the index of the modulation
    public int t;
    //the carrier frequency
    private int fc;
    //sample rate of the recording
    private int sample_rate;

    public double[] Q_buffer;
    public double[] I_buffer;

    public QI_modulation(int signal_length, int carrier_frequency, int sample_frequency){
        L = signal_length;
        fc = carrier_frequency;
        sample_rate = sample_frequency;
        Q_buffer = new double[L];
        I_buffer = new double[L];
    }

    public void reset(){
        for (int i = 0; i < L; i++) {
            Q_buffer[i] = 0;
            I_buffer[i] = 0;
        }
    }


    public void modulation(double[] input_signal){
        assert input_signal.length == L : "input signal length must = L";
        for (int i = 0; i < L; i++) {
            Q_buffer[i] =  input_signal[i] * (-1) * Math.sin(2 * PI * fc * (1.0/sample_rate) * t);
            I_buffer[i] =  input_signal[i] * Math.cos(2 * PI * fc * (1.0/sample_rate) * t);
            t = t + 1;
        }
    }


}




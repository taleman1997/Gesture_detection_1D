/**
 * @File:      Distance
 * @Author:    Jianing Li (S1997612)
 * @Date:      18032021
 * @Description:    This is the file of CDistance. The function is to
 *                  calculate the distance based on the dynamic phasor
 *                  component.
 *                  The code is tested on the smart phone.
 * Test equipment:  1. HUAWEI MAIMANG 6       Android Version: 8.0.0
 *                  2. OPPO R11               Android Version: 8.1.0
 */
package com.example.multithread;

import static java.lang.Math.PI;

public class Distance {

    /**total distance buffer*/
    public double[] total_distance;

    /**phase buffer*/
    public double[] phase;

    /**length of input signal*/
    private int L;

    /**previous distance*/
    private double previous_distance;

    /**wave length*/
    private double wavelength = 0.025;

    /**
     * This method is the structure function of the Distance
     * The size of the buffer is defined here.
     * @param input_length
     */
    public Distance(int input_length){
        L = input_length;
        total_distance = new double[L];
        phase = new double[L];
        previous_distance = 0;
    }

    /**
     * This method reset the buffer
     */
    public void reset(){
        for (int i = 0; i < L; i++) {
            total_distance[i] = 0;
            phase[i] = 0;
        }
        total_distance[0] = previous_distance; // do the update
    }

    /**
     * This method calculate the distance based on QI component
     * For the phase angle cross the quadrant just set to zero
     * @param Q_component (length should equal to L)
     * @param I_component (length should equal to L)
     */
    public void calculate_distance(double[] Q_component, double[] I_component){
        double distance_update = 0;
        double phase_difference = 0;
        for (int i = 0; i < L; i++) {
            phase[i] = Math.atan2(Q_component[i], I_component[i]);
        }
        for (int i = 1; i < L; i++) {
            phase_difference = phase[i] - phase[i-1];
            if ((phase_difference < -PI) || (phase_difference > PI)) { phase_difference = 0; }
            distance_update = -(phase_difference * wavelength) / (4*PI);
            total_distance[i] = total_distance[i-1] + distance_update;
        }
        previous_distance = total_distance[L-1];
    }


}


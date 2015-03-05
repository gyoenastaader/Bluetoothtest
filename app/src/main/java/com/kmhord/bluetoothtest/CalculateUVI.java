package com.kmhord.bluetoothtest;

public class CalculateUVI{


    public static Number[] updatehistory(Number newdata, Number[] storeddata){

         for (int i = 0; i < storeddata.length-1; i++) {
             storeddata[i]=storeddata[i+1];
         }
         storeddata[storeddata.length-1]=newdata;

        return storeddata;
     }

    //This class will require future calibration.
    public Number calcuvi(String newdata){

        double ndata = Double.parseDouble(newdata);
        Number uvi;

        //ADC to UV index conversion, based on data from "See UV on Your Skin"
        uvi=0.04*ndata-12.8;

        //Voltage to UV index conversion, based on data from "See UV on Your Skin"
        //ndata=12.481*ndata-12.348;

        return uvi;
    }


}

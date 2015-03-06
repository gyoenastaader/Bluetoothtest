package com.kmhord.SpitFire;

import java.util.Date;

public class CalculateUVI{


    public static Number[] updatehistory(Number newdata, Number[] storeddata){

         for (int i = 0; i < storeddata.length-1; i++) {
             storeddata[i]=storeddata[i+1];
         }
         storeddata[storeddata.length-1]=  newdata;

        return storeddata;
     }

    //This class will require future calibration.
    public static double UVindex(String newdata){

        double ndata = Double.parseDouble(newdata);
        double uvi;

        //ADC to UV index conversion, based on data from "See UV on Your Skin"
        uvi=0.04*ndata-12.8;

        //Voltage to UV index conversion, based on data from "See UV on Your Skin"
        //ndata=12.481*ndata-12.348;

        return uvi;
    }

    public static long starttime(){

        long StartTime = new Date().getTime()/1000;

        return StartTime;

    }

    public static long endtime(){

        long EndTime = new Date().getTime()/1000;

        return EndTime;

    }

    public static double burnresistance() {

        //SkinType
        // 1 -> 67min / UVI
        // 2 -> 100min / UVI
        // 3 -> 200min / UVI
        // 4 -> 300min / UVI
        // 5 -> 400min / UVI
        // 6 -> 500min / UVI
        // http://www.himaya.com/solar/avoidsunburn.html

        int type = 1;
        int[] skinlimit = {67, 100, 200, 300, 400, 500};
        double SPF = 0.1; //Skin burns 10 times faster, set to 1 for no sunscreen
        double burnresistance;

        burnresistance = skinlimit[type] * SPF;

        return burnresistance;

    }

    public static double remresist(double dUVI, double remresist, long exptime){

        //exptime should be in seconds

        remresist=remresist-dUVI*exptime/60;

        if (remresist < 0){remresist=0;}

        return remresist;

    }
}


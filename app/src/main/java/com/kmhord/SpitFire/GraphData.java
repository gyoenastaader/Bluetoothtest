package com.kmhord.SpitFire;

import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;


import java.text.DecimalFormat;
import java.util.Arrays;


public class GraphData extends MainActivity{


    public void plotxygraph(Number[] storeddata, XYPlot mySimpleXYPlot){

        mySimpleXYPlot.clear();

           // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(storeddata),
                // SimpleXYSeries takes a List so turn our array into a list
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                // Y_VALS_ONLY means use the element index as the x value
                "Series1"); // Set the display title of the series



        // reduce the number of range labels
        mySimpleXYPlot.setTicksPerRangeLabel(3);

        mySimpleXYPlot.setPlotMargins(0, 0, 0, 0);

        //This gets rid of the gray grid
        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);

        //This gets rid of the black border (up to the graph) there is no black border around the labels
        mySimpleXYPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);

        //This gets rid of the black behind the graph
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);

        //Hide the Margin and border
        mySimpleXYPlot.setBorderPaint(null);
        mySimpleXYPlot.setPlotMargins(0, 0, 0, 0);

        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);

        mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);

        mySimpleXYPlot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        // Domain
        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, storeddata.length);
        mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("0"));
        mySimpleXYPlot.setDomainStepValue(1);

        //Range
        mySimpleXYPlot.setRangeBoundaries(0, 11, BoundaryMode.FIXED);
        mySimpleXYPlot.setRangeStepValue(1);
        //mySimpleXYPlot.setRangeStep(XYStepMode.SUBDIVIDE, values.length);
        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0"));

        //Remove legend
        mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getLegendWidget());
        mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getDomainLabelWidget());
        mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getRangeLabelWidget());
        mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getTitleWidget());

        //Remove lines
        mySimpleXYPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);

        // Set the display title of the series

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 0, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                Color.BLACK, new PointLabelFormatter());                            // fill color

        series1Format.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat("##.#");

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });
             // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.rgb(255,102,0), Shader.TileMode.MIRROR));

        series1Format.setFillPaint(lineFill);

        // add a new series' to the xyplot:
        mySimpleXYPlot.addSeries(series1, series1Format);

        mySimpleXYPlot.redraw();

    }

    public void plotpiegraph(PieChart pie, double init_resist, double rem_resist){

        pie.clear();

        Paint paint = new Paint();

        Segment s1;
        Segment s2;

        double pexpose = rem_resist/(init_resist)*100;
        double pleftover=100-pexpose;

        s1 = new Segment("Remaining", pexpose);
        s2 = new Segment("Exposure", pleftover);

        //EmbossMaskFilter emf = new EmbossMaskFilter(
         //       new float[]{1, 1, 1}, 0.4f, 10, 8.2f);

        SegmentFormatter sf1 = new SegmentFormatter();
        sf1.getFillPaint().setColor(Color.GRAY);
        //sf1.getFillPaint().setMaskFilter(emf);

        SegmentFormatter sf2 = new SegmentFormatter();
        sf2.getFillPaint().setColor(Color.RED);
       // sf2.getFillPaint().setMaskFilter(emf);

        pie.addSeries(s1, sf1);
        pie.addSeries(s2, sf2);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);

        pie.redraw();
    }


}

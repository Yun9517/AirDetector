package com.github.mikephil.charting.buffer;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by B00055 on 2018/1/4.
 */

public class NewBarBuffer extends BarBuffer {
    public NewBarBuffer(int size, float groupspace, int dataSetCount, boolean containsStacks) {
        super(size, groupspace, dataSetCount, containsStacks);
    }
    List<Float> YInterval=null;
    public void SetYChartInterval(List<Float> src){
        YInterval=src;
    }
    @Override
    public void feed(IBarDataSet data) {
        super.feed(data);

        float size = data.getEntryCount() * phaseX;
        int dataSetOffset = (mDataSetCount - 1);
        float barSpaceHalf = mBarSpace / 2f;
        float groupSpaceHalf = mGroupSpace / 2f;
        float barWidth = 0.5f;
        for (int i = 0; i < size; i++) {

            BarEntry e = data.getEntryForIndex(i);

            // calculate the x-position, depending on datasetcount
            float x = e.getXIndex() + e.getXIndex() * dataSetOffset + mDataSetIndex
                    + mGroupSpace * e.getXIndex() + groupSpaceHalf;
            float y = e.getVal();
            float [] vals = e.getVals();

            if (!mContainsStacks || vals == null) {

                float left = x - barWidth + barSpaceHalf;
                float right = x + barWidth - barSpaceHalf;
                float bottom, top;
                if (mInverted) {
                    bottom = y >= 0 ? y : 0;
                    top = y <= 0 ? y : 0;
                } else {
                    top = y >= 0 ? y : 0;
                    bottom = y <= 0 ? y : 0;
                }

                // multiply the height of the rect with the phase
                if (top > 0)
                    top *= phaseY;
                else
                    bottom *= phaseY;
                int temp=YInterval.size();

                top=countTop(top,YInterval.get(temp-1));
                addBar(left, top, right, bottom);

            }
        }

        reset();

    }
    private float countTop(float top ,float srcMax){

        float fYChartMax= srcMax;//mAxisLeft.getAxisMaximum();//mChart.getYChartMax();
        List<Float> myInterval=YInterval;
        ArrayList<Float> myDistance=new ArrayList<>();
        Float temp=Float.valueOf(0);//new Float(0);
        for (Float a:myInterval){
            myDistance.add(a-temp);
            temp=a;
        }
        float topTemp=top;
        float Temp2=0;
        for (int i=0,j=-1;i<myInterval.size();i++) {
            float counter=myInterval.get(i);
            float value=myDistance.get(i);
            if (top>=counter){
                topTemp=topTemp-counter;
                Temp2+= value*fYChartMax/(myInterval.size()-1)/value;
                j++;
                if (Float.isNaN(Temp2))
                    Temp2=0;
            }else{

                top=Temp2+(top-myInterval.get(j))*fYChartMax/(myInterval.size()-1)/value;
                break;
            }

        }
        return top;
    }

}

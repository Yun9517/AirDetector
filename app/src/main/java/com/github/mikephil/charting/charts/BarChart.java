package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.BarHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.XAxisRendererBarChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chart that draws bars.
 * 
 * @author Philipp Jahoda
 */
public class BarChart extends BarLineChartBase<BarData> implements BarDataProvider {

	/** flag that enables or disables the highlighting arrow */
	private boolean mDrawHighlightArrow = false;

	/**
	 * if set to true, all values are drawn above their bars, instead of below their top
	 */
	private boolean mDrawValueAboveBar = true;

	/**
	 * if set to true, a grey area is drawn behind each bar that indicates the maximum value
	 */
	private boolean mDrawBarShadow = false;

	private List<Float >mYChartInterval=null;
	public BarChart(Context context) {
		super(context);
	}

	public BarChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BarChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init() {
		super.init();

		mRenderer = new BarChartRenderer(this, mAnimator, mViewPortHandler);
		mXAxisRenderer = new XAxisRendererBarChart(mViewPortHandler, mXAxis, mLeftAxisTransformer, this);

		setHighlighter(new BarHighlighter(this));

		mXAxis.mAxisMinimum = -0.5f;
	}

	@Override
	protected void calcMinMax() {
		super.calcMinMax();

		// increase deltax by 1 because the bars have a width of 1
		mXAxis.mAxisRange += 0.5f;

		// extend xDelta to make space for multiple datasets (if ther are one)
		mXAxis.mAxisRange *= mData.getDataSetCount();

		float groupSpace = mData.getGroupSpace();
		mXAxis.mAxisRange += mData.getXValCount() * groupSpace;
		mXAxis.mAxisMaximum = mXAxis.mAxisRange - mXAxis.mAxisMinimum;
	}

	/**
	 * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch point
	 * inside the BarChart.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public Highlight getHighlightByTouchPoint(float x, float y) {

		if (mData == null) {
			Log.e(LOG_TAG, "Can't select by touch. No data set.");
			return null;
		} else
			return getHighlighter().getHighlight(x, y);
	}

	/**
	 * Returns the bounding box of the specified Entry in the specified DataSet. Returns null if the Entry could not be
	 * found in the charts data.
	 * 
	 * @param e
	 * @return
	 */
	public RectF getBarBounds(BarEntry e) {

		IBarDataSet set = mData.getDataSetForEntry(e);

		if (set == null)
			return null;

		float barspace = set.getBarSpace();
		float y = e.getVal();
		float x = e.getXIndex();

		float barWidth = 0.5f;

		float spaceHalf = barspace / 2f;
		float left = x - barWidth + spaceHalf;
		float right = x + barWidth - spaceHalf;
		float top = y >= 0 ? y : 0;
		float bottom = y <= 0 ? y : 0;
        /*
		float valueLowLimite=220;
		float valueHightLimite=660;
		float maxvalue=mAxisLeft.getAxisMaximum();
		if (top <=valueLowLimite) {
			//top *= 65535/3/220;//65535/3=21845  21845/220=99.29
			top *= mAxisLeft.getAxisMaximum()/3/220;
		}
		else if (top>=valueHightLimite){
			top=(float)(220*mAxisLeft.getAxisMaximum()/3/220+440*49.64+(top-660)*0.33);//21845/440=49.64 21845/64875=0.33
		}
		else{
			top = (float)(220*mAxisLeft.getAxisMaximum()/3/220+(top-220)*49.64);
		}*/
        top=countTop(top);
		RectF bounds = new RectF(left, top, right, bottom);

		getTransformer(set.getAxisDependency()).rectValueToPixel(bounds);

		return bounds;
	}

    private float countTop(float top ){
        float fYChartMax=mAxisLeft.getAxisMaximum();//mChart.getYChartMax();
        List<Float> myInterval=getYChartInterval();
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

	/**
	 * set this to true to draw the highlightning arrow
	 * 
	 * @param enabled
	 */
	public void setDrawHighlightArrow(boolean enabled) {
		mDrawHighlightArrow = enabled;
	}

	/**
	 * returns true if drawing the highlighting arrow is enabled, false if not
	 * 
	 * @return
	 */
	public boolean isDrawHighlightArrowEnabled() {
		return mDrawHighlightArrow;
	}

	/**
	 * If set to true, all values are drawn above their bars, instead of below their top.
	 * 
	 * @param enabled
	 */
	public void setDrawValueAboveBar(boolean enabled) {
		mDrawValueAboveBar = enabled;
	}

	/**
	 * returns true if drawing values above bars is enabled, false if not
	 * 
	 * @return
	 */
	public boolean isDrawValueAboveBarEnabled() {
		return mDrawValueAboveBar;
	}

	/**
	 * If set to true, a grey area is drawn behind each bar that indicates the maximum value. Enabling his will reduce
	 * performance by about 50%.
	 * 
	 * @param enabled
	 */
	public void setDrawBarShadow(boolean enabled) {
		mDrawBarShadow = enabled;
	}

	/**
	 * returns true if drawing shadows (maxvalue) for each bar is enabled, false if not
	 * 
	 * @return
	 */
	public boolean isDrawBarShadowEnabled() {
		return mDrawBarShadow;
	}

	@Override
	public BarData getBarData() {
		return mData;
	}

	/**
	 * Returns the lowest x-index (value on the x-axis) that is still visible on the chart.
	 * 
	 * @return
	 */
	@Override
	public int getLowestVisibleXIndex() {

		float step = mData.getDataSetCount();
		float div = (step <= 1) ? 1 : step + mData.getGroupSpace();

		float[] pts = new float[] { mViewPortHandler.contentLeft(), mViewPortHandler.contentBottom() };

		getTransformer(AxisDependency.LEFT).pixelsToValue(pts);
		return (int) ((pts[0] <= getXChartMin()) ? 0 : (pts[0] / div) + 1);
	}

	/**
	 * Returns the highest x-index (value on the x-axis) that is still visible on the chart.
	 * 
	 * @return
	 */
	@Override
	public int getHighestVisibleXIndex() {

		float step = mData.getDataSetCount();
		float div = (step <= 1) ? 1 : step + mData.getGroupSpace();

		float[] pts = new float[] { mViewPortHandler.contentRight(), mViewPortHandler.contentBottom() };

		getTransformer(AxisDependency.LEFT).pixelsToValue(pts);
		return (int) ((pts[0] >= getXChartMax()) ? getXChartMax() / div : (pts[0] / div));
	}
   public  List<Float> getYChartInterval(){
       Collections.sort(mYChartInterval);
       return mYChartInterval;
    }
    public void setYChartInterval(List <Float > value){
        mYChartInterval=value;
        mYChartInterval.add(getYChartMax());
        mYChartInterval.add(getYChartMin());
    }
}

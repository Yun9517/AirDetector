package microjet.com.airqi2.CustomAPI;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

/**
 * Created by ray650128 on 2017/11/28.
 */

public class MyBarDataSet extends BarDataSet {


    public MyBarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        if(getEntryForXIndex(index).getVal() < 95) // less than 95 green
            return mColors.get(0);
        else if(getEntryForXIndex(index).getVal() < 100) // less than 100 orange
            return mColors.get(1);
        else // greater or equal than 100 red
            return mColors.get(2);
    }

}

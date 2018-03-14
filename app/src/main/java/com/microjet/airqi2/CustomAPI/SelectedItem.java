package com.microjet.airqi2.CustomAPI;

/**
 * Created by ray650128 on 2018/3/14.
 */

public class SelectedItem {
    private static int slectedItem ;
    public final static int TODO = 0;
    public final static int DATEEVENT = 1;


    public static int getSelectedItem() {
        return slectedItem;
    }

    public static void setSelectedItem(int selectedItem) {
        SelectedItem.slectedItem = selectedItem;
    }
}
package com.microjet.airqi2.CustomAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microjet.airqi2.AsmDataModel;
import com.microjet.airqi2.MyApplication;
import com.microjet.airqi2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by ray650128 on 2018/3/14.
 *
 */

public class AirMapAdapter extends RecyclerView.Adapter<AirMapAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<AsmDataModel> mDataset;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener = null;

    // 定義 OnItenClickListener 的介面
    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意這裡使用getTag方法獲取position
            mOnItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }

    // 暴露給外部的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AirMapAdapter(ArrayList<AsmDataModel> myDataset) {
        mDataset = myDataset;
        mContext = MyApplication.Companion.applicationContext();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AirMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mapdata_list, parent, false);
        // set the view's size, margins, paddings and layout parameters

        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(AirMapAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");

        holder.textDate.setText(dateFormat.format(mDataset.get(position).getCreated_time()));

        holder.textLat.setText(
                mContext.getResources().getString(R.string.text_label_latitude) +
                        mDataset.get(position).getLatitude().toString());

        holder.textLng.setText(
                mContext.getResources().getString(R.string.text_label_longitude) +
                        mDataset.get(position).getLongitude().toString());

        holder.textTVOC.setText(mDataset.get(position).getTVOCValue() + " ppb");
        holder.textPM25.setText(mDataset.get(position).getPM25Value() + " μm");
        holder.textECO2.setText(mDataset.get(position).getECO2Value() + " ppm");
        holder.textTEMP.setText(mDataset.get(position).getTEMPValue() + " °C");
        holder.textHUMI.setText(mDataset.get(position).getHUMIValue() + " %");

        // 將position保存在itemView的Tag中，以便點擊時進行獲取
        holder.itemView.setTag(position);

        // 設置選中狀態
        if (position == SelectedItem.getSelectedItem()) {
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.recyclerview_selected));

            holder.textDate.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textLat.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textLng.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textTVOC.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textPM25.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textECO2.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textTEMP.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textHUMI.setTextColor(mContext.getResources().getColor(R.color.whiteColor));

            holder.textTVOCtitle.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textPM25title.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textECO2title.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textTEMPtitle.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.textHUMItitle.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
        } else {
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.recyclerview_not_select));

            holder.textDate.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textLat.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textLng.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textTVOC.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textPM25.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textECO2.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textTEMP.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textHUMI.setTextColor(mContext.getResources().getColor(R.color.blackColor));

            holder.textTVOCtitle.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textPM25title.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textECO2title.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textTEMPtitle.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.textHUMItitle.setTextColor(mContext.getResources().getColor(R.color.blackColor));
        }

        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView textDate;
        TextView textLat;
        TextView textLng;
        TextView textTVOC;
        TextView textPM25;
        TextView textECO2;
        TextView textTEMP;
        TextView textHUMI;
        TextView textTVOCtitle;
        TextView textPM25title;
        TextView textECO2title;
        TextView textTEMPtitle;
        TextView textHUMItitle;

        ViewHolder(View v) {
            super(v);
            textDate = v.findViewById(R.id.textDate);
            textLat = v.findViewById(R.id.textLat);
            textLng = v.findViewById(R.id.textLng);
            textTVOC = v.findViewById(R.id.textTVOCvalue);
            textPM25 = v.findViewById(R.id.textPM25value);
            textECO2 = v.findViewById(R.id.textECO2value);
            textTEMP = v.findViewById(R.id.textTEMPvalue);
            textHUMI = v.findViewById(R.id.textHUMIvalue);
            textTVOCtitle = v.findViewById(R.id.textTVOCtitle);
            textPM25title = v.findViewById(R.id.textPM25title);
            textECO2title = v.findViewById(R.id.textECO2title);
            textTEMPtitle = v.findViewById(R.id.textTEMPtitle);
            textHUMItitle = v.findViewById(R.id.textHUMItitle);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

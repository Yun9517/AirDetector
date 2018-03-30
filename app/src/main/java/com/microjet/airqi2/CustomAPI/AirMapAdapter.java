package com.microjet.airqi2.CustomAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.microjet.airqi2.AsmDataModel;
import com.microjet.airqi2.MyApplication;
import com.microjet.airqi2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ray650128 on 2018/3/14.
 */

public class AirMapAdapter extends RecyclerView.Adapter<AirMapAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<AsmDataModel> mDataset;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener = null;

    // 定義 OnItenClickListener 的介面
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意這裡使用getTag方法獲取position
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        //Calendar calendar = Calendar.getInstance();
        long nowTime = ((mDataset.get(position).getCreated_time()));// - calendar.getTimeZone().getRawOffset());

        holder.textDate.setText(String.valueOf(dateFormat.format(nowTime)));

        // 將position保存在itemView的Tag中，以便點擊時進行獲取
        holder.itemView.setTag(position);

        // 設置選中狀態
        if (position == SelectedItem.getSelectedItem()) {
            holder.textDate.setBackground(mContext.getResources().getDrawable(R.drawable.recyclerview_selected));
            holder.textDate.setTextColor(mContext.getResources().getColor(R.color.whiteColor));
            holder.imgPointer.setImageResource(R.drawable.img_pointer_selected);
        } else {
            holder.textDate.setBackground(mContext.getResources().getDrawable(R.drawable.recyclerview_not_select));
            holder.textDate.setTextColor(mContext.getResources().getColor(R.color.blackColor));
            holder.imgPointer.setImageResource(R.drawable.img_pointer_unselect);
        }

        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView textDate;
        ImageView imgPointer;

        ViewHolder(View v) {
            super(v);
            textDate = v.findViewById(R.id.textDate);
            imgPointer = v.findViewById(R.id.imgPointer);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

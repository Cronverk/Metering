package com.example.grass.metering.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.grass.metering.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Grass on 29.04.2016.
 */
public class ItemAdapter extends BaseAdapter {
    Context context;
    List<Item> list;
    LayoutInflater inflater;

    public ItemAdapter(Context context, ArrayList<Item> arrayList) {
        // TODO Auto-generated constructor stub
        this.list = arrayList;
        this.context=context;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View viewItem;
        Item item = list.get(position);
        viewItem = inflater.inflate(R.layout.item, null);
               ((TextView) viewItem.findViewById(R.id.text2)).setText(doubleToDegree(item.getAngle()));
        ((TextView) viewItem.findViewById(R.id.text3)).setText(""+item.getMeger()+" м");
        ((TextView) viewItem.findViewById(R.id.text4)).setText(""+item.getuMerge()+" м");
        ((TextView) viewItem.findViewById(R.id.text5)).setText(""+roundNumber(item.getPercent(),2)+" %");
        if(position%2==0)
            viewItem.setBackgroundColor(0xFFECF0F1);
        return viewItem;
    }

    public static String doubleToDegree(double value){
        int degree = (int) value;
        double rawMinute = Math.abs((value % 1) * 60);
        int minute = (int) rawMinute;
        return String.format("%d° %d′ ", degree,minute);
    }
    public double roundNumber(double number, double accurancy) {
        accurancy = Math.pow(10, accurancy);
        number = Math.round(number * accurancy);
        return number / accurancy;
    }
}

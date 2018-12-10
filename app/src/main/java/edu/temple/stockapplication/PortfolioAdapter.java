package edu.temple.stockapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PortfolioAdapter extends BaseAdapter {

    private Context context;
    private JSONArray jsonArray;
    private String oldprice;
    private String newprice;


    public PortfolioAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.jsonArray = jsonArray;
    }

//    public PortfolioAdapter(Context context, JSONArray jsonArray, String oldprice, String newprice) {
//        this.context = context;
//        this.jsonArray = jsonArray;
//        this.oldprice = oldprice;
//        this.newprice = newprice;
//    }


    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public Object getItem(int i) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = jsonArray.getJSONObject(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        try {
            textView.setText(jsonArray.getJSONObject(i).getString("Symbol"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        if ((oldprice != null) & (newprice != null))
//        {
//
//            if (Integer.parseInt(newprice) - Integer.parseInt(oldprice) > 0)
//                textView.setBackgroundColor(Color.GREEN);
//            else
//                textView.setBackgroundColor(Color.RED);
//        }

        return textView;
    }


    public void updateJSONArray(JSONArray jsonArray){
        this.jsonArray = jsonArray;
    }

}

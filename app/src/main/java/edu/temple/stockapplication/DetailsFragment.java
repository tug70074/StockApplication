package edu.temple.stockapplication;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    TextView companyName;
    TextView stockPrice;
    WebView stockImage;
    View view;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_details, container, false);
        companyName = view.findViewById(R.id.companyName);
        stockPrice = view.findViewById(R.id.currentStockPrice);
        stockImage = view.findViewById(R.id.chart);
        stockImage.getSettings().setJavaScriptEnabled(true);

        Bundle args = getArguments();
        if (args != null){
            companyName.setText(args.getString("companyName"));
            String price = getString(R.string.price);
            stockPrice.setText(price.concat(" $").concat(args.getString("stockPrice")));
            getimage(args.getString("symbol"));
        }
        return view;
    }

    public void getimage(final String symbol){
        Thread thread = new Thread(){
            @Override
            public void run(){
                URL url;
                try{
                    url = new URL("https://macc.io/lab/cis3515/?symbol=" + symbol); // build url


                    //send the object to a Handler
                    Message msg = Message.obtain();
                    msg.obj = symbol;
                    imageResponseHandler.sendMessage(msg);

                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    Handler imageResponseHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String symbol =  (String) msg.obj;
            stockImage.loadUrl("https://macc.io/lab/cis3515/?symbol=" + symbol);
            return false;
        }
    });

}

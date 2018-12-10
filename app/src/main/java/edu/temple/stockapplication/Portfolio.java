package edu.temple.stockapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Portfolio extends AppCompatActivity implements PortfolioFragment.OnStockSelectedListener{


    FragmentManager fm;
    PortfolioFragment portfolioFragment;
    FloatingActionButton floatingActionButton;
    MyService myService;

    File file;
    String fileName = "my_file.json";
    boolean twopane;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        floatingActionButton = findViewById(R.id.fab);

        file = new File(getFilesDir(), fileName);


        portfolioFragment = new PortfolioFragment();

        try {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.fragmentcontainer, portfolioFragment).commit();
        }
//        fm.executePendingTransactions();
            catch(Exception e){
            e.printStackTrace();
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Portfolio.this, StockAdd.class);
                startActivityForResult(i, 1);


            }
        });





    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                final String symbol =data.getStringExtra("SYMBOL");

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            URL url;
                            try {
                                url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                                String response = "", tmpResponse;
                                tmpResponse = bufferedReader.readLine();
                                while (tmpResponse != null ) { //keep reading until null
                                    response = response + tmpResponse;
                                    tmpResponse = bufferedReader.readLine();
                                }
                                JSONObject stockObject = new JSONObject(response);

                                Message msg = Message.obtain();
                                msg.obj = stockObject;
                                stockResponseHandler.sendMessage(msg); //sends to Handler to see if it is a valid stock string

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();


            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(Portfolio.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    Handler stockResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            //receives message from thresad
            JSONObject responseObject = (JSONObject) msg.obj;
            if (!responseObject.has("Name")){ // if the message in handleMessage has no Name, toast pops up
                Toast.makeText(Portfolio.this, "Not a Valid Stock", Toast.LENGTH_SHORT).show();
                return false;
            }


            JSONArray jsonArray = null;

            // if array json exists in file already, go to end and make new array
            if (file.exists()){
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    StringBuilder text = new StringBuilder();
                    String line;
                    while (((line = bufferedReader.readLine()) != null)) {
                        text.append(line);
                        text.append('\n');
                    }
                    bufferedReader.close();
                    jsonArray = new JSONArray(text.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else{ // else make a new array at end
                jsonArray = new JSONArray();
            }

            jsonArray.put(responseObject);

            //writes json array to the end of file
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(jsonArray.toString().getBytes());
                fileOutputStream.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            //updates fragment with new json array so new stock on the list
            portfolioFragment.portfolioAdapter.updateJSONArray(jsonArray);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    portfolioFragment.textView.setVisibility(View.GONE);
                    portfolioFragment.portfolioAdapter.notifyDataSetChanged();

                }
            });
            return false;

        }
    });
    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.TestBinder binder = (MyService.TestBinder) service;
            myService = binder.getService();
            myService.doSomething(ServiceHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    Handler ServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            DetailsFragment stockDetailsFragment;
            if ((stockDetailsFragment = (DetailsFragment) fm.findFragmentByTag("Details")) != null){
                JSONArray jsonArray = (JSONArray) msg.obj;
                try {
                    String oldPrice = stockDetailsFragment.stockPrice.getText().toString();
                    String newPrice = getString(R.string.price).concat(jsonArray.getJSONObject(position).getString("LastPrice"));
                    stockDetailsFragment.stockPrice.setText(newPrice);

//                    Bundle bundle = new Bundle();
//                    bundle.putString("oldPrice", oldPrice);
//                    bundle.putString("newPrice", newPrice);
//                    portfolioFragment.portfolioAdapter.updateJSONArray();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    });



    @Override
    public void OnStockSelectedListener(int position) {
        this.position = position;
        JSONArray jsonArray = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            bufferedReader.close();
            jsonArray = new JSONArray(text.toString());

            DetailsFragment stockDetailsFragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putString("companyName", jsonArray.getJSONObject(position).getString("Name"));
            args.putString("stockPrice", jsonArray.getJSONObject(position).getString("LastPrice"));
            args.putString("symbol", jsonArray.getJSONObject(position).getString("Symbol"));
            stockDetailsFragment.setArguments(args);

           FragmentManager fm = getSupportFragmentManager();


            if (findViewById(R.id.fragmentcontainer2) != null){
                fm.beginTransaction().replace(R.id.fragmentcontainer2, stockDetailsFragment, "Details").commit();
            } else{
                fm.beginTransaction().replace(R.id.fragmentcontainer, stockDetailsFragment, "Details").addToBackStack(null).commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

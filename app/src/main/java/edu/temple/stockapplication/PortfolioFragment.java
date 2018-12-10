package edu.temple.stockapplication;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {


    OnStockSelectedListener mCallback;
    PortfolioAdapter portfolioAdapter;
    ListView listView;
    TextView textView;
    String oldprice;
    String newprice;

    public PortfolioFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);

        listView = v.findViewById(R.id.StockList);
        textView = v.findViewById(R.id.introText);

        //read json file again and set the listview to the adapter
        File file = new File(getActivity().getFilesDir(), "my_file.json");
        if (file.exists()){
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                bufferedReader.close();
                portfolioAdapter = new PortfolioAdapter(getContext(), new JSONArray(text.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            portfolioAdapter = new PortfolioAdapter(getContext(), new JSONArray());
        }


        listView.setAdapter(portfolioAdapter);

        textView = v.findViewById(R.id.introText);
        if (portfolioAdapter.getCount() > 0){  // if there is a JSON object, erase text view
            textView.setVisibility(View.GONE);
        } else{
            textView.setText("Enter a Stock");
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> ParentView, View view, int position, long id) {
                mCallback.OnStockSelectedListener(position);
            }
        });



        return v;
    }

    public interface OnStockSelectedListener{ //uses interface to pull up details of fragment
        void OnStockSelectedListener(int position);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try{
            mCallback = (OnStockSelectedListener) activity;
        } catch(ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallback = null;
    }

}

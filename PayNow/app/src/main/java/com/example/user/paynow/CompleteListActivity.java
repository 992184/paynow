package com.example.user.paynow;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.paynow.underground.InsertData;
import com.example.user.paynow.underground.NetworkData;
import com.example.user.paynow.underground.ProductData;
import com.example.user.paynow.underground.RecycleViewAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CompleteListActivity extends AppCompatActivity {

    NetworkData networkData = new NetworkData();
    String IP_ADDRESS = networkData.IP_ADDRESS;
    String TAG = networkData.TAG;

    private EditText mEditTextName;
    private EditText mEditTextCountry;
    //private TextView mTextViewResult;
    private ArrayList<ProductData> mArrayList;
    private RecycleViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mJsonString;
    private Button payCancelBtn;
    Button confirmBtn;

    Activity currentActivity = this;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_list);
        try{
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        /*결제취소버튼임*/
        context = this;
        payCancelBtn = (Button) findViewById(R.id.button_complete_all);


        //mTextViewResult = (TextView)findViewById(R.id.textView_shoppinglist_result);
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_complete_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //mTextViewResult.setMovementMethod(new ScrollingMovementMethod());


        mArrayList = new ArrayList<>();

        mAdapter = new RecycleViewAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);


        mArrayList.clear();
        mAdapter.notifyDataSetChanged();
        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/getReciptJSON.php", "");
        confirmBtn = (Button) findViewById(R.id.admit_shoppingBtn); //결제취소버튼
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(currentActivity).initiateScan();
            }
        });

        //결제취소버튼
        payCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               cancelPay();
            }
        });
    }

    public void cancelPay(){
        final int totalPrice = mAdapter.getTotalPrice();    //현재 토탈 가져와서

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("구매취소");
        builder.setMessage("리스트의 상품이 일괄적으로 환불됩니다.\n 정말로 환불요청을 하시겠습니까?");
        builder.setPositiveButton("전체 취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GetData getData1 = new GetData();
                        getData1.execute( "http://" + IP_ADDRESS + "/emptyRecipt", "");
                        mAdapter.notifyDataSetChanged();
                        //mRecyclerView.notifyAll();
                        dialog.dismiss();

                    }
                });
        builder.setNegativeButton("구매를 취소하지 않겠습니다",
                new DialogInterface.OnClickListener() {         //다이얼로그 닫기
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    //get result of scan
    //test complete
    @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {      //if qr doesn't exist
                Toast.makeText(CompleteListActivity.this, "직원 코드를 다시 인식해주세요", Toast.LENGTH_LONG).show();
            } else {                                //if qr exist
                String barcodeString = (String) result.getContents();   //스캔한 product code
                if(barcodeString.equals("GOOAH") || barcodeString.equals("YUNJU")) {
                    Toast.makeText(CompleteListActivity.this, "구매가 최종적으로 확인되었습니다.", Toast.LENGTH_LONG).show();
                    confirmBtn.setText("승인완료");
                    confirmBtn.setClickable(false);
                    confirmBtn.setEnabled(false);
                    payCancelBtn.setText("환불은 고객센터로 문의해주세요.");
                    payCancelBtn.setClickable(false);
                    payCancelBtn.setEnabled(false);
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CompleteListActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){
                // mTextViewResult.setText(errorString);
            }
            else {
                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String postParameters = params[1];

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {
                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    //JSON을 파싱해서 보여준다
    private void showResult(){

        String TAG_JSON="root";
        String TAG_CODE = "CODE";
        String TAG_NAME = "NAME";
        String TAG_PRICE ="PRICE";

        int currentPrice = 0;
        int totalPrice = 0;

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_CODE);
                String name = item.getString(TAG_NAME);
                String price = item.getString(TAG_PRICE);

                ProductData productData = new ProductData();

                productData.setMember_code(id);
                productData.setProduct_name(name);
                productData.setMember_price(price);

                currentPrice = Integer.parseInt(price);
                totalPrice += currentPrice;

                mArrayList.add(productData);
                mAdapter.notifyDataSetChanged();
            }


        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}
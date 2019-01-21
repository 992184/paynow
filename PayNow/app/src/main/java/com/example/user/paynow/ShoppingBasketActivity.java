package com.example.user.paynow;


import android.app.ActionBar;
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

import com.example.user.paynow.underground.InsertData;
import com.example.user.paynow.underground.NetworkData;
import com.example.user.paynow.underground.ProductData;
import com.example.user.paynow.underground.RecycleViewAdapter;

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

public class ShoppingBasketActivity extends AppCompatActivity {

    NetworkData networkData = new NetworkData();
    String IP_ADDRESS = networkData.IP_ADDRESS;
    String TAG = networkData.TAG;

    Context context = this;

    private EditText mEditTextName;
    private EditText mEditTextCountry;
    //private TextView mTextViewResult;
    private ArrayList<ProductData> mArrayList;
    private RecycleViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private EditText mEditTextSearchKeyword;
    private String mJsonString;
    public TextView mTotalText;
    private Button paymentShoppingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_basket);
        try{
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }


        paymentShoppingBtn = (Button) findViewById(R.id.payment_shoppingBtn);

        //mTextViewResult = (TextView)findViewById(R.id.textView_shoppinglist_result);
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_shopping_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //mTextViewResult.setMovementMethod(new ScrollingMovementMethod());


        mArrayList = new ArrayList<>();

        mAdapter = new RecycleViewAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);


        mArrayList.clear();
        mAdapter.notifyDataSetChanged();
        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/getShoppingListJSON.php", "");

        //결제 버튼 onClickAction
        paymentShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Integer> mSelectArray = mAdapter.getSelectList(); //선택된 position들이 있음
                showAlert();    //토탈 보여주고 결제 할 건지 말건지
            }
        });
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ShoppingBasketActivity.this,
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
        int total = 0;

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);


            //리스트 세팅하면서 total 출력
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
                //total += currentPrice;    //여기서 토탈 계산해주면 아이템 삭제 시 반응 불가

                mArrayList.add(productData);
                mAdapter.notifyDataSetChanged();
            }

          // total_price = total;

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    //total까지 완벽하게 작동함
    public void showAlert(){
        final int totalPrice = mAdapter.getTotalPrice();    //현재 토탈 가져와서
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("구매 확인");
        builder.setMessage("총" + totalPrice + "원");
        builder.setPositiveButton("결제",
                new DialogInterface.OnClickListener() {         //결제 누르면 결제창으로 넘어감
                    public void onClick(DialogInterface dialog, int which) {
                        //장바구니 테이블 구매목록으로 옮기는 코드
                        //copyTableandDrop();
                        showChoosePay();    //결제 선택창 고르고
                        dialog.dismiss();   //끄기
                    }
                });
        builder.setNegativeButton("장바구니로",
                new DialogInterface.OnClickListener() {         //다이얼로그 닫기
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void showChoosePay(){

        final int totalPrice = mAdapter.getTotalPrice();    //현재 토탈 가져와서

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("결제방식선택");
        builder.setMessage("결제방식을 선택해주세요");
        builder.setPositiveButton("핸드폰 결제",
                new DialogInterface.OnClickListener() {         //결제 누르면 결제창으로 넘어감
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(ShoppingBasketActivity.this, PayActivity.class);
                        intent.putExtra("total", totalPrice);
                        intent.putExtra("payment", "danal");
                        startActivity(intent);
                        dialog.dismiss();

                    }
                });
        builder.setNegativeButton("카카오페이",
                new DialogInterface.OnClickListener() {         //다이얼로그 닫기
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ShoppingBasketActivity.this, PayActivity.class);
                        intent.putExtra("total", totalPrice);
                        intent.putExtra("payment", "kakao");
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    //장바구니 테이블 구매목록으로 옮기고 드랍함
    public void copyTableandDrop(){
        //일단 쇼핑리스트에 남아있는 애들 싹 받아다가 지워버리고 구매목록으로 옮겨버려야 함
        /*shopping table을 recipt list로 copy하고 싹 밀어버리는 코드*/
        //String query = "INSERT INTO recipt SELECT * FROM shoppinglist";
        InsertData task = new InsertData(context);
        task.execute("http://" + IP_ADDRESS + "/shoppingToRecipt.php", ""); //param을 넘겨줘야 함->insertData가 param을 받기 때문
        Log.d("DB", "shoppingList -> Recipt");
    }
}
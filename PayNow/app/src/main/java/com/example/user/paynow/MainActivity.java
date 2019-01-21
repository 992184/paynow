package com.example.user.paynow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.user.paynow.underground.InsertData;
import com.example.user.paynow.underground.NetworkData;
import com.example.user.paynow.underground.ProductData;
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

public class MainActivity extends AppCompatActivity {

    NetworkData networkData = new NetworkData();
    String IP_ADDRESS = networkData.IP_ADDRESS;
    String TAG = networkData.TAG;

    Activity currentActivity = this;
    FloatingActionButton fab;
    ImageButton shoppingListBtn;
    ImageButton completeListBtn;
    ImageButton mainPayBtn;

    String barcodeString;
    Context context;

    private ArrayList<ProductData> mArrayList;
    private String mJsonString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        context = this;
        mArrayList = new ArrayList<>();
        mArrayList.clear();

        shoppingListBtn = (ImageButton) findViewById(R.id.shoppingListBtn);
        completeListBtn = (ImageButton) findViewById(R.id.completeListBtn);
        mainPayBtn = (ImageButton) findViewById(R.id.mainPayBtn);

        //fab에 바코드 스캔 연결
        fab = (FloatingActionButton) findViewById(R.id.barcodeFAB);

        //onClicklistener
        Button.OnClickListener onClickListener = new Button.OnClickListener(){
            @Override
            public void onClick(View view){

                Intent intent;

                switch(view.getId()){
                    case R.id.barcodeFAB:           //oepn ZXing barcode Scanner Library
                        new IntentIntegrator(currentActivity).initiateScan();
                        break;
                    case R.id.shoppingListBtn:  //go Shopping List Activity
                        intent = new Intent(MainActivity.this, ShoppingBasketActivity.class);
                        //intent.putExtra(“text”,String.valueOf(editText.getText()));   //넘겨 줄 데이터 이름과 내용
                        startActivity(intent);
                        break;
                    case R.id.completeListBtn:  //go Complete List Activity
                        intent = new Intent(MainActivity.this, CompleteListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.mainPayBtn:
                        showChoosePay();
                        break;
                    default:
                        break;
                }
            }
        };

        //connect btn with onClickListener
        fab.setOnClickListener(onClickListener);
        shoppingListBtn.setOnClickListener(onClickListener);
        completeListBtn.setOnClickListener(onClickListener);
        mainPayBtn.setOnClickListener(onClickListener);
    }

    public void showChoosePay(){

        final int totalPrice = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("결제방식선택");
        builder.setMessage("총 "+ totalPrice + "원\n결제방식을 선택해주세요");
        builder.setPositiveButton("핸드폰 결제",
                new DialogInterface.OnClickListener() {         //결제 누르면 결제창으로 넘어감
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(MainActivity.this, PayActivity.class);
                        intent.putExtra("total", totalPrice);
                        intent.putExtra("payment", "danal");
                        startActivity(intent);
                        dialog.dismiss();

                    }
                });
        builder.setNegativeButton("카카오페이",
                new DialogInterface.OnClickListener() {         //다이얼로그 닫기
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, PayActivity.class);
                        intent.putExtra("total", totalPrice);
                        intent.putExtra("payment", "kakao");
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
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
            }

            // total_price = total;

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }


    //get result of scan
    //test complete
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {      //if qr doesn't exist
                Toast.makeText(MainActivity.this, "상품을 다시 인식해주세요", Toast.LENGTH_LONG).show();
            } else {                                //if qr exist
                barcodeString = (String) result.getContents();   //스캔한 product code
                GetData task = new GetData();
                task.execute( "http://" + IP_ADDRESS + "/getProductName.php", barcodeString);
                Toast.makeText(MainActivity.this, "상품이 인식되었습니다.", Toast.LENGTH_LONG).show();

                /*상품구매여부 ALERT DIALOG*/

               show(barcodeString);

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //code로 name이랑 price 찾기
    /*
    public ProductData findProduct(String barcodeString){

       // GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/getProductName.php", barcodeString);

        Log.d("FIND_PRODUCT", barcodeString);
        if(mArrayList != null){

            Log.d("FIND_PRODUCT", mArrayList.get(0).getProduct_name());
            Log.d("FIND_PRODUCT", mArrayList.get(0).getProduct_price());
            return mArrayList.get(0);
        }else{
            return null;
        }
    }*/

    //alert dialog띄우는 부분
    public void show(final String barcodeString) {
        //ProductData searchResult = findProduct(barcodeString);  //코드값 기반으로 이름하고 가격 검색해서
        String result = null;

        Log.d("SHOW", "start for()");
            for(ProductData i :mArrayList){
                result = i.getProduct_name();
                Log.d("SHOW", i.getProduct_price());
            }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("상품을 구매하시겠습니까?");
        builder.setMessage(result);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {         //제대로 돌아가는 거 확인
                    public void onClick(DialogInterface dialog, int which) {

                        InsertData insertTask = new InsertData(context);
                        insertTask.execute("http://" + IP_ADDRESS + "/insertShoppingList.php", barcodeString);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "장바구니에 추가되었습니다.", Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {         //
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        new IntentIntegrator(currentActivity).initiateScan();

                        Toast.makeText(getApplicationContext(), "아니오를 선택했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }


}

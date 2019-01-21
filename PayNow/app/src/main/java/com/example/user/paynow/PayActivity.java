package com.example.user.paynow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.example.user.paynow.underground.InsertData;
import com.example.user.paynow.underground.NetworkData;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class PayActivity extends AppCompatActivity {

    NetworkData networkData = new NetworkData();
    Context context;
    String IP_ADDRESS = networkData.IP_ADDRESS;
    String PAY_URL = "http://" + IP_ADDRESS + "/payment_danal.php";

    private WebView payView;
    private final String APP_SCHEME = "paynow://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        try{
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        context = this;
        Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyTableandDrop();
                Intent intent = new Intent(PayActivity.this, PayCompleteActivity.class);
                startActivity(intent);
            }
        });


        payView = (WebView) findViewById(R.id.payWebView);
        payView.setWebViewClient(new PayWebViewClient(this));
        WebSettings settings = payView.getSettings();
        settings.setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String url = "";
        url = intent.getStringExtra("payment");

        if(url.equals("kakao")){
            PAY_URL = "http://" + IP_ADDRESS + "/payment_kakao.php";
        } else{
            PAY_URL = "http://" + IP_ADDRESS + "/payment_danal.php";
        }


        int getTotal = intent.getIntExtra("total", 0);   //토탈프라이스 받아다가
        Log.d("TOTALLLLLLLL", Integer.toString(getTotal));   //로그로 일단 뿌림

        try {
            String postData = "";
            String total = Integer.toString(getTotal);//"10000";//intent.getStringExtra("total");
            postData = "total=" + URLEncoder.encode(total, "UTF-8");    //total값 post방식으로 넘겨주기
            payView.postUrl(PAY_URL, postData.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if ( intent != null ) {
            Uri intentData = intent.getData();

            if ( intentData != null ) {
                //카카오페이 인증 후 복귀했을 때 결제 후속조치
                String url = intentData.toString();

                if ( url.startsWith(APP_SCHEME) ) {
                    String path = url.substring(APP_SCHEME.length());
                    if ( "process".equalsIgnoreCase(path) ) {
                        payView.loadUrl("javascript:IMP.communicate({result:'process'})");
                    } else {
                        payView.loadUrl("javascript:IMP.communicate({result:'cancel'})");
                    }
                }
            }
        }

    }
    public void copyTableandDrop(){
        //일단 쇼핑리스트에 남아있는 애들 싹 받아다가 지워버리고 구매목록으로 옮겨버려야 함
        /*shopping table을 recipt list로 copy하고 싹 밀어버리는 코드*/
        //String query = "INSERT INTO recipt SELECT * FROM shoppinglist";
        InsertData task = new InsertData(context);
        task.execute("http://" + IP_ADDRESS + "/shoppingToRecipt.php", ""); //param을 넘겨줘야 함->insertData가 param을 받기 때문
        Log.d("DB", "shoppingList -> Recipt");
    }

}

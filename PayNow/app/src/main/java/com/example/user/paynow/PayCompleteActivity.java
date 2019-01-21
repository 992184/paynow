package com.example.user.paynow;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
* 결제 마치면 자동으로 여기로 온다.
* */
public class PayCompleteActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_complete);
        try{
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        //onClicklistener
        Button.OnClickListener onClickListener = new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                switch (view.getId()){

                    case R.id.gomainBtn:
                        intent = new Intent(PayCompleteActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.goReceiptBtn:
                        intent = new Intent(PayCompleteActivity.this, CompleteListActivity.class);
                        startActivity(intent);
                        break;
                }

            }
        };

        Button button = (Button)findViewById(R.id.gomainBtn);
        Button goRecipt = (Button)findViewById(R.id.goReceiptBtn);
        button.setOnClickListener(onClickListener);
        goRecipt.setOnClickListener(onClickListener);
    }
}

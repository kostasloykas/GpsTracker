package com.loukas.gpstracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private EditText phone;
    private EditText email;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ClearTitle();
        setContentView(R.layout.activity_user_info);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS} ,100);
        }
        sharedPreferences = getSharedPreferences("user_info",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        InitPhoneEmailText();


        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString("phone" , phone.getText().toString());
                editor.commit();
                String str =UserInfoActivity.this.GetValueOfKey("phone");
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString("email" , email.getText().toString());
                editor.commit();
                String str =UserInfoActivity.this.GetValueOfKey("email");
            }
        });


    }

    public String GetValueOfKey(String key){
        Map<String,?> entries = sharedPreferences.getAll();
        for (Map.Entry<String,?> entry:entries.entrySet()){
            if(entry.getKey().equals(key)){
                return (String) entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG,"Code="+requestCode+" length="+grantResults.length+" result="+grantResults[0]);
        if(requestCode==100 && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//          I have permission to send Message
        }else{
            this.finish();
        }
    }

    public void InitPhoneEmailText(){
        String tmp="";
        if((tmp = GetValueOfKey("phone"))!=null)
            phone.setText(tmp);

        if((tmp = GetValueOfKey("email"))!=null)
            email.setText(tmp);
    }

    public void ClearTitle(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }



}
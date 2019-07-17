package com.bytedance.androidcamp.network.dou;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView btnLogin;
    private Button btnSendCode;
    private EditText etPhone;
    private EditText etCode;
    EventHandler eventHandler;
    private TimeCount mTimeCount;
    private TextView note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        note = findViewById(R.id.note);
        btnLogin=findViewById(R.id.btn_login);
        btnSendCode=findViewById(R.id.btn_getcode);
        etCode=findViewById(R.id.et_code);

        String str = "登录即表明同意 <font color='#FFFF00'>用户协议</font> 和 <font color='#FFFF00'>隐私协议</font>";
        note.setText(Html.fromHtml(str));

        btnSendCode.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        etPhone=findViewById(R.id.et_phone);
        mTimeCount=new TimeCount(60000,1000);
        init();
    }

    public void init() {
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) { //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) { //提交验证码成功
                        startActivity(new Intent(LoginActivity.this, MainActivity1.class)); //页面跳转
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) { //获取验证码成功
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) { //返回支持发送验证码的国家列表
                    }
                } else {
                    ((Throwable) data).printStackTrace();
                }
            }
        };
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_getcode:
                if(!etPhone.getText().toString().trim().equals("")){
                    if(checkTel(etPhone.getText().toString().trim())){
                        SMSSDK.getVerificationCode("+86",etPhone.getText().toString());
                        mTimeCount.start();
                    }else{
                        Toast.makeText(
                                LoginActivity.this,"请输入正确的手机号码",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"请输入手机号码",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_login:
                if(!etPhone.getText().toString().trim().equals("")){
                    if(checkTel(etPhone.getText().toString().trim())){
                        if(!etCode.getText().toString().trim().equals("")){
                            SMSSDK.submitVerificationCode("+86",etPhone.getText().toString().trim(),etCode.getText().toString().trim());
                        }else{
                            Toast.makeText(LoginActivity.this,"请输入验证码",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(LoginActivity.this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"请输入手机号码",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public boolean checkTel(String tel){
        Pattern p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        Matcher matcher = p.matcher(tel);
        return matcher.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long l) {
            btnSendCode.setClickable(false);
            btnSendCode.setText(l/1000+"秒后重新获取");
        }
        @Override
        public void onFinish() {
            btnSendCode.setClickable(true);
            btnSendCode.setText("获取验证码");
        }
    }

}

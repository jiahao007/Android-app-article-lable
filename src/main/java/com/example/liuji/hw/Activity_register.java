package com.example.liuji.hw;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




public class Activity_register extends AppCompatActivity
{
    private EditText name;
    private EditText pwd;
    private EditText email;
    private String name1, pwd1, email1;
    private Button reg;

    final OkHttpClient client = new OkHttpClient();

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                String ReturnMessage = (String)msg.obj;
                final UserBean userBean = new Gson().fromJson(ReturnMessage, UserBean.class);
                final String info = userBean.getMsg();
                Toast. makeText (Activity_register.this, info,
                            Toast. LENGTH_SHORT ).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (EditText) findViewById(R.id.name);
        pwd = (EditText) findViewById(R.id.pwd);
        email = (EditText) findViewById(R.id.email);
        reg = (Button) findViewById(R.id.reg);
        reg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                name1 = name.getText().toString().trim();
                pwd1 = pwd.getText().toString().trim();
                email1 = email.getText().toString().trim();
                postRequest(name1, pwd1, email1);
            }
        });
    }

    /**
     *@param username
     *@param password
     */
    private void postRequest(String username,String email, String password)
    {
        RequestBody formBody = new FormBody.Builder().add("username", name1).add("email",email1).add("password", pwd1).build();
        final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_register").post(formBody).build();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Response response = null;
                try{
                    response = client.newCall(request).execute();
                    if(response.isSuccessful())
                    {
                        mHandler.obtainMessage(1, response.body().string()).sendToTarget();
                    }
                    else
                    {
                        throw new IOException("Unexpected code:" + response);
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
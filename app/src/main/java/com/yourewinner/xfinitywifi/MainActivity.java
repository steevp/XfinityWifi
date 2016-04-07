package com.yourewinner.xfinitywifi;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private OkHttpClient mClient;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        mClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();

        mDialog = new ProgressDialog(this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        Button startWifi = (Button) findViewById(R.id.start_wifi);
        startWifi.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new WifiAsyncTask().execute();
    }

    private void startWifi() throws Exception {
        mDialog.setProgress(0);

        Request request;
        Response response;

        request = new Request.Builder()
                .url("http://google.com")
                .build();

        response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        mDialog.setProgress(25);

        String redirect = response.priorResponse().header("Location");

        Pattern r = Pattern.compile("cm=([^&]+)&");
        Matcher m = r.matcher(redirect);
        if (!m.find()) throw new Exception("Client MAC not found in url: " + redirect);

        String clientMac = m.group(1);

        request = new Request.Builder()
                .url("https://xfinity.nnu.com/xfinitywifi/?client-mac=" + clientMac)
                .build();

        response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        mDialog.setProgress(50);

        FormBody formBody = new FormBody.Builder()
                .add("spn_terms", "1")
                .add("expyy", "99")
                .add("spn_email", "g@gmail.com")
                .add("rateplanid", "spn")
                .add("spn_postal", "12345")
                .add("expmm", "12")
                .build();

        request = new Request.Builder()
                .url("https://xfinity.nnu.com/xfinitywifi/signup/validate")
                .post(formBody)
                .build();

        response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        mDialog.setProgress(75);

        request = new Request.Builder()
                .url("https://xfinity.nnu.com/xfinitywifi/signup?loginid=1456186542")
                .build();

        response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        mDialog.setProgress(100);
    }

    private class WifiAsyncTask extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                startWifi();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(MainActivity.this, R.string.failure, Toast.LENGTH_LONG).show();
            }
        }
    }
}

package com.airk.exercise.volley;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.airk.exercise.volley.cloudant.CloudantIO;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import org.json.JSONException;

public class MainActivity extends ActionBarActivity implements CloudantIO.ResponseListener {
    private String TAG = "MainActivity";
    @InjectView(R.id.username)
    EditText mUsername;
    @InjectView(R.id.password)
    EditText mPassword;
    @InjectView(R.id.login)
    Button mLogin;
    @InjectView(R.id.tv)
    TextView mTV;

    private RequestQueue mQueue;
    private CloudantIO mCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mQueue = Volley.newRequestQueue(this);
        mCloud = new CloudantIO(mQueue);
        mCloud.setOnResponseListener(this);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloud.doLogin(mUsername.getText().toString(),
                        mPassword.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (!mLogin.isEnabled()) {
            mCloud.doLogout();
        }
        super.onDestroy();
    }

    @Override
    public void onResponse(String tag, String response) {
        Log.d(TAG, "TAG:" + tag + " " + response);
        mTV.setText(tag + ":" + response);
        if (tag.equals(CloudantIO.DOC_READ_TAG)) { //read success
            GsonUtil.Item item = new GsonUtil.Item();
            item._id = "test_data";
            item.name = "someone";
            item.phone = "18501020333";
            try {
                mCloud.createDoc("crud", item, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (tag.equals(CloudantIO.DOC_CREATE_TAG)) {
            GsonUtil.InsertResponse r = new Gson().fromJson(response, GsonUtil.InsertResponse.class);
            GsonUtil.Item item = new GsonUtil.Item();
            item.name = "Polly";
            item.phone = "1838383838383";
            try {
                mCloud.updateDoc("crud", item, r.id, r.rev, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (tag.equals(CloudantIO.DOC_UPDATE_TAG)) {
            GsonUtil.UpdateResponse r = new Gson().fromJson(response, GsonUtil.UpdateResponse.class);
            if (r.ok) {
                mCloud.deleteDoc("crud", r.id, r.rev, null);
            }
        }
        if (tag.equals(CloudantIO.DOC_DELETE_TAG)) {
            mCloud.createDatabase("data1", null);
        }
        if (tag.equals(CloudantIO.DATABASE_CREATE_TAG)) {
            mCloud.deleteDatabase("data1", null);
        }
    }

    @Override
    public void onLoginSuccess() {
        mCloud.readDoc("crud", "welcome", null);
        mLogin.setEnabled(false);
    }
}

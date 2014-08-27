package com.airk.exercise.volley.cloudant;

import android.support.annotation.NonNull;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 14-8-27.
 *
 * Cloudant Utils
 */
public class CloudantIO {
    private final String TAG = "Cloudant";

    private final String BASE_URL = "https://%1$s.cloudant.com/";
    private final String SESSION = "_session";

    private final String NAME_KEY = "name";
    private final String PASS_KEY = "password";
    private final String CONTENT_KEY = "Content-Type";
    private final String SET_COOKIE_KEY = "Set-Cookie";
    private final String COOKIE_KEY = "Cookie";
    private final String APP_JSON = "application/json";
    private final String APP_URL = "application/x-www-form-urlencoded";

    public static final String DATABASE_CREATE_TAG = "create_database";
    public static final String DATABASE_DELETE_TAG = "delete_database";
    public static final String DOC_READ_TAG = "read";
    public static final String DOC_CREATE_TAG = "create";
    public static final String DOC_UPDATE_TAG = "update";
    public static final String DOC_DELETE_TAG = "delete";

    private final String REV_KEY = "_rev";

    private String mCookie;
    private String mUrl;
    private RequestQueue mQueue;

    /**
     * ResponseListener
     */
    public interface ResponseListener {
        /**
         * Any response will cause it
         * @param tag tag what you have set, otherwise will be default
         * @param response Response message
         */
        public void onResponse(String tag, String response);

        /**
         * As you see, when login success will cause it
         */
        public void onLoginSuccess();
    }
    private ResponseListener mListener;

    /**
     * New
     * @param queue the Volley Request queue
     */
    public CloudantIO(RequestQueue queue) {
        mQueue = queue;
    }

    /**
     * Set response listener
     * @param listener ResponseListener
     */
    public void setOnResponseListener(ResponseListener listener) {
        mListener = listener;
    }

    /**
     * Login
     * @param name username
     * @param pass password
     */
    public void doLogin(final String name, final String pass) {
        mUrl = String.format(BASE_URL, name);
        final StringRequest request = new StringRequest(Request.Method.POST,
                mUrl + SESSION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (mListener != null) {
                            mListener.onLoginSuccess();
                        }
                    }
                }, null) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(CONTENT_KEY, APP_URL);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(NAME_KEY, name);
                map.put(PASS_KEY, pass);
                return map;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                for (String s : response.headers.keySet()) {
                    if (s.contains(SET_COOKIE_KEY)) {
                        mCookie = response.headers.get(s);
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };
        mQueue.add(request);
    }

    /**
     * Logout
     */
    public void doLogout() {
        StringRequest request = new StringRequest(Request.Method.DELETE,
                mUrl + SESSION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mQueue.add(request);
    }

    public void createDatabase(String name, final String tag) {
        StringRequest request = new StringRequest(Request.Method.PUT,
                mUrl + name,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DATABASE_CREATE_TAG : tag, s);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

    public void deleteDatabase(String name, final String tag) {
        StringRequest request = new StringRequest(Request.Method.DELETE,
                mUrl + name,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DATABASE_DELETE_TAG : tag, s);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

    /**
     * Read data from database
     * @param database database's name
     * @param _id ID
     * @param tag tag if your need, for response
     */
    public void readDoc(String database, String _id, final String tag) {
        StringRequest request = new StringRequest(mUrl + database + "/" + _id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DOC_READ_TAG : tag, s);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

    /**
     * Insert any object as data to database
     * @param database database's name
     * @param item to be insert
     * @param tag tag if your need, for response
     * @throws JSONException
     */
    public void createDoc(String database, Object item, final String tag) throws JSONException {
        JSONObject jsonObject = new JSONObject(new Gson().toJson(item));
        createDoc(database, jsonObject, tag);
    }

    /**
     * Insert json object as data to database
     * @param database database's name
     * @param json to be insert
     * @param tag tag if your need, for response
     */
    public void createDoc(String database, JSONObject json, final String tag) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                mUrl + database,
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DOC_CREATE_TAG : tag,
                                    jsonObject.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

    /**
     * Update data from database
     * @param database database's name
     * @param item data to be updated
     * @param rev Must equip this rev string
     * @param tag tag if your need, for response
     * @throws JSONException
     */
    public void updateDoc(String database, Object item, @NonNull String id,
                          @NonNull String rev, final String tag) throws JSONException {
        String url = mUrl + database + "/" + id + "?" + "rev=" + rev;
        JSONObject json = new JSONObject(new Gson().toJson(item));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                url,
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DOC_UPDATE_TAG : tag, jsonObject.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

    /**
     * Delete data from database
     * @param database database's name
     * @param id data's id
     * @param rev data's rev
     * @param tag tag if your need, for response
     */
    public void deleteDoc(String database, String id, final String rev, final String tag) {
        StringRequest request = new StringRequest(Request.Method.DELETE,
                mUrl + database + "/" + id + "?" + "rev=" + rev,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (mListener != null) {
                            mListener.onResponse(tag == null ? DOC_DELETE_TAG : tag, s);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(COOKIE_KEY, mCookie);
                return map;
            }
        };
        mQueue.add(request);
    }

}

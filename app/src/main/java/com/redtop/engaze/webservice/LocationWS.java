package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;

import org.json.JSONObject;

public class LocationWS extends BaseWebService {

    public static void updateLocation(JSONObject jRequestobj,
                                      final OnAPICallCompleteListner listnerOnSuccess,
                                      final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String apiUrl = MAP_API_URL + Routes.USER_LOCATION_UPLOAD;

            Log.d(TAG, "Calling URL:" + apiUrl);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    apiUrl, jRequestobj, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    listnerOnSuccess.apiCallComplete(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Volley Error: " + error.getMessage());
                    listnerOnFailure.apiCallComplete(null);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DEFAULT_MEDIUM_TIME_TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            addToRequestQueue(jsonObjReq, AppContext.context);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }
}

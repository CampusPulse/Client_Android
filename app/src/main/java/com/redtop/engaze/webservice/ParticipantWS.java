package com.redtop.engaze.webservice;

import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public class ParticipantWS extends BaseWebService implements IParticipantWS {

    private final static String TAG = ParticipantWS.class.getName();

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                        final OnAPICallCompleteListner listnerOnSuccess,
                                        final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String JsonPostURL = MAP_API_URL + ApiUrl.POKEALL_CONTACTS;

            postData(pokeAllContactsJSON, JsonPostURL, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }

    public void addRemoveParticipants(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + ApiUrl.UPDATE_PARTICIPANTS;

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

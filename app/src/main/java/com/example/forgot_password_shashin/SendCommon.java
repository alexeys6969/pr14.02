package com.example.forgot_password_shashin;

import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import java.io.IOException;

public class SendCommon extends AsyncTask<Void, Void, Void> {
    public String Url = "http://10.111.20.114:5000/api/CommonController/Send", Code;
    public EditText TbEmail;
    CallbackResponse CallbackResponse, CallbackError;
    public SendCommon(EditText tbEmail, CallbackResponse callbackResponse, CallbackResponse callbackError) {
        this.TbEmail = tbEmail;
        this.CallbackResponse = callbackResponse;
        this.CallbackError = callbackError;
    }

    @Override
    protected Void doInBackground(Void... Voids) {
        try {
            Document Response = (Document) Jsoup.connect(Url + "?Email="+TbEmail.getText())
                    .ignoreContentType(true)
                    .get();
            Code = Response.toString();
        } catch (IOException e) {
            Log.e("Errors", e.getMessage());
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(Code == null) CallbackError.returner("Error");
        else CallbackResponse.returner(Code);
    }
}

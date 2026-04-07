package com.example.forgot_password_shashin;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class SendCommon extends AsyncTask<Void, Void, String> {
    // ВАЖНО: Если сервер на другом устройстве, используйте его реальный IP.
    // 10.111.20.114 должен быть доступен с телефона/эмулятора.
    public String Url = "http://10.111.20.114:5000/api/user/send";
    public EditText TbEmail;
    CallbackResponse CallbackResponse, CallbackError;

    public SendCommon(EditText tbEmail, CallbackResponse callbackResponse, CallbackResponse callbackError) {
        this.TbEmail = tbEmail;
        this.CallbackResponse = callbackResponse;
        this.CallbackError = callbackError;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String email = TbEmail.getText().toString().trim();
        if (email.isEmpty()) return null;

        try {
            // Используем POST, если сервер ожидает данные в теле, или GET с параметром, как у вас было
            // Ваш сервер, судя по коду, ждет ?Email=...
            Document doc = Jsoup.connect(Url)
                    .data("Email", email) // Отправляем как параметр запроса (работает и для GET и для POST формы)
                    .timeout(5000)        // Таймаут 5 секунд
                    .ignoreContentType(true)
                    .execute()
                    .parse();

            // Если сервер возвращает просто текст (код), а не HTML:
            String response = Jsoup.connect(Url)
                    .data("Email", email)
                    .timeout(5000)
                    .ignoreContentType(true)
                    .execute()
                    .body();

            Log.d("SendCommon", "Ответ сервера: " + response);
            return response;

        } catch (SocketTimeoutException e) {
            Log.e("SendCommon", "Таймаут соединения", e);
            return "TIMEOUT";
        } catch (ConnectException e) {
            Log.e("SendCommon", "Не удалось подключиться к серверу (проверьте IP/сеть)", e);
            return "CONNECTION_ERROR";
        } catch (IOException e) {
            Log.e("SendCommon", "Ошибка IO", e);
            return "IO_ERROR";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null || result.contains("ERROR") || result.contains("TIMEOUT")) {
            if (CallbackError != null) CallbackError.returner(result);
        } else {
            if (CallbackResponse != null) CallbackResponse.returner(result);
        }
    }
}
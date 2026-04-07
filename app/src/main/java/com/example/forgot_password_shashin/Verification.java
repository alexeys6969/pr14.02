package com.example.forgot_password_shashin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask; // Убедитесь, что импортирован системный Timer, а не android.os

public class Verification extends AppCompatActivity {

    public ArrayList<EditText> BthNumbers = new ArrayList<>();
    public TextView tvText, tvSendMail;
    public Integer SelectNumbers = 0;
    public String Code;
    public SendCommon SendCommon;
    public MyTimerTask TimerTask;
    public Context Context;
    public Timer Timer = new Timer();
    public EditText tbUserEmail;
    public Drawable BackgroundRed, Background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Context = this;

        tvText = findViewById(R.id.timer);
        tvSendMail = findViewById(R.id.send_mail);
        tbUserEmail = findViewById(R.id.user_email);

        BthNumbers.add(findViewById(R.id.number1));
        BthNumbers.add(findViewById(R.id.number2));
        BthNumbers.add(findViewById(R.id.number3));
        BthNumbers.add(findViewById(R.id.number4));
        BthNumbers.add(findViewById(R.id.number5));
        BthNumbers.add(findViewById(R.id.number6));

        for(EditText BthNumber : BthNumbers)
            BthNumber.addTextChangedListener(TextChangedListener);

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            Code = arguments.get("Code").toString();
            String email = arguments.get("Email").toString();
            tbUserEmail.setText(email);

            // Запускаем таймер и первую отправку сразу при открытии
            startTimerAndSend(email);
        }

        BackgroundRed = ContextCompat.getDrawable(this, R.drawable.edittext_background_red);
        Background = ContextCompat.getDrawable(this, R.drawable.edittext_background);

        // Начальное состояние интерфейса: часы видны, кнопка скрыта
        tvText.setVisibility(View.VISIBLE);
        tvSendMail.setVisibility(View.GONE);
    }

    // Метод для запуска таймера и отправки (используется при старте и при повторе)
    private void startTimerAndSend(String email) {
        // 1. Сброс старого таймера
        if (TimerTask != null) TimerTask.cancel();
        Timer.purge();

        // 2. Создание и запуск нового таймера
        TimerTask = new MyTimerTask(this, tvText, tvSendMail);
        Timer.schedule(TimerTask, 0, 1000);

        // 3. Обновление UI
        tvText.setVisibility(View.VISIBLE);
        tvSendMail.setVisibility(View.GONE);

        // 4. КРИТИЧЕСКИ ВАЖНО: Создаем НОВЫЙ экземпляр AsyncTask
        // Старый нельзя использовать повторно
        SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);

        // Проверка статуса перед запуском (на всякий случай)
        if (SendCommon.getStatus() == AsyncTask.Status.PENDING) {
            SendCommon.execute();
        } else {
            Log.e("Verification", "Ошибка статуса AsyncTask: " + SendCommon.getStatus());
            // Если статус не PENDING, создаем еще раз (защита)
            SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
            SendCommon.execute();
        }
    }

    public void SendCode(View view) {
        String email = tbUserEmail.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(this, "Email пуст", Toast.LENGTH_SHORT).show();
            return;
        }
        startTimerAndSend(email);
    }

    TextWatcher TextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            // Логика переключения фокуса
            if(editable.length() > 0) {
                // Находим текущий EditText через цикл, так как editable.getParent() может быть ненадежным
                EditText currentEditText = null;
                for (EditText et : BthNumbers) {
                    if (et.getText() == editable) {
                        currentEditText = et;
                        break;
                    }
                }

                if (currentEditText != null) {
                    int index = BthNumbers.indexOf(currentEditText);
                    if(index < BthNumbers.size() - 1){
                        BthNumbers.get(index + 1).requestFocus();
                    }
                }
            }
            CheckCode();
        }
    };

    public void CheckCode() {
        String UserCode = "";
        for(EditText BthNumber : BthNumbers)
            UserCode += String.valueOf(BthNumber.getText());

        if(UserCode.length() == 6) {
            if(UserCode.equals(Code)) {
                for(EditText BthNumber : BthNumbers)
                    BthNumber.setBackground(Background);

                AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(this);
                AlertDialogBuilder.setTitle("Авторизация");
                AlertDialogBuilder.setMessage("Успешное подтверждение ОТР кода");
                AlertDialog AlertDialog = AlertDialogBuilder.create();
                AlertDialog.show();
            } else {
                for(EditText BthNumber : BthNumbers)
                    BthNumber.setBackground(BackgroundRed);
            }
        }
    }

    CallbackResponse CallbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context, "Ошибка сервера: " + (Response != null ? Response : "Нет связи"), Toast.LENGTH_LONG).show();
            // Не создаем здесь новый SendCommon, это делает пользователь кнопкой
        }
    };

    CallbackResponse CallbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context, "Код успешно отправлен", Toast.LENGTH_SHORT).show();
            Code = Response; // Обновляем код для проверки
            Log.d("Verification", "Получен новый код: " + Code);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (TimerTask != null) TimerTask.cancel();
        Timer.cancel();
        if (SendCommon != null && SendCommon.getStatus() == AsyncTask.Status.RUNNING) {
            SendCommon.cancel(true);
        }
    }
}
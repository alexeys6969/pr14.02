package com.example.forgot_password_shashin;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    public int Time = 30; // Начальное значение
    public Activity Activity;
    public TextView tvText, tvSendMail;

    public MyTimerTask(Activity activity, TextView tvText, TextView tvSendMail) {
        this.Activity = activity;
        this.tvText = tvText;
        this.tvSendMail = tvSendMail;
        this.Time = 30; // Явно сбрасываем при создании
    }

    @Override
    public void run() {
        Time--;

        // Обновляем UI в главном потоке
        Activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Time <= 0) {
                    Time = 0;
                    tvText.setText("00:00");
                    tvText.setVisibility(View.GONE);
                    tvSendMail.setVisibility(View.VISIBLE);
                    cancel(); // Останавливаем задачу
                } else {
                    String Second = Time > 9 ? String.valueOf(Time) : "0" + String.valueOf(Time);
                    tvText.setText("00:" + Second);
                }
            }
        });

        if (Time <= 0) {
            this.cancel();
        }
    }
}
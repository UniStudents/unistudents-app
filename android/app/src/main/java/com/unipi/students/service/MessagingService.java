package com.unipi.students.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.datadog.android.log.Logger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.students.MainActivity;
import com.unipi.students.R;
import com.unipi.students.worker.ScrapeDataWorker;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MessagingService extends FirebaseMessagingService {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.service.MessagingService")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("MessagingService")
            .build();
    private final String TAG = "MessagingServiceTAG";
    private Date timestampOfLastMessage = null;

    public MessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String urlToRedirect = remoteMessage.getData().get("url_to_redirect");
        String survey = remoteMessage.getData().get("survey");
        String title = remoteMessage.getData().get("title");
        String text = remoteMessage.getData().get("text");
        if (urlToRedirect != null && title != null && text != null) {
            this.redirectToUrl(urlToRedirect, title, text);
            return;
        }

        if (survey != null && title != null && text != null) {
            this.notifySurvey(title, text);
            return;
        }

        Date now = new Date();
        if (timestampOfLastMessage == null) {
            timestampOfLastMessage = now;
        } else {
            long diff = now.getTime() - timestampOfLastMessage.getTime();
            long diffMinutes = diff / (60 * 1000) % 60;
            if (diffMinutes >= 1) {
                timestampOfLastMessage = now;
            } else {
                return;
            }
        }

        if (remoteMessage.getData().size() > 0) {
            String searchFor = remoteMessage.getData().get("search_for");
            Log.d(TAG, "SEARCH_FOR: " + searchFor);
            if (searchFor != null) {
                logger.d("receive search_for " + searchFor);
                WorkRequest scrapeWorkRequest = new OneTimeWorkRequest.Builder(ScrapeDataWorker.class)
                        .setInputData(
                            new Data.Builder()
                                .putString("SEARCH_FOR", searchFor)
                                .build()
                        )
                        .setInitialDelay(new Random().nextInt(25 - 1) + 1, TimeUnit.SECONDS)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                        .build();

                WorkManager.getInstance(getApplicationContext()).enqueue(scrapeWorkRequest);
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void redirectToUrl(String url, String title, String text) {
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        intent.putExtra("url_to_redirect", url);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), "grades_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify("unistudents", 2021 , builder.build());
    }

    private void notifySurvey(String title, String text) {
        // check if the survey is already Completed or Dismissed
        if (new StorageService(getApplicationContext()).getSurvey() != null) return;

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), "grades_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify("unistudents", 2021 , builder.build());
    }
}

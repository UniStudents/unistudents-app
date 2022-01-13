package com.unipi.students;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.datadog.android.Datadog;
import com.datadog.android.DatadogConfig;
import com.unipi.students.common.CommonsFactory;
import com.unipi.students.plugins.ScrapePlugin;
import com.unipi.students.worker.ScrapeDataWorker;

import java.util.concurrent.TimeUnit;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private WorkManager mWorkManager;
    private static final String CHANNEL = "com.unipi.students/native";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Datadog.initialize(this, new DatadogConfig.Builder(getResources().getString(R.string.dd_client_token), "prod").build());
        Datadog.setVerbosity(Log.INFO);

        CommonsFactory.buildSSLSocketFactory(getApplicationContext());

        mWorkManager = WorkManager.getInstance(getApplication());
        fetchData();
        createNotificationChannel();
        checkIntent();

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    switch (call.method) {
                        case "getProgress":
                            new ScrapePlugin().getProgess(call, result);
                            break;
                        default:
                            result.notImplemented();
                            break;
                    }
                });
    }

    private void fetchData() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(ScrapeDataWorker.class, 2, TimeUnit.HOURS)
                        .addTag("SCRAPE_WORKER")
                        .setConstraints(constraints)
                        .build();

        mWorkManager.enqueueUniquePeriodicWork(
                "SCRAPE_WORKER",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("grades_channel", "Grades", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void checkIntent() {
        if (getIntent().getExtras() != null) {
            String urlToRedirect = getIntent().getExtras().getString("url_to_redirect");
            if (urlToRedirect != null) {
                Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToRedirect));
                if (openUrl.resolveActivity(getPackageManager()) != null) {
                    startActivity(openUrl);
                }
            }
        }
    }
}

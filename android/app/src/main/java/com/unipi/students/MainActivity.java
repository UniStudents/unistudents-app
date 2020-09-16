package com.unipi.students;

import android.os.Bundle;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.datadog.android.Datadog;
import com.datadog.android.DatadogConfig;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.unipi.students.plugin.FCMPlugin;
import com.unipi.students.plugin.PermissionPlugin;
import com.unipi.students.plugin.ScrapePlugin;
import com.unipi.students.worker.ScrapeDataWorker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BridgeActivity {

    private WorkManager mWorkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWorkManager = WorkManager.getInstance(getApplication());
        fetchData();

        // Initializes the Bridge
        this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
            // Additional plugins you've installed go here
            add(ScrapePlugin.class);
            add(FCMPlugin.class);
            add(PermissionPlugin.class);
        }});
    }

    private void fetchData() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(ScrapeDataWorker.class, 50, TimeUnit.MINUTES)
                        .addTag("SCRAPE_WORKER")
                        .setConstraints(constraints)
                        .build();

        mWorkManager.enqueueUniquePeriodicWork(
                "SCRAPE_WORKER",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);
    }
}

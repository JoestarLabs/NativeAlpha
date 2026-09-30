package com.cylonid.nativealpha.util;

import android.app.Application;
import android.content.Context;

import androidx.work.Configuration;
import androidx.work.WorkManager;

public class App extends Application {

	private static App instance;

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;
		if (!WorkManager.isInitialized()) {
			WorkManager.initialize(this, new Configuration.Builder().build());
		}

	}

	public static Context getAppContext() {
		return instance != null ? instance.getApplicationContext() : null;
	}
}

/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.catrobat.catroid.dagger.AppComponent;
import org.catrobat.catroid.dagger.CatroidModule;
import org.catrobat.catroid.dagger.DaggerAppComponent;
import org.catrobat.catroid.utils.CrashReporter;

import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class CatroidApplication extends Application implements HasActivityInjector {

	private static final String TAG = CatroidApplication.class.getSimpleName();

	private static Context context;
	public static String defaultSystemLanguage;

	public static final String OS_ARCH = System.getProperty("os.arch");
	public static boolean parrotLibrariesLoaded = false;
	public static boolean parrotJSLibrariesLoaded = false;

	private static GoogleAnalytics googleAnalytics;
	private static Tracker googleTracker;

	@Inject
	DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
	protected AppComponent appComponents;

	@TargetApi(28)
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "CatroidApplication onCreate");
		Log.d(TAG, "git commit info: " + BuildConfig.GIT_COMMIT_INFO);

		if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectNonSdkApiUsage()
					.penaltyLog()
					.build());
		}

		CrashReporter.initialize(this);

		context = getApplicationContext();

		createApplicationComponents();
		appComponents.initializeEagerSingletons();
		appComponents.inject(this);

		defaultSystemLanguage = Locale.getDefault().getLanguage();

		googleAnalytics = GoogleAnalytics.getInstance(this);
		googleAnalytics.setDryRun(BuildConfig.DEBUG);
	}

	protected void createApplicationComponents() {
		appComponents = DaggerAppComponent.builder()
				.catroidModule(new CatroidModule(this))
				.build();
	}

	@Override
	public AndroidInjector<Activity> activityInjector() {
		return dispatchingActivityInjector;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public synchronized Tracker getDefaultTracker() {
		if (googleTracker == null) {
			googleTracker = googleAnalytics.newTracker(R.xml.global_tracker);
		}

		return googleTracker;
	}

	@SuppressWarnings("PMD.AvoidUsingNativeCode")
	public static synchronized boolean loadNativeLibs() {
		if (parrotLibrariesLoaded) {
			return true;
		}

		try {
			System.loadLibrary("avutil");
			System.loadLibrary("swscale");
			System.loadLibrary("avcodec");
			System.loadLibrary("avfilter");
			System.loadLibrary("avformat");
			System.loadLibrary("avdevice");
			System.loadLibrary("adfreeflight");
			parrotLibrariesLoaded = true;
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG, Log.getStackTraceString(e));
			parrotLibrariesLoaded = false;
		}
		return parrotLibrariesLoaded;
	}

	@SuppressWarnings("PMD.AvoidUsingNativeCode")
	public static synchronized boolean loadJumpingSumoSDKLib() {
		if (parrotJSLibrariesLoaded) {
			return true;
		}

		try {
			System.loadLibrary("curl");
			System.loadLibrary("json");
			System.loadLibrary("arsal");
			System.loadLibrary("arsal_android");
			System.loadLibrary("arnetworkal");
			System.loadLibrary("arnetworkal_android");
			System.loadLibrary("arnetwork");
			System.loadLibrary("arnetwork_android");
			System.loadLibrary("arcommands");
			System.loadLibrary("arcommands_android");
			System.loadLibrary("arstream");
			System.loadLibrary("arstream_android");
			System.loadLibrary("arstream2");
			System.loadLibrary("arstream2_android");
			System.loadLibrary("ardiscovery");
			System.loadLibrary("ardiscovery_android");
			System.loadLibrary("arutils");
			System.loadLibrary("arutils_android");
			System.loadLibrary("ardatatransfer");
			System.loadLibrary("ardatatransfer_android");
			System.loadLibrary("armedia");
			System.loadLibrary("armedia_android");
			System.loadLibrary("arupdater");
			System.loadLibrary("arupdater_android");
			System.loadLibrary("armavlink");
			System.loadLibrary("armavlink_android");
			System.loadLibrary("arcontroller");
			System.loadLibrary("arcontroller_android");
			parrotJSLibrariesLoaded = true;
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG, Log.getStackTraceString(e));
			parrotJSLibrariesLoaded = false;
		}
		return parrotJSLibrariesLoaded;
	}

	public static Context getAppContext() {
		return CatroidApplication.context;
	}
}

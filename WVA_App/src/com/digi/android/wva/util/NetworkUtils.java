/* 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (c) 2013 Digi International Inc., All Rights Reserved. 
 */
 
package com.digi.android.wva.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides an abstraction over the Android networking APIs, for the purposes
 * of adding the method {@link #shouldBeAllowedToConnect(android.content.Context)},
 * which is used to indicate whether the Android device on which the application
 * is running is either connected to a Wi-Fi network, or serving as a mobile
 * Wi-Fi hotspot.
 */
public class NetworkUtils {
    /**
     * Because the Android emulator lacks support for using Wi-Fi connections,
     * we need a way to let NetworkUtils bypass the actual network-connectivity-
     * checking and allow the app, while running on the emulator, to connect
     * to devices regardless of Android-specific network state. To that end,
     * we know that the Android emulator, by default, has the build property
     * ro.product.model set to "sdk", and presumably no real device will have
     * that model 'number'. Knowing that, we can simply check
     * whether android.os.Build.MODEL === "sdk", and we can use that
     * to make {@link #shouldBeAllowedToConnect(android.content.Context)} always
     * return true when the app is running on the emulator.
     */
    private static final String EMULATOR_MODEL = "sdk";

    /**
     * No need for a public constructor, since NetworkUtils exists solely
     * as a namespace for {@link #shouldBeAllowedToConnect(android.content.Context)}
     */
    private NetworkUtils() { }

	private static boolean connectedToWifi(Context context) {
		ConnectivityManager connManager =
				(ConnectivityManager) context.getSystemService(
											Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(
											ConnectivityManager.TYPE_WIFI);
		return mWifi != null && mWifi.isConnected();
	}
	
	private static boolean tetheringActive(Context context) {
		// Use Java reflection to get WiFi access point state
		// stackoverflow.com/q/9065592
		
		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		boolean isEnabled = false;
		try {
			Method wmMethod = wifi.getClass().getDeclaredMethod("isWifiApEnabled");
			isEnabled = (Boolean) wmMethod.invoke(wifi);
		} catch (NoSuchMethodException e) {
			Log.e("NetworkUtils", "No WifiManager.isWifiApEnabled method!");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return isEnabled;
	}

    /**
     * Indicate if the Android device on which the application is running should
     * be allowed to begin its network calls to connect to a WVA device. This
     * is decided by detecting whether the device is <em>connected</em> to a
     * Wi-Fi network, or serving as a Wi-Fi hotspot.
     *
     * @param context to be used when invoking WifiManager and ConnectivityManager
     *                methods to determine network connectivity
     * @return true if attempted WVA networking should be permitted, given the
     * current state of network connectivity
     */
	public static boolean shouldBeAllowedToConnect(Context context) {
        if (EMULATOR_MODEL.equals(Build.MODEL)) {
            return true;
        }

		return connectedToWifi(context) || tetheringActive(context);
	}
}

package com.magic.spring.lightmode;

import android.app.Activity;
import android.support.annotation.ColorInt;

/**
 * Created by magic on 2017/11/16.
 */

public class StatusUtils {
	private static StatusUtils mInstance;
	private int TYPE = 0;

	private StatusUtils() {
	}

	public static StatusUtils getInstance() {
		if (mInstance == null) {
			synchronized (StatusUtils.class) {
				if (mInstance == null) {
					mInstance = new StatusUtils();
				}
			}
		}
		return mInstance;
	}


	public void setStatusBarColor(Activity activity, @ColorInt int color) {

		boolean darkEnough = StatusBar.isDarkEnough(color);
		if (!darkEnough) {//浅色系 设置默认透明度
			if (TYPE == 0) {
				TYPE = StatusBar.statusBarLightMode(activity);
			} else {
				StatusBar.statusBarLightMode(activity, TYPE);
			}

			if (TYPE == 0) {
				StatusBar.compat(activity);
			} else {
				StatusBar.compat(activity, color, 0);
			}
		} else {
			StatusBar.compat(activity, color, 0);//深色系  不设置透明度
			if (TYPE != 0) {
				StatusBar.statusBarDarkMode(activity, TYPE);
			}
		}
	}

	/**
	 * 透明效果
	 *
	 * @param activity
	 */
	public void compat(Activity activity) {
		StatusBar.compat(activity);
	}
}

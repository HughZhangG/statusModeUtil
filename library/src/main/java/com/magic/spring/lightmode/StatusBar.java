package com.magic.spring.lightmode;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by magic on 2017/11/16.
 */
class StatusBar {
	public static final int DEFAULT_STATUS_BAR_ALPHA = 112;
	public static final int DEFAULT_STATUS_BAR_ALPHA_TRANSPARENT = 0;
	private static final int FAKE_STATUS_BAR_VIEW_ID = R.id.statusbarutil_fake_status_bar_view;
	private static final int FAKE_TRANSLUCENT_VIEW_ID = R.id.statusbarutil_translucent_view;

	public static void setColor(Activity activity, @ColorInt int color) {
		setColor(activity, color, DEFAULT_STATUS_BAR_ALPHA);
	}

	public static void setColor(Activity activity, @ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			activity.getWindow().setStatusBarColor(calculateStatusColor(color, statusBarAlpha));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
			View fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID);
			if (fakeStatusBarView != null) {
				if (fakeStatusBarView.getVisibility() == View.GONE) {
					fakeStatusBarView.setVisibility(View.VISIBLE);
				}
				fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
			} else {
				decorView.addView(createStatusBarView(activity, color, statusBarAlpha));
			}
			setRootView(activity);
		}
	}


	/**
	 * 生成一个和状态栏大小相同的半透明矩形条
	 *
	 * @param activity 需要设置的activity
	 * @param color    状态栏颜色值
	 * @param alpha    透明值
	 * @return 状态栏矩形条
	 */
	private static View createStatusBarView(Activity activity, @ColorInt int color, int alpha) {
		// 绘制一个和状态栏一样高的矩形
		View statusBarView = new View(activity);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
		statusBarView.setLayoutParams(params);
		statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
		statusBarView.setId(FAKE_STATUS_BAR_VIEW_ID);
		return statusBarView;
	}


	/**
	 * 设置根布局参数
	 */
	private static void setRootView(Activity activity) {
		ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
		for (int i = 0, count = parent.getChildCount(); i < count; i++) {
			View childView = parent.getChildAt(i);
			if (childView instanceof ViewGroup) {
				childView.setFitsSystemWindows(true);
				((ViewGroup) childView).setClipToPadding(true);
			}
		}
	}

	/**
	 * 隐藏伪状态栏 View
	 *
	 * @param activity 调用的 Activity
	 */
	public static void hideFakeStatusBarView(Activity activity) {
		ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		View fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID);
		if (fakeStatusBarView != null) {
			fakeStatusBarView.setVisibility(View.GONE);
		}
		View fakeTranslucentView = decorView.findViewById(FAKE_TRANSLUCENT_VIEW_ID);
		if (fakeTranslucentView != null) {
			fakeTranslucentView.setVisibility(View.GONE);
		}
	}

	/**
	 * 获取状态栏高度
	 *
	 * @param context context
	 * @return 状态栏高度
	 */
	private static int getStatusBarHeight(Context context) {
		// 获得状态栏高度
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		return context.getResources().getDimensionPixelSize(resourceId);
	}

	/**
	 * 在不知道手机系统的情况下尝试设置状态栏字体模式为深色
	 * 也可以根据此方法判断手机系统类型
	 * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
	 *
	 * @param activity
	 * @return 1:MIUUI 2:Flyme 3:android6.0 0:设置失败
	 */
	public static int statusBarLightMode(Activity activity) {
		int result = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (MIUISetStatusBarLightMode(activity.getWindow(), true)) {
				result = 1;
			} else if (FlymeSetStatusBarLightMode(activity.getWindow(), true)) {
				result = 2;
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				int flags = activity.getWindow().getDecorView().getWindowSystemUiVisibility();
				activity.getWindow().getDecorView().setSystemUiVisibility(flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				result = 3;
			}
		}
		return result;
	}

	/**
	 * 小米 设置statusbar字体颜色
	 *
	 * @param darkmode
	 * @param window
	 */
	public static boolean MIUISetStatusBarLightMode(Window window, boolean darkmode) {
		boolean result = false;
		Class<? extends Window> clazz = window.getClass();
		try {
			int darkModeFlag = 0;
			Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
			Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
			darkModeFlag = field.getInt(layoutParams);
			Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
			extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
			result = true;
		} catch (Exception e) {
			Log.e("Xiaomi", "setStatusBarDarkMode: failed");
		}
		return result;
	}

	/**
	 * 魅族 设置statusbar字体颜色
	 *
	 * @param window
	 * @param dark
	 * @return
	 */
	public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
		boolean result = false;
		if (window != null) {
			try {
				WindowManager.LayoutParams lp = window.getAttributes();
				Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
				Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
				darkFlag.setAccessible(true);
				meizuFlags.setAccessible(true);
				int bit = darkFlag.getInt(null);
				int value = meizuFlags.getInt(lp);
				if (dark) {
					value |= bit;
				} else {
					value &= ~bit;
				}
				meizuFlags.setInt(lp, value);
				window.setAttributes(lp);
				result = true;
			} catch (Exception e) {
				Log.e("MeiZu", "setStatusBarDarkIcon: failed");
			}
		}
		return result;
	}


	/**
	 * 已知系统类型时，设置状态栏字体图标为深色。
	 * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
	 *
	 * @param activity
	 * @param type     1:MIUUI 2:Flyme 3:android6.0
	 */
	public static void statusBarLightMode(Activity activity, int type) {
		if (type == 1) {
			MIUISetStatusBarLightMode(activity.getWindow(), true);
		} else if (type == 2) {
			FlymeSetStatusBarLightMode(activity.getWindow(), true);
		} else if (type == 3) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					int flags = activity.getWindow().getDecorView().getWindowSystemUiVisibility();
					activity.getWindow().getDecorView().setSystemUiVisibility(flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				}
			}
		}
	}

	/**
	 * 已知系统类型时，清除MIUI或flyme或6.0以上版本状态栏字体深色模式
	 *
	 * @param activity
	 * @param type     1:MIUUI 2:Flyme 3:android6.0
	 */
	public static void statusBarDarkMode(Activity activity, int type) {
		if (type == 1) {
			MIUISetStatusBarLightMode(activity.getWindow(), false);
		} else if (type == 2) {
			FlymeSetStatusBarLightMode(activity.getWindow(), false);
		} else if (type == 3) {
			activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}

	public static boolean isDarkEnough(@ColorInt int color) {
		int red = color >> 16 & 0xff;
		int green = color >> 8 & 0xff;
		int blue = color & 0xff;
		//转换为YUV
		double grayLevel = red * 0.299 + green * 0.587 + blue * 0.114;
		//判断是否够“灰”
		if (grayLevel >= 192.0) {  //这里的192可以根据需要更改
			//浅色
			return false;
		} else {
			//深色
			return true;
		}
	}


	/**
	 * 计算状态栏颜色
	 *
	 * @param color color值
	 * @param alpha alpha值
	 * @return 最终的状态栏颜色
	 */
	private static int calculateStatusColor(@ColorInt int color, int alpha) {
		if (alpha == 0) {
			return color;
		}
		float a = 1 - alpha / 255f;
		int red = color >> 16 & 0xff;
		int green = color >> 8 & 0xff;
		int blue = color & 0xff;
		red = (int) (red * a + 0.5);
		green = (int) (green * a + 0.5);
		blue = (int) (blue * a + 0.5);
		return 0xff << 24 | red << 16 | green << 8 | blue;
	}
}

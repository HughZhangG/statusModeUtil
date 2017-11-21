package com.cheng.light.lightmodestatus;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.magic.spring.statusbarcolor.StatusUtils;

public class NextActivity extends AppCompatActivity {

	int type = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_next);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_next);
		int color = 0xffffffff;
		toolbar.setBackgroundColor(color);
		StatusUtils.getInstance().setStatusBarColor(this,color);
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	public void changeMode(View view){
		type = 1- type;
		if (type == 1){
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}else {
			int flags = getWindow().getDecorView().getWindowSystemUiVisibility();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				getWindow().getDecorView().setSystemUiVisibility(flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			}
		}
	}
}

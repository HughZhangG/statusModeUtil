package com.cheng.light.lightmodestatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.magic.spring.statusbarcolor.StatusUtils;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
		int color = 0xffaa66cc;
		toolbar.setBackgroundColor(color);

		StatusUtils.getInstance().setStatusBarColor(this,color);

	}

	public void go(View view) {
		startActivity(new Intent(this, NextActivity.class));
	}
}

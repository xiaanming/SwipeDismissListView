package com.example.swipedismisslistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.swipedismisslistview.SwipeDismissListView.OnDismissCallback;

public class SwipeActivity extends Activity {
	private SwipeDismissListView swipeDismissListView;
	private ArrayAdapter<String> adapter;
	private List<String> dataSourceList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe);
		init();
	}

	private void init() {
		swipeDismissListView = (SwipeDismissListView) findViewById(R.id.swipeDismissListView);
		for (int i = 0; i < 20; i++) {
			dataSourceList.add("»¬¶¯É¾³ý" + i);
		}

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
                android.R.id.text1, dataSourceList);
		
		swipeDismissListView.setAdapter(adapter);
		
		swipeDismissListView.setOnDismissCallback(new OnDismissCallback() {
			
			@Override
			public void onDismiss(int dismissPosition) {
				 adapter.remove(adapter.getItem(dismissPosition)); 
			}
		});

	}

}

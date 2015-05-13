package com.baby.cartoonnetwork;

import java.io.IOException;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.baby.adapters.CategoryAdapter;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.URLProvider;
import com.baby.model.CategoryData;
import com.baby.utils.Debug;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

@SuppressWarnings("deprecation")
public class CategoryActivity extends ActionBarActivity {

	private ListView categoryList;
	private CategoryAdapter categoryAdapter;
	private ProgressBar loadingBtn;
	private ImageButton disconnectBtn;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.category_activity);

		categoryList = (ListView) findViewById(R.id.categoryList);
		loadingBtn = (ProgressBar) findViewById(R.id.loadingBtn);
		disconnectBtn = (ImageButton) findViewById(R.id.disconnectBtn);

		categoryAdapter = new CategoryAdapter(this);
		categoryList.setAdapter(categoryAdapter);

		setTitle(getResources().getString(R.string.action_category));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		loadData();

		categoryList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				GlobalSingleton.getInstance().categoryID = categoryAdapter
						.getData().get(position).id;
				Intent data = new Intent();
				if (getParent() == null) { 
				    setResult(Activity.RESULT_OK, data);
				} else { 
				    getParent().setResult(Activity.RESULT_OK, data);
				} 
				finish();
			}
		});

		disconnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//loadData();
			}
		});
	}

	private void loadData() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(URLProvider.getCategory(), new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, String arg2) {
				Debug.logData("CategoryActivity", arg2);
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(
						DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);

				try {
					CategoryData detailObject = objectMapper.readValue(arg2,
							CategoryData.class);
					categoryAdapter.setData(detailObject.categories);
					categoryAdapter.notifyDataSetChanged();

					loadingBtn.setVisibility(View.GONE);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2,
					Throwable arg3) {
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

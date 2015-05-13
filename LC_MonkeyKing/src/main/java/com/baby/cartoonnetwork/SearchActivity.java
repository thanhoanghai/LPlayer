package com.baby.cartoonnetwork;

import java.io.IOException;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baby.adapters.FilmAdapter;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.URLProvider;
import com.baby.model.ConfigureObject;
import com.baby.model.MovieList;
import com.baby.policy.EndlessScrollListener;
import com.baby.policy.EndlessScrollListener.RefreshList;
import com.baby.utils.Debug;
import com.baby.utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

public class SearchActivity extends ActionBarActivity {

	private GridView mGrid;
	private ProgressBar mWaitingBar;
	private ImageButton disconnectBtn;

	private FilmAdapter filmAdapter;
	private int pageIndex = 1;
	private EndlessScrollListener scrollListener;

	protected AutoCompleteTextView mSearch;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.search_activity);
		setTitle(getResources().getString(R.string.action_search));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mSearch = (AutoCompleteTextView) findViewById(R.id.search_bar_et);
		mGrid = (GridView) findViewById(R.id.filmsGridView);
		mWaitingBar = (ProgressBar) findViewById(R.id.loadingBtn);
		disconnectBtn = (ImageButton) findViewById(R.id.disconnectBtn);
		scrollListener = new EndlessScrollListener(mGrid, new RefreshList() {

			@Override
			public void onRefresh(int pageNumber) {
				pageIndex = pageNumber;
				loadData();
			}
		});

		mGrid.setOnScrollListener(scrollListener);
		showContent();

		filmAdapter = new FilmAdapter(this);
		mGrid.setAdapter(filmAdapter);

		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent detailActivity = new Intent(SearchActivity.this,
						DetailActivity.class);
				detailActivity.putExtra(Constants.FILM_POSTER, filmAdapter
						.getData().get(position).poster);
				detailActivity.putExtra(Constants.FILM_ID, filmAdapter
						.getData().get(position).id);
				startActivity(detailActivity);
			}
		});

		disconnectBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadData();
			}
		});

		mSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String temp = mSearch.getText().toString();
					Utils.notifyEvent(getApplication(), "Search", temp, temp);
					filmAdapter.clearData();
					filmAdapter.notifyDataSetChanged();
					loadData();
					Utils.keyBoardForceHide(SearchActivity.this);
					return true;
				}
				return false;
			}
		});
	}

	private void loadData() {
		if (!TextUtils.isEmpty(mSearch.getText().toString())) {
			showLoading();
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(URLProvider.getSearch(mSearch.getText().toString(),
					pageIndex), new TextHttpResponseHandler() {

				@Override
				public void onSuccess(int arg0, Header[] arg1, String arg2) {
					Debug.logData("", arg2);

					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.configure(
							DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);

					try {
						MovieList moviesList = objectMapper.readValue(arg2,
								MovieList.class);

						if (moviesList != null
								&& moviesList.moviesObject != null
								&& moviesList.moviesObject.moviesList != null) {

							filmAdapter
									.setData(moviesList.moviesObject.moviesList);
							filmAdapter.notifyDataSetChanged();

							showContent();

							if ("yes"
									.equalsIgnoreCase(moviesList.moviesObject.more)) {
								scrollListener.notifyMorePages();
							} else {
								scrollListener.noMorePages();
							}

							if (!TextUtils.isEmpty(moviesList.cf)) {
								moviesList.cf = Utils
										.EncryptionKey(moviesList.cf);

								GlobalSingleton.getInstance().cf = moviesList.cf;

								ConfigureObject configure = objectMapper
										.readValue(moviesList.cf,
												ConfigureObject.class);
								GlobalSingleton.getInstance().Version = configure.v;
								GlobalSingleton.getInstance().configureObject = configure;

							}
						} else {
							Toast.makeText(SearchActivity.this, "No result",
									Toast.LENGTH_LONG).show();
						}

					} catch (IOException e) {
						e.printStackTrace();
						showDisconnect();
					}
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, String arg2,
						Throwable arg3) {
					showDisconnect();
				}
			});
		}
	}

	public void showContent() {
		mGrid.setVisibility(View.VISIBLE);
		mWaitingBar.setVisibility(View.GONE);
		disconnectBtn.setVisibility(View.GONE);
	}

	public void showLoading() {
		mGrid.setVisibility(View.GONE);
		mWaitingBar.setVisibility(View.VISIBLE);
		disconnectBtn.setVisibility(View.GONE);
	}

	public void showDisconnect() {
		mGrid.setVisibility(View.GONE);
		mWaitingBar.setVisibility(View.GONE);
		disconnectBtn.setVisibility(View.VISIBLE);
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

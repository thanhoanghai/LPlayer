package com.baby.policy;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.baby.adapters.FilmAdapter;
import com.baby.cartoonnetwork.DetailActivity;
import com.baby.cartoonnetwork.R;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.DataLoader;
import com.baby.dataloader.URLProvider;
import com.baby.model.ConfigureObject;
import com.baby.model.MovieList;
import com.baby.policy.EndlessScrollListener.RefreshList;
import com.baby.utils.Debug;
import com.baby.utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.IOException;

public class BaseFragment extends Fragment implements OnRefreshListener {

	private static final String TAG = "BaseFragment";

	private GridView mGrid;
	private ProgressBar mWaitingBar;
	private ImageButton disconnectBtn;
	private SwipeRefreshLayout mSwipeRefreshWidget;

	private FilmAdapter filmAdapter;
	private int pageIndex = 1;
	private EndlessScrollListener scrollListener;

	protected String category = "";

	public BaseFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.home_fragment, container, false);
		mGrid = (GridView) view.findViewById(R.id.filmsGridView);
		mWaitingBar = (ProgressBar) view.findViewById(R.id.loadingBtn);
		disconnectBtn = (ImageButton) view.findViewById(R.id.disconnectBtn);
		mSwipeRefreshWidget = (SwipeRefreshLayout) view
				.findViewById(R.id.swipe_refresh_widget);
		mSwipeRefreshWidget.setColorScheme(R.color.color1, R.color.color2,
				R.color.color3, R.color.color4);
		mSwipeRefreshWidget.setOnRefreshListener(this);

		scrollListener = new EndlessScrollListener(mGrid, new RefreshList() {

			@Override
			public void onRefresh(int pageNumber) {
				Debug.logFlow(TAG, "On Refresh invoked..");
				pageIndex = pageNumber;
				loadData();
			}
		});

		mGrid.setOnScrollListener(scrollListener);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Debug.logFlow(TAG, "onResume");
		if (GlobalSingleton.getInstance().changeMode) {
			GlobalSingleton.getInstance().changeMode = false;
			pageIndex = 1;
			mSwipeRefreshWidget.setRefreshing(true);
			loadData();
		}
	}

	@Override
	public void onRefresh() {
		Debug.logFlow(TAG, "onRefresh");
		pageIndex = 1;
		loadData();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		filmAdapter = new FilmAdapter(getActivity());
		mGrid.setAdapter(filmAdapter);

		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent detailActivity = new Intent(getActivity(),
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
	}

	protected void loadData() {
		String type = "";
		switch (GlobalSingleton.getInstance().menuType) {
		case Constants.MENU_ANIMES:
			type = "anime";
			break;
		case Constants.MENU_CARTOONS:
			type = "cartoon";
			break;
		case Constants.MENU_MOVIES:
			type = "movie";
			break;
		case Constants.MENU_TVSHOW:
			type = "show";
			break;
		case Constants.MENU_BOX:
			type = "box";
			break;
		default:
			break;
		}

		String url = "";
		if (type.equalsIgnoreCase("box")) {
			url = URLProvider.getFilmsBox(GlobalSingleton.getInstance().boxID,
					pageIndex);
		} else {
			url = URLProvider.getData(category, type,
					GlobalSingleton.getInstance().categoryID, pageIndex);
		}

		DataLoader.get(url, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, String arg2) {
				Debug.logData(TAG, arg2);

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(
						DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);

				try {
					MovieList moviesList = objectMapper.readValue(arg2,
							MovieList.class);

					if (!isAdded()) {
						return;
					}

					if (moviesList != null && moviesList.moviesObject != null
							&& moviesList.moviesObject.moviesList != null
							&& moviesList.moviesObject.moviesList.size() > 0) {

						if (mSwipeRefreshWidget.isRefreshing()) {
							filmAdapter.clearData();
							mSwipeRefreshWidget.setRefreshing(false);
							pageIndex = 1;
							scrollListener.setPageNumber(pageIndex);
						}

						filmAdapter.setData(moviesList.moviesObject.moviesList);
						filmAdapter.notifyDataSetChanged();

						showContent();

						if ("yes"
								.equalsIgnoreCase(moviesList.moviesObject.more)) {
							scrollListener.notifyMorePages();
						} else {
							scrollListener.noMorePages();
						}

						if (!TextUtils.isEmpty(moviesList.cf)) {
							moviesList.cf = Utils.EncryptionKey(moviesList.cf);

							Debug.logData(TAG, moviesList.cf);

							GlobalSingleton.getInstance().cf = moviesList.cf;

							ConfigureObject configure = objectMapper.readValue(
									moviesList.cf, ConfigureObject.class);
							GlobalSingleton.getInstance().Version = configure.v;
							GlobalSingleton.getInstance().configureObject = configure;

							if (configure.adsscreen > 0) {
								Intent intent = new Intent("loadeddata");
								LocalBroadcastManager
										.getInstance(getActivity())
										.sendBroadcast(intent);
							}

							try {
								if (!configure.appver
										.equalsIgnoreCase(getActivity()
												.getPackageManager()
												.getPackageInfo(
														getActivity()
																.getPackageName(),
														0).versionName)) {
									if (configure.upgrade
											.equalsIgnoreCase("yes")) {
										showForceApp();
									} else {
										showUpdateApp(configure.applink);
									}
								}
							} catch (NameNotFoundException e) {
								e.printStackTrace();
							}

						} else {
							if (GlobalSingleton.getInstance().configureObject.adsscreen > 0) {
								Intent intent = new Intent("loadeddata");
								LocalBroadcastManager
										.getInstance(getActivity())
										.sendBroadcast(intent);
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
					if (isAdded()) {
						showDisconnect();
					}
				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2,
					Throwable arg3) {
				Debug.logData(TAG, "onFailure");
				if (isAdded()) {
					showDisconnect();
				}
			}
		});
	}

	protected void showUpdateApp(String appLink) {
	}

	protected void showForceApp() {
	}

	/**
	 * For Force Update
	 * 
	 * @return
	 */
	protected AlertDialog showDialog() {
		return new AlertDialog.Builder(getActivity())
				.setTitle("Information")
				.setMessage(
						"Once more we are sorry any inconvenience. Please update the lastest app.")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									getActivity().finish();

									Intent myIntent = new Intent(
											Intent.ACTION_VIEW,
											Uri.parse(GlobalSingleton
													.getInstance().configureObject.applink));
									startActivity(myIntent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
								getActivity().finish();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	protected AlertDialog showDialog(final String link) {
		return new AlertDialog.Builder(getActivity())
				.setTitle("Information")
				.setMessage(
						"Once more we are sorry any inconvenience. Please update the lastest app.")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									dialog.dismiss();
									Intent myIntent = new Intent(
											Intent.ACTION_VIEW, Uri.parse(link));
									startActivity(myIntent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).show();
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

}
package com.baby.fragments;

import java.io.IOException;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.baby.adapters.BoxAdapter;
import com.baby.adapters.MenuAdapter;
import com.baby.cartoonnetwork.MainActivity;
import com.baby.cartoonnetwork.R;
import com.baby.cartoonnetwork.SettingActivity;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.DataLoader;
import com.baby.dataloader.URLProvider;
import com.baby.model.BoxData;
import com.baby.policy.BaseActivity;
import com.baby.utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nct.customview.GridviewCustom;
import com.nct.customview.ListviewCustome;

public class MenuFragment extends Fragment {

	private ListView mMenuList;
	private GridviewCustom mBoxList;

	private BoxAdapter boxAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_fragment, container, false);
		mMenuList = (ListView) view.findViewById(R.id.menuList);
		mBoxList = (GridviewCustom) view.findViewById(R.id.boxList);
		return view;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		MenuAdapter menuAdapter = new MenuAdapter(getActivity());
		mMenuList.setAdapter(menuAdapter);

		setListViewHeightBasedOnChildren(mMenuList);

		Fragment newContent = new ContentFragment();
		switchFragment(newContent);

		loadData();

		mMenuList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Fragment newContent = null;
				switch (position) {
				case 0:
					GlobalSingleton.getInstance().menuType = Constants.MENU_MOVIES;
					Utils.notifyGA(getActivity().getApplication(),
							"Movies Page");
					newContent = new ContentFragment();
					break;
				case 1:
					GlobalSingleton.getInstance().menuType = Constants.MENU_TVSHOW;
					Utils.notifyGA(getActivity().getApplication(),
							"TV Shows Page");
					newContent = new ContentFragment();
					break;
				case 2:
					GlobalSingleton.getInstance().menuType = Constants.MENU_CARTOONS;
					Utils.notifyGA(getActivity().getApplication(),
							"Cartoons Page");
					newContent = new ContentFragment();
					break;
				case 3:
					GlobalSingleton.getInstance().menuType = Constants.MENU_ANIMES;
					Utils.notifyGA(getActivity().getApplication(),
							"Animes Page");
					newContent = new ContentFragment();
					break;
				case 4:
					GlobalSingleton.getInstance().menuType = Constants.MENU_DOWNLOAD;
					newContent = new DownloadFragment();
					break;
				case 5:
					Intent settingActivity = new Intent(getActivity(),
							SettingActivity.class);
					startActivity(settingActivity);
					break;
				}

				if (newContent != null) {
					switchFragment(newContent);
					switch (GlobalSingleton.getInstance().menuType) {
					case Constants.MENU_MOVIES:
						((BaseActivity) getActivity()).updateTitle("Movies");
						break;
					case Constants.MENU_TVSHOW:
						((BaseActivity) getActivity()).updateTitle("TV Shows");
						break;
					case Constants.MENU_CARTOONS:
						((BaseActivity) getActivity()).updateTitle("Cartoons");
						break;
					case Constants.MENU_ANIMES:
						((BaseActivity) getActivity()).updateTitle("Animes");
						break;
					case Constants.MENU_DOWNLOAD:
						((BaseActivity) getActivity()).updateTitle("Download");
						break;
					default:
						break;
					}

				}
			}
		});

		mBoxList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Fragment newContent = null;
				GlobalSingleton.getInstance().menuType = Constants.MENU_BOX;
				GlobalSingleton.getInstance().boxID = String.valueOf(boxAdapter
						.getData().get(position).id);
				newContent = new BoxFragment();

				if (newContent != null) {
					switchFragment(newContent);
					((BaseActivity) getActivity()).updateTitle(boxAdapter
							.getData().get(position).title);
				}
			}
		});
	}

	private void loadData() {
		DataLoader.get(URLProvider.getBoxChannel(),
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, String arg2) {
						if (isAdded()) {
							ObjectMapper objectMapper = new ObjectMapper();
							objectMapper
									.configure(
											DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
											false);

							try {
								BoxData boxData = objectMapper.readValue(arg2,
										BoxData.class);
								if (boxData != null) {
									boxAdapter = new BoxAdapter(getActivity());
									boxAdapter.setData(boxData.boxs);
									mBoxList.setAdapter(boxAdapter);

									//setListViewHeightBasedOnChildren(mBoxList);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, String arg2,
							Throwable arg3) {
					}
				});
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity fca = (MainActivity) getActivity();
			fca.switchContent(fragment);
		}
	}
}

package com.baby.constant;

import com.baby.model.ConfigureObject;

public class GlobalSingleton {

	public String OS = "Android";
	public String Version = "1.0.0";
	public int menuType = Constants.MENU_MOVIES;
	public ConfigureObject configureObject;
	// For Client
	public int kidMode = 0;
	public int countFilm = 0;
	public String cf = "";
	public boolean changeMode = false;
	public String boxID = "0";
	public String categoryID = "0";
	public boolean offline = false;

	private GlobalSingleton() {
	}

	private static GlobalSingleton mInstance = null;

	public static GlobalSingleton getInstance() {
		if (mInstance == null) {
			mInstance = new GlobalSingleton();
		}
		return mInstance;
	}

}
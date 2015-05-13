package com.baby.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryData {
	public int result;
	public String message;
	@JsonProperty("data")
	public ArrayList<CategoryObject> categories;
}

package com.baby.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoxData {
	public int result;
	public String msg;
	@JsonProperty("data")
	public ArrayList<BoxObject> boxs;
}

package com.baby.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StreamResult {

	public int result;
	public String msg;
	@JsonProperty("data")
	public ArrayList<StreamObject> data;
	public String cf;

}

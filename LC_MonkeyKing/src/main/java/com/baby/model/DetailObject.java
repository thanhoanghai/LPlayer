package com.baby.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailObject {

	public int result;
	public String msg;
	@JsonProperty("data")
	public DetailData data;
	public String cf;
}

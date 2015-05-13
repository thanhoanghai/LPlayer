package com.baby.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieList {

	public int result;
	public String msg;
	@JsonProperty("data")
	public MoviesObject moviesObject;
	@JsonProperty("cf")
	public String cf;

}
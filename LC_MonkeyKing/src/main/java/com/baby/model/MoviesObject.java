package com.baby.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MoviesObject {
	@JsonProperty("films")
	public ArrayList<MovieObject> moviesList;
	@JsonProperty("more")
	public String more;
}

package com.baby.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailData {

	public String id;
	public String title;
	public String description;
	public String genre;
	public String state;
	@JsonProperty("chapters")
	public ArrayList<ChapterObject> chapters;
}

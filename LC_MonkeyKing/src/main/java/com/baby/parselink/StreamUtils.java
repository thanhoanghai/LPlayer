package com.baby.parselink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.text.TextUtils;

import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.URLProvider;
import com.baby.model.StreamObject;
import com.baby.model.StreamResult;
import com.baby.utils.Debug;
import com.baby.utils.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

public class StreamUtils {
	
	
	
	public static String getTextFromLink(String url) {
		try {
			HttpURLConnection.setFollowRedirects(true);
			URL u = new URL(url);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();

			huc.setRequestMethod("GET");
			huc.setConnectTimeout(8000);
			huc.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
			huc.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					huc.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getHeadFromLink(String url) {
		try {
			HttpURLConnection.setFollowRedirects(true);
			URL u = new URL(url);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			huc.setRequestMethod("GET");
			huc.setConnectTimeout(5000);
			huc.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
			huc.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					huc.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static ArrayList<StreamObject> parserStream(List<String> links,
			String df, String serverName, String orgStream) {
		ArrayList<StreamObject> result = new ArrayList<StreamObject>();
		for (String link : links) {
			StreamObject stream = new StreamObject();
			result.add(stream);
			stream.server = serverName;
			stream.stream = link;
			stream.parseType = 0;
			stream.sourceParsed = orgStream;
			stream.quality = TextUtils.isEmpty(df) ? "Auto" : df;
			if (link.indexOf("itag=18") > 0) {
				stream.quality = "360p";
			} else if (link.indexOf("itag=22") > 0) {
				stream.quality = "720p";
			} else if (link.indexOf("itag=34") > 0) {
				stream.quality = "360p";
			} else if (link.indexOf("itag=35") > 0) {
				stream.quality = "480p";
			} else if (link.indexOf("itag=36") > 0) {
				stream.quality = "240p";
			} else if (link.indexOf("itag=37") > 0) {
				stream.quality = "1080p";
			} else if (link.indexOf(".240.mp4") > 0) {
				stream.quality = "240p";
			} else if (link.indexOf(".360.mp4") > 0) {
				stream.quality = "360p";
			} else if (link.indexOf(".480.mp4") > 0) {
				stream.quality = "480p";
			} else if (link.indexOf(".720.mp4") > 0) {
				stream.quality = "720p";
			}else if (link.indexOf("720p") > 0) {
				stream.quality = "720p";
			}else if (link.indexOf("1080p") > 0) {
				stream.quality = "1080p";
			}else if (link.indexOf("360p") > 0) {
				stream.quality = "360p";
			}else if (link.indexOf("480p") > 0) {
				stream.quality = "480p";
			}
		}
		Collections.sort(result, new Comparator<StreamObject>() {

			@Override
			public int compare(StreamObject lhs, StreamObject rhs) {
				if(lhs.quality.equalsIgnoreCase("720p")){
					return 1;
				}
				if(rhs.quality.equalsIgnoreCase("720p")){
					return -1;
				}
				return lhs.quality.compareTo(rhs.quality);
			}
		});
		return result;
	}
	
	public static StreamObject findBestStream(List<StreamObject> streams, StreamObject lastStream){
		if(lastStream == null){
			return streams.get(0);
		}
		for(StreamObject currObj : streams)
		{
			if(currObj.server.equals(lastStream.server) && currObj.quality.equals(lastStream.quality)){
				return currObj;
			}
		}
		for(StreamObject currObj : streams)
		{
			if(currObj.server.equals(lastStream.server)){
				return currObj;
			}
		}
		for(StreamObject currObj : streams)
		{
			if(currObj.quality.equals(lastStream.quality)){
				return currObj;
			}
		}
		return streams.get(0);
	}

	public static JSONObject getJSONObject(String data) {
		JSONObject producedObject = null;
		try {
			if (!TextUtils.isEmpty(data)) {
				producedObject = new JSONObject(data);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return producedObject;
	}

	public static Map<Integer, JSONObject> parseFilm(JSONObject json)
			throws JSONException {
		Map<Integer, JSONObject> data = new HashMap<Integer, JSONObject>();
		JSONArray parseType = json.getJSONArray("pd");
		for (int i = 0; i < parseType.length(); i++) {
			JSONObject element = (JSONObject) parseType.get(i);
			data.put(element.getInt("t"), element.getJSONObject("p"));
		}
		return data;
	}
}

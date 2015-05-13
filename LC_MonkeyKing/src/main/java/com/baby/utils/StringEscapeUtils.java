/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baby.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author tuehm
 */
public class StringEscapeUtils {

	public static String escapeHtml(String str) {
		if (str == null) {
			return null;
		}
		try {
			StringWriter writer = new StringWriter((int) (str.length() * 1.5D));
			escapeHtml(writer, str);
			return writer.toString();
		} catch (IOException ioe) {
		}
		return null;
	}

	public static void escapeHtml(Writer writer, String string)
			throws IOException {
		if (writer == null) {
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (string == null) {
			return;
		}
		Entities.HTML40.escape(writer, string);
	}

	public static String unescapeHtml(String str) {
		if (str == null) {
			return null;
		}
		try {
			StringWriter writer = new StringWriter((int) (str.length() * 1.5D));
			unescapeHtml(writer, str);
			return writer.toString();
		} catch (IOException ioe) {
		}
		return null;
	}

	public static void unescapeHtml(Writer writer, String string)
			throws IOException {
		if (writer == null) {
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (string == null) {
			return;
		}
		Entities.HTML40.unescape(writer, string);
	}

	private static final Pattern htmlNumber = Pattern.compile("\\&\\#\\d+;?");

	public static String replaceHtmlEscapeNumber(String str) {
		if (str == null) {
			return null;
		}
		Matcher matcher = htmlNumber.matcher(str);
		while (matcher.find()) {
			String tmp = matcher.group();
			int pos = matcher.start();
			int end = matcher.end();
			int number = Integer.parseInt(tmp.replaceAll("[^0-9]*", ""));
			char ch = (char) number;
			str = str.substring(0, pos) + ch + str.substring(end);
			matcher = htmlNumber.matcher(str);
		}

		return str;
	}

}

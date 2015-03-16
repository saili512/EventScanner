package com.example.eventscanner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationParser {
	int start = 0;
	String subString = "";
	String location;
	String subLocation = "";
	String REGEX = "\\broom\\b|\\blocation\\b|\\bvenue\\b|\\bVenue\b|\\bLocation\b";
	boolean flag = true;
	HashMap<String, Boolean> dictionary = null;
	
	public LocationParser(){
		location = new String();
	}

	public void initialize() {
		dictionary = new HashMap<String, Boolean>();
		dictionary.put("Marston Science library", true);
		dictionary.put("Smathers library", true);
		dictionary.put("CISE Building", true);
		dictionary.put("Reitz Union", true);
		dictionary.put("West library", true);
		dictionary.put("Turlington", true);
		dictionary.put("L404", true);
		dictionary.put("T011", true);
		dictionary.put("E101", true);
		dictionary.put("E116", true);
		dictionary.put("B112", true);
	}

	public void getLocation(String input, int startindex) {
		System.out.println(subString + "==" + start);
		subString = input.substring(start);
		System.out.println("subString==" + subString);
		int end = subString.indexOf(" ");
		if (end < 0) {
			subLocation = subString.substring(0);
			flag = false;
		} else {
			subLocation = subString.substring(0, end);
		}
		if (containedInMap(subLocation)) {
			location = location + " " + subLocation;
		} else
			flag = false;
		start = subLocation.length() + 1;
	}

	public boolean containedInMap(String sublocation) {
		Set<String> keys = dictionary.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			String strings[] = string.split(" ");
			for (int i = 0; i < strings.length; i++) {
				String tempString = strings[i];
				if (tempString.equalsIgnoreCase(sublocation))
					return true;
			}
		}
		return false;
	}

	public String returnLocation(String input) {
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(input.toLowerCase()); // get a matcher object
		if (m.find()) {
			start = m.end() + 1;
			this.getLocation(input, start);
		}

		while (flag) {
			this.getLocation(this.subString, start);

		}
		return location.trim();
	}
}

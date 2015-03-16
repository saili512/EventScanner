package com.example.eventscanner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {
	ArrayList<String> dateregexes;
	ArrayList<SimpleDateFormat> formats;
	ArrayList<String> testStrings;

	ArrayList<String> timeregexes;
	HashMap<String, SimpleDateFormat> monthregexes;

	public void initialize() {
		dateregexes = new ArrayList<String>();
		dateregexes
				.add("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
		formats = new ArrayList<SimpleDateFormat>();
		formats.add(new SimpleDateFormat("dd/mm/yyyy"));
		dateregexes.add("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/(\\d\\d)");
		formats.add(new SimpleDateFormat("dd/mm/yy"));
		dateregexes.add("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])");
		formats.add(new SimpleDateFormat("dd/mm"));

		timeregexes = new ArrayList<String>();
		timeregexes.add("(?i)[0-9]{1,2}:??[0-9]{0,2}\\s??(?:am|pm)");

		monthregexes = new HashMap();
		monthregexes
				.put("(\\d\\d)\\s??\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|(nov|dec)(?:ember)?)\\s??((19|20)\\d\\d)",
						new SimpleDateFormat("dd MMM yyyy"));
		monthregexes
				.put("\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|(nov|dec)(?:ember)?)\\s??(\\d\\d)\\s??((19|20)\\d\\d)",
						new SimpleDateFormat("MMM dd yyyy"));
		monthregexes
				.put("(\\d\\d)\\s??\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|(nov|dec)(?:ember)?)",
						new SimpleDateFormat("dd MMM"));
		monthregexes
				.put("\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|(nov|dec)(?:ember)?)\\s??(\\d\\d)",
						new SimpleDateFormat("MMM dd"));
		monthregexes
				.put("\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|(nov|dec)(?:ember)?)",
						new SimpleDateFormat("MMM"));
	}

	private Date getDate(String desc) {

		String d = null;
		for (int i = 0; i < dateregexes.size(); i++) {
			Matcher m = Pattern.compile(dateregexes.get(i)).matcher(desc);
			SimpleDateFormat sF = formats.get(i);
			while (m.find()) {
				if (m.group() != null)
					d = m.group();
			}
			if (d != null)
				try {

					Date dt = sF.parse(d);
					if (i == dateregexes.size() - 1)
						;
					dt.setYear(new Date().getYear());
					return dt;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}

	private String[] getTime(String desc) {

		String[] times = new String[2];
		int count = 0;
		for (String s : timeregexes) {
			Matcher m = Pattern.compile(s).matcher(desc);
			while (m.find()) {
				times[count] = m.group();
				// System.out.println(times[count]);
				count++;
				if (count == 2)
					return times;
			}

		}
		return times;
	}

	public Date getMonth(String desc) {
		for (String regex : monthregexes.keySet()) {
			Matcher m = Pattern.compile(regex).matcher(desc);
			SimpleDateFormat sF = monthregexes.get(regex);
			if (m.find()) {
				try {
					return sF.parse(m.group());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days); // minus number would decrement the days
		return cal.getTime();
	}

	private Date getDay(String desc) {
		String[] days = { "monday", "tuesday", "wednesday", "thursday",
				"friday", "saturday", "sunday" };
		Calendar localCalendar = Calendar.getInstance();
		Date d = new Date();
		SimpleDateFormat format = new SimpleDateFormat("EEEE");
		for (String day : days) {
			if (desc.contains(day)) {
				while (!format.format(d).equalsIgnoreCase(day)) {
					d = addDays(d, 1);
				}
				return d;
			}
		}
		return d;
	}

	static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	// long t=date.getTime();
	// Date afterAddingTenMins=new Date(t + (10 * ONE_MINUTE_IN_MILLIS));

	public Date[] parseDate(String desc) {
		desc = desc.toLowerCase();
		desc = desc.replaceAll(",", " ");
		Date d = getDate(desc);

		if (d != null) {
		}
		// System.out.println(d);
		else {
			d = getMonth(desc);

			if (d != null) {
				// System.out.println(d);
				d.setYear(new Date().getYear());
			} else {
				d = getDay(desc);
			}
		}
		String[] ds = getTime(desc);

		String startTime = ds[0];
		String endTime = ds[1];

		Date startDate = new Date();
		Date endDate = new Date();
		if (startTime != null) {
			SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
			try {
				startDate = sdfs.parse(startTime);
				// System.out.println(startDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (endTime != null) {
			SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
			try {
				endDate = sdfs.parse(endTime);
				// System.out.println(endDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Date d1 = new Date(d.getTime());
		d.setHours(startDate.getHours());
		d.setMinutes(startDate.getMinutes());
		Date[] dates = new Date[2];
		dates[0] = d;
		d1.setHours(endDate.getHours());
		d1.setMinutes(endDate.getMinutes());
		dates[1] = d1;

		return dates;
	}
}

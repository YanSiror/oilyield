package com.example.demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static String dateToString(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strdate = df.format(date);
		return strdate;
	}

	public static String dateToStringNoTime(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String strdate = df.format(date);
		return strdate;
	}
	
	public static Date stringToDate(String strdate) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = df.parse(strdate);	//使用date类将相应数据转为date型,用于SQL查询
		} catch (ParseException e) {
			System.out.println("有异常");
			e.printStackTrace();
		}
		return date;
	}

	public static boolean isSameDate(Date d1, Date d2) {
		if(null == d1 || null == d2)
			return false;
		//return getOnlyDate(d1).equals(getOnlyDate(d2));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(d1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(d2);
		return  cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
	}
}

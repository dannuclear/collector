package ru.bisoft.collector.xjc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;


public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
	private static DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

	public LocalDate unmarshal(String v) throws Exception {
		if (v == null || v.length() == 0)
			return null;
		return (LocalDate) fmt.parse(v);
	}

	public String marshal(LocalDate v) throws Exception {
		if (v == null)
			return null;
		return fmt.format(v);
	}
}
package org.ValueReplacer.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVUtil {
	public String convertToCSV(String[] data) {
		return Stream.of(data)
				.map(this::escapeSpecialCharacters)
				.collect(Collectors.joining(","));
	}

	public void writeToCsvFile(List<String[]> dataLines, String file) {
		try {
			File csvOutputFile = new File(file);
			try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
				dataLines.stream()
						.map(this::convertToCSV)
						.forEach(pw::println);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String escapeSpecialCharacters(String data) {
		if (data == null) {
			throw new IllegalArgumentException("Input data cannot be null");
		}
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}

	public List<String[]> readAll(String file) {
		try {
			FileReader filereader = new FileReader(file);

			RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
			CSVReader csvReader = new CSVReaderBuilder(filereader)
					.withCSVParser(rfc4180Parser)
					.build();
			return csvReader.readAll();
		} catch (Exception e) {
			System.out.println(e);
		}

		return null;
	}
}

package org.ValueReplacer.valueReplacers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ValueReplacer {
	Map<String, List<String>> wordToFileMap = null;
	Map<String, String> fileToContentMap = null;
	String baseDirectory = null;
	void initialize();

	default Map<String, List<String>> getWordToFileMap() {
		return wordToFileMap;
	}
	default Map<String, String> getFileToContentMap() {
		return fileToContentMap;
	}
	default String getBaseDirectory() {
		return baseDirectory;
	}

	void setBaseDirectory(String directory);

	default List<String> getAllFiles(String directory) {
		File[] files = new File(directory).listFiles();
		List<String> fileList = new ArrayList<>();
		for (File file: files) {
			if (file.isDirectory()) {
				fileList.addAll(getAllFiles(file.getAbsolutePath()));
			} else {
				fileList.add(file.getAbsolutePath());
			}
		}

		return fileList;
	}
	void extractFileTokens(String file);

	void replaceValue(String old, String newValue, String modificationFile);

	default void writeContentsToFile(String file, String content) {
		try {
			Files.write(Paths.get(file), content.getBytes());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	default void combineResults() {

	}
}

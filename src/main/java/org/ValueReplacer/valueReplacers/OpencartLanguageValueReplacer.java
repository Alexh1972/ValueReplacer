package org.ValueReplacer.valueReplacers;

import org.ValueReplacer.modules.translation.OpencartTranslation;
import org.ValueReplacer.utils.CSVUtil;
import org.ValueReplacer.utils.ProgressBar;

import java.io.File;
import java.util.*;

public class OpencartLanguageValueReplacer implements ValueReplacer {
	Map<String, List<String>> wordToFileMap = null;
	Map<String, String> fileToContentMap = null;
	String baseDirectory = null;
	final ProgressBar progressBar = new ProgressBar();
	String directoryKeyword;
	final CSVUtil csvUtil = new CSVUtil();
	final OpencartTranslation opencartTranslation = new OpencartTranslation();
	@Override
	public Map<String, List<String>> getWordToFileMap() {
		return wordToFileMap;
	}
	@Override
	public Map<String, String> getFileToContentMap() {
		return fileToContentMap;
	}
	@Override
	public String getBaseDirectory() {
		return baseDirectory;
	}

	@Override
	public void initialize() {
		wordToFileMap = new HashMap<>();
		fileToContentMap = new HashMap<>();
		requestForExport();
	}

	private void exportToFile(String fileAddress) {
		List<String> languageFiles = getAllFiles(getBaseDirectory());
		List<String[]> languageInformations = new ArrayList<>();

		if (languageFiles.isEmpty())
			return;

		System.out.println("Exporting to file");
		long step = 1;
		long startTime = System.currentTimeMillis();
		progressBar.printProgress(startTime, languageFiles.size(), 0, "");
		for (String file : languageFiles) {
			try {
				File f = new File(file);
				String fileContent = "";
				Scanner fileReader = new Scanner(f);
				while (fileReader.hasNextLine()) {
					String data = fileReader.nextLine();
					fileContent += data + "\n";
				}

				fileReader.close();

				while (fileContent.contains("'")) {
					fileContent = fileContent.substring(fileContent.indexOf("'") + 1);
					String key = fileContent.substring(0, fileContent.indexOf("'"));
					fileContent = fileContent.substring(fileContent.indexOf("'") + 1);

					if (fileContent.contains("'")) {
						fileContent = fileContent.substring(fileContent.indexOf("'") + 1);
						String value = fileContent.substring(0, fileContent.indexOf("'"));
						fileContent = fileContent.substring(fileContent.indexOf("'") + 1);
						String[] languageInformation = {key, value, file};
						languageInformations.add(languageInformation);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			progressBar.printProgress(startTime, languageFiles.size(), step, "");
			step++;
		}
		progressBar.stopProgressBar();

		csvUtil.writeToCsvFile(languageInformations, fileAddress);
	}

	private void requestForExport() {
		System.out.print("Would you like to export all OC language files into csv file (y/n): ");
		Scanner scanner = new Scanner(System.in);

		String response = scanner.nextLine();

		if (response.isEmpty())
			return;

		if (response.toLowerCase().charAt(0) == 'n')
			return;

		if (response.toLowerCase().charAt(0) == 'y') {
			System.out.print("Address of export file: ");
			String fileAddress = scanner.nextLine();

			if (fileAddress.isEmpty())
				return;

			exportToFile(fileAddress);
		}
	}

	@Override
	public void setBaseDirectory(String directory) {
		this.baseDirectory = directory;
	}

	@Override
	public List<String> getAllFiles(String directory) {
		File[] files = new File(directory).listFiles();
		List<String> fileList = new ArrayList<>();
		for (File file: files) {
			if (file.isDirectory()) {
				fileList.addAll(getAllFiles(file.getAbsolutePath()));
			} else {
				if (file.getAbsolutePath().substring(0, (int) (file.getAbsolutePath().length() - file.getName().length())).contains("language"))
					fileList.add(file.getAbsolutePath());
			}
		}

		return fileList;
	}

	@Override
	public void extractFileTokens(String file) {
		try {
			File f = new File(file);
			String fileContent = "";
			Scanner fileReader = new Scanner(f);
			while (fileReader.hasNextLine()) {
				String data = fileReader.nextLine();
				fileContent += data + "\n";
			}

			fileReader.close();
			fileToContentMap.put(file, fileContent);

			while (fileContent.contains("'")) {
				fileContent = fileContent.substring(fileContent.indexOf("'") + 1);
				String key = fileContent.substring(0, fileContent.indexOf("'"));
				fileContent = fileContent.substring(fileContent.indexOf("'") + 1);

				if (fileContent.contains("'")) {
					fileContent = fileContent.substring(fileContent.indexOf("'") + 1);
					fileContent = fileContent.substring(fileContent.indexOf("'") + 1);

					List <String> listFiles = wordToFileMap.get(key);
					if (listFiles == null) {
						listFiles = new ArrayList<>();
					}

					listFiles.add(file);
					wordToFileMap.put(key, listFiles);
				}
			}


		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void replaceValue(String old, String newValue, String modificationFile) {
		List<String> filesList = getWordToFileMap().get(old);

		if (filesList == null)
			return;

		for (String file : filesList) {
			if (modificationFile != null && !file.equals(modificationFile))
				continue;

			String content = getFileToContentMap().get(file);
			int currentIndex = 0;
			while (content.substring(currentIndex).contains(old)) {
				int searchIndex = content.substring(currentIndex).indexOf(old) + old.length() + currentIndex;
				int beginValueIndex = content.substring(searchIndex + 1).indexOf("'") + searchIndex + 1;
				int endValueIndex = content.substring(beginValueIndex + 1).indexOf("'") + beginValueIndex + 1;
				beginValueIndex++;

				content = content.substring(0, beginValueIndex) + newValue + content.substring(endValueIndex);
				currentIndex = beginValueIndex + newValue.length();
			}

			getFileToContentMap().put(file, content);
		}
	}

	@Override
	public void combineResults() {
		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		opencartTranslation.getMissingTranslations();
	}
}

package org.ValueReplacer;

import org.ValueReplacer.utils.CSVUtil;
import org.ValueReplacer.utils.ProgressBar;
import org.ValueReplacer.utils.SleepUtil;
import org.ValueReplacer.valueReplacers.OpencartLanguageValueReplacer;
import org.ValueReplacer.valueReplacers.ValueReplacer;

import java.nio.file.FileSystems;
import java.util.*;

public class Application {
	private final SleepUtil sleepUtil = new SleepUtil();
	private final ProgressBar progressBar = new ProgressBar();
	private ValueReplacer valueReplacer = null;

	private final CSVUtil csvUtil = new CSVUtil();

	public void run() {
		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		System.out.print("Choose value replacer (default OC): ");

		Scanner scanner = new Scanner(System.in);
		String option = scanner.nextLine();

		if (!option.isEmpty()) {
			// other replacers
		}

		if (valueReplacer == null) {
			valueReplacer = new OpencartLanguageValueReplacer();
			System.out.println("You have chosen OpenCart!");
		}

		boolean runningModifications = true;
		while (runningModifications) {
			progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
			System.out.print("Would you like to modify values (y/n): ");

			String response = scanner.nextLine();

			if (!response.isEmpty() && response.toLowerCase().charAt(0) == 'y') {
				modifyValues();
			} else {
				runningModifications = false;
			}
		}

		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		System.out.print("Would you like to combine the results (y/n): ");
		String response = scanner.nextLine();

		if (!response.isEmpty() && response.toLowerCase().charAt(0) == 'y')
			valueReplacer.combineResults();
	}
	public void modifyValues() {
		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());

		Scanner scanner = new Scanner(System.in);
		System.out.print("Choose base directory (recursive): ");

		String baseDirectory = scanner.nextLine();

		if (baseDirectory.isEmpty()) {
			baseDirectory = FileSystems.getDefault()
					.getPath("")
					.toAbsolutePath()
					.toString();
		}

		System.out.println("Base directory: " + baseDirectory);

		valueReplacer.setBaseDirectory(baseDirectory);
		valueReplacer.initialize();

		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		System.out.println("Searching for matching files!");
		List<String> files = valueReplacer.getAllFiles(valueReplacer.getBaseDirectory());
		System.out.println("Found " + files.size() + " matching files!");

		if (files.isEmpty())
			return;

		System.out.println("Reading from files!");
		long readingFilesStartTime = System.currentTimeMillis();
		long readingFilesStep = 1;
		progressBar.printProgress(readingFilesStartTime, files.size(), 0, "");
		for (String file : files) {
			valueReplacer.extractFileTokens(file);

			progressBar.printProgress(readingFilesStartTime, files.size(), readingFilesStep, "");
			readingFilesStep++;
		}
		progressBar.stopProgressBar();
		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());

		System.out.print("Replacement file address(csv): ");
		String replacementFile = scanner.nextLine();

		if (replacementFile.isEmpty())
			return;

		replaceFromCsvFile(replacementFile);

		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		System.out.println("Saving modifications to files!");
		Set<String> savingFiles = valueReplacer.getFileToContentMap().keySet();
		long savingFilesStartTime = System.currentTimeMillis();
		long savingFilesStep = 1;
		progressBar.printProgress(savingFilesStartTime, files.size(), 0, "");
		for (String file : savingFiles) {
			valueReplacer.writeContentsToFile(file, valueReplacer.getFileToContentMap().get(file));

			progressBar.printProgress(savingFilesStartTime, files.size(), savingFilesStep, "");
			savingFilesStep++;
		}
		progressBar.stopProgressBar();
	}

	private void replaceFromCsvFile(String replacementFile) {
		try {
			System.out.println("Reading and executing modifications!");

			List<String[]> allData = csvUtil.readAll(replacementFile);

			if (allData == null || allData.isEmpty())
				return;

			long startTime = System.currentTimeMillis();
			long step = 1;
			progressBar.printProgress(startTime, allData.size(), 0, "");
			for (String[] row : allData) {
				if (row.length < 2)
					continue;

				if (row.length == 2) {
					String old = row[0];
					String newValue = row[1];
					valueReplacer.replaceValue(old, newValue, null);
				}

				if (row.length == 3) {
					String old = row[0];
					String newValue = row[1];
					String modificationFile = row[2];
					valueReplacer.replaceValue(old, newValue, modificationFile);
				}

				progressBar.printProgress(startTime, allData.size(), step, "");
				step++;
			}
			progressBar.stopProgressBar();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

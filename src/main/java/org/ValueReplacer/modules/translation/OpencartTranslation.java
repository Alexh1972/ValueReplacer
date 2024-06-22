package org.ValueReplacer.modules.translation;

import org.ValueReplacer.utils.CSVUtil;
import org.ValueReplacer.utils.PathConversion;
import org.ValueReplacer.utils.ProgressBar;
import org.ValueReplacer.utils.SleepUtil;

import java.util.*;

public class OpencartTranslation {
	final ProgressBar progressBar = new ProgressBar();
	final CSVUtil csvUtil = new CSVUtil();
	final PathConversion pathConversion = new PathConversion();

	final String mustModifyLabel = "MODIFY: ";
	public void getMissingTranslations() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Choose translation language .csv export file: ");
		String translationFile = scanner.nextLine();

		System.out.print("Base directory for translation language folder: ");
		String baseDirectoryTranslation = scanner.nextLine();

		System.out.print("Choose main language .csv export file: ");
		String mainLanguageFile = scanner.nextLine();

		System.out.print("Base directory for main language folder: ");
		String baseDirectoryMainLanguage = scanner.nextLine();

		System.out.print("File to save new translation: ");
		String newTranslationFile = scanner.nextLine();

		if (translationFile.isEmpty() || mainLanguageFile.isEmpty() || newTranslationFile.isEmpty())
			return;

		Map<List<String>, String> translationMap = exportCsvToMap(translationFile);
		Map<List<String>, String> mainLanguageMap = exportCsvToMap(mainLanguageFile);
		List<String[]> newLanguage = new ArrayList<>();
		List<String[]> alreadyTranslated = new ArrayList<>();
		List<String[]> sameWords = new ArrayList<>();
		List<String[]> notTranslated = new ArrayList<>();

		Set<List<String>> keys = mainLanguageMap.keySet();

		long step = 1;
		long startTime = System.currentTimeMillis();
		progressBar.printProgress(startTime, keys.size(), 0, "");
		for (List<String> key : keys) {
			String relativePath = pathConversion.getRelativePath(key.get(1), baseDirectoryMainLanguage);
			String translationAbsolutePath = baseDirectoryTranslation + relativePath;

			String translation = translationMap.get(Arrays.asList(key.get(0), translationAbsolutePath));
			String mainLanguage = mainLanguageMap.get(key);

			if (translation == null || translation.isEmpty()) {
				notTranslated.add(new String[]{key.get(0), mustModifyLabel + mainLanguage, translationAbsolutePath});
			} else {
				if (!translation.equals(mainLanguage)) {
					alreadyTranslated.add(new String[]{key.get(0), translation, translationAbsolutePath});
				} else {
					sameWords.add(new String[]{key.get(0), mustModifyLabel + translation, translationAbsolutePath});
				}
			}

			progressBar.printProgress(startTime, keys.size(), step, "");
			step++;
		}
		progressBar.stopProgressBar();

		newLanguage.addAll(sameWords);

		newLanguage.addAll(notTranslated);

		newLanguage.addAll(alreadyTranslated);

		csvUtil.writeToCsvFile(newLanguage, newTranslationFile);

		progressBar.printDelimiter(progressBar.getDefaultDelimiterSize(), progressBar.getDefaultDelimiter());
		System.out.println("New language is now available at " + newTranslationFile + "!");
		System.out.println("Expressions which are not already translated may be found in the beggining of the file!");
	}

	private Map<List<String>, String> exportCsvToMap(String file) {
		List<String[]> csvFileData = csvUtil.readAll(file);
		Map<List<String>, String> map = new HashMap<>();

		for (String[] row : csvFileData) {
			if (row.length == 3) {
				map.put(Arrays.asList(row[0], row[2]), row[1]);
			}
		}

		return map;
	}
}

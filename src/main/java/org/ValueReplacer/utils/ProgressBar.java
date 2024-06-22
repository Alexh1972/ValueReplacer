package org.ValueReplacer.utils;

import lombok.Data;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
@Data
public class ProgressBar {
	private final String defaultDelimiter = "+";
	private final int defaultDelimiterSize = 140;
	public void printProgress(long startTime, long total, long current, String description) {
		long eta = current == 0 ? 0 :
				(total - current) * (System.currentTimeMillis() - startTime) / current;

		String etaHms = current == 0 ? "N/A" :
				String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
						TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
						TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

		StringBuilder string = new StringBuilder(140);
		int percent = (int) (current * 100 / total);
		string
				.append('\r')
				.append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
				.append(description)
				.append("\t")
				.append(String.format(" %d%% [", percent))
				.append(String.join("", Collections.nCopies(percent, "=")))
				.append('>')
				.append(String.join("", Collections.nCopies(100 - percent, " ")))
				.append(']')
				.append(String.join("", Collections.nCopies(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
				.append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

		System.out.print(string);
	}

	public void stopProgressBar() {
		System.out.println("");
	}

	public void printDelimiter(int size, char delimiter) {
		printDelimiter(size, String.valueOf(delimiter));
	}

	public void printDelimiter(int size, String delimiter) {
		System.out.println(String.join("", Collections.nCopies(size, delimiter)));
	}
}

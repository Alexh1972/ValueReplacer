package org.ValueReplacer.utils;

public class PathConversion {
	public String getRelativePath(String path, String basePath) {
		if (path.startsWith(basePath))
			return path.substring(basePath.length());
		return null;
	}
}

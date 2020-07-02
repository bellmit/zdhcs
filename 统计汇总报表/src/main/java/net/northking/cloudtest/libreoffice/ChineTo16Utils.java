package net.northking.cloudtest.libreoffice;

public class ChineTo16Utils {
	public static String getUtf8(String strInput) {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < strInput.length(); i++) {
			output.append(Integer.toString(strInput.charAt(i), 16));
		}
		return output.toString().toUpperCase();
	}
}

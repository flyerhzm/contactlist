package com.huangzhimin.contacts.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 转换Unicode编码的中文字符
 * 
 * @author flyerhzm
 * 
 */
public class UnicodeChinese {

	// 存储所有的unicode编码的中文字符
	private static Map<String, String> map = new HashMap<String, String>();

	// 匹配unicode中文的正则
	private static Pattern pattern = Pattern.compile("&#\\d{5};");
	
	// 读取chinese.txt中所有的unicode编码的中文字符
	static {
		try {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource("chinese.txt");
            InputStream in = url.openStream();
            if (in == null) {
                JarFile jarFile = new JarFile(UnicodeChinese.class
                        .getProtectionDomain().getCodeSource().getLocation().getFile().toString());
                JarEntry jarEntry = jarFile.getJarEntry("chinese.txt");
                in = jarFile.getInputStream(jarEntry);
            }
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String temp = null;
			if ((temp = br.readLine()) != null) {
				String[] strs = temp.split(" ");
				for (String str : strs) {
					map.put("&#"+str.split(":")[1]+";", str.split(":")[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private UnicodeChinese() {}
	
	/**
	 * 根据单个unicode码获取对应的中文
	 * 
	 * @param unicode unicode码，形式如：&#40644;
	 * @return 对应的中文
	 */
	public static String getByUnicode(String unicode) {
		return map.get(unicode);
	}
	
	/**
	 * 将字符串中所有的unicode码全部转换成中文
	 * 
	 * @param str 字符串
	 * @return 转换之后的中文
	 */
	public static String transform(String str) {
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			str = str.replaceAll(matcher.group(), getByUnicode(matcher.group()));
		}
		return str;
	}
}

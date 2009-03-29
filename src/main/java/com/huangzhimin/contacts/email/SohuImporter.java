package com.huangzhimin.contacts.email;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import org.apache.commons.httpclient.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;


/**
 * 导入sohu联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class SohuImporter extends EmailImporter {
	// 预登录Url
	private String beforeLoginUrl = "http://passport.sohu.com/sso/login.jsp?userid=%email&password=%md5_pwd&appid=1000&persistentcookie=0&s=%time&b=2&w=1024&pwdtype=1";

	// 登录Url
	private String loginUrl = "http://login.mail.sohu.com/servlet/LoginServlet";

	// 联系人列表Url
	private String contactsUrl = null;

	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	private static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	/**
	 * 转换字节为16进制字符串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字符串
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * 构造函数
	 * 
	 * @param email
	 * @param password
	 */
	public SohuImporter(String email, String password) {
		super(email, password);
	}

	/**
	 * 登录sohu邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			String encode_email = URLEncoder.encode(email, "UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			String md5_pwd = byteArrayToHexString(md
					.digest(password.getBytes()));
			long time = Calendar.getInstance().getTimeInMillis();

			String encodeBeforeLoginUrl = beforeLoginUrl.replaceFirst("%email",
					encode_email).replaceFirst("%md5_pwd", md5_pwd)
					.replaceFirst("%time", "" + time);
			doGet(encodeBeforeLoginUrl, "http://mail.sohu.com/");

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, 36);
			client.getState().addCookie(
					new Cookie(".sohu.com", "crossdomain", ""
							+ calendar.getTimeInMillis(), "/", calendar
							.getTime(), false));

			doGet(loginUrl, "http://mail.sohu.com");
			contactsUrl = lastUrl.substring(0, lastUrl.lastIndexOf("/")) + "/contact";
		} catch (Exception e) {
			throw new ContactsException("sohu protocol has changed", e);
		}
	}

	/**
	 * 进入联系人列表页面，并读取所有的联系人信息
	 * 
	 * @return 所有的联系人信息
	 * @throws ContactsException
	 */
	public List<Contact> parseContacts() throws ContactsException {
		try {
			String json = doGet(contactsUrl);
			JSONTokener jsonTokener = new JSONTokener(json);
			Object o = jsonTokener.nextValue();
			JSONObject jsonObj = (JSONObject) o;
			JSONArray jsonContacts = jsonObj.getJSONArray("listString");
			List<Contact> contacts = new ArrayList<Contact>(jsonContacts
					.length());
			for (int i = 0; i < jsonContacts.length(); i++) {
				jsonObj = jsonContacts.getJSONObject(i);
				if (jsonObj.has("name") && jsonObj.has("email")) {
					contacts.add(new Contact(jsonObj.getString("name"), jsonObj
							.getString("email")));
				}

			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("sohu protocol has changed", e);
		}
	}
    
}

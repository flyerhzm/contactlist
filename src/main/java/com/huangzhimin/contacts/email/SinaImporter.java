package com.huangzhimin.contacts.email;

import java.util.ArrayList;
import java.util.List;


import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;

/**
 * 导入sina联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class SinaImporter extends EmailImporter {
	// 登录Url
	private String loginUrl = "http://mail.sina.com.cn/cgi-bin/login.cgi";

	/**
	 * 构造函数
	 * 
	 * @param email
	 * @param password
	 */
	public SinaImporter(String email, String password) {
		super(email, password);
	}

	/**
	 * 登录sina邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			NameValuePair params[] = { new NameValuePair("logintype", "uid"),
					new NameValuePair("u", getUsername(email)),
					new NameValuePair("psw", password) };
			client.getState().addCookies(
					new Cookie[] {
							new Cookie("mail.sina.com.cn",
									"sina_free_mail_recid", "false", "/", null,
									false),
							new Cookie("mail.sina.com.cn",
									"sina_vip_mail_recid", "false", "/", null,
									false) });
			doPost(loginUrl, params, "http://mail.sina.com.cn");
		} catch (Exception e) {
			throw new ContactsException("sina protocol has changed", e);
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
			NameValuePair params[] = { new NameValuePair("act", "list"),
					new NameValuePair("sort_item", "letter"),
					new NameValuePair("sort_type", "desc") };
			String contactsUrl = lastUrl.substring(0, lastUrl.lastIndexOf("/"))
					+ "/addr_member.php";
			String json = doPost(contactsUrl, params);
			JSONTokener jsonTokener = new JSONTokener(json);
			Object o = jsonTokener.nextValue();
			JSONObject jsonObj = (JSONObject) o;
			JSONObject jsonData = jsonObj.getJSONObject("data");
			JSONArray jsonContacts = jsonData.getJSONArray("contact");
			List<Contact> contacts = new ArrayList<Contact>();
			for (int i = 0; i < jsonContacts.length(); i++) {
				jsonObj = jsonContacts.getJSONObject(i);
				if (jsonObj.has("name") && jsonObj.has("email")) {
					contacts.add(new Contact(jsonObj.getString("name")
							.replaceAll("&nbsp;", " "), jsonObj
							.getString("email")));
				}

			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("sina protocol has changed", e);
		}
	}

}

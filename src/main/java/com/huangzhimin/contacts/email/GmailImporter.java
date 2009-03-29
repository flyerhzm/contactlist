package com.huangzhimin.contacts.email;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;


/**
 * 导入Gmail联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class GmailImporter extends EmailImporter {

	// 登录的URL
	private String loginUrl = "https://www.google.com/accounts/ServiceLoginAuth";

	// 联系人列表的URL
	private String contactsUrl = "https://mail.google.com/mail/contacts/data/contacts?thumb=true&show=ALL&enums=true&psort=Name&max=10000&out=js&rf=&jsx=true";

	/**
	 * 构造函数
	 * 
	 * @param email
	 * @param password
	 */
	public GmailImporter(String email, String password) {
		super(email, password);
	}

	/**
	 * 登录Gmail邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			NameValuePair params[] = { new NameValuePair("Email", email),
					new NameValuePair("Passwd", password) };
			doPost(loginUrl, params);
		} catch (Exception e) {
			throw new ContactsException("Gmail protocol has changed", e);
		}
	}

	/**
	 * 解析联系人列表
	 * 
	 * @throws ContactsException
	 */
	public List<Contact> parseContacts() throws ContactsException {
		try {
			retainCookies(new String[] { "SID" });
			String json = doGet(contactsUrl, "");

			String startTag = "&&&START&&&";
			String endTag = "&&&END&&&";
			json = json.substring(json.indexOf(startTag) + startTag.length(),
					json.indexOf(endTag));
			JSONTokener jsonTokener = new JSONTokener(json);
			Object o = jsonTokener.nextValue();
			JSONObject jsonObj = (JSONObject) o;
			jsonObj = jsonObj.getJSONObject("Body");
			JSONArray jsonContacts = jsonObj.getJSONArray("Contacts");
			List<Contact> contacts = new ArrayList<Contact>(jsonContacts
					.length());
			for (int i = 0; i < jsonContacts.length(); i++) {
				jsonObj = jsonContacts.getJSONObject(i);
				String name = null;
				if (jsonObj.has("Name"))
					name = jsonObj.getString("Name");
				if (jsonObj.has("Emails")) {
					JSONArray emails = jsonObj.getJSONArray("Emails");
					for (int j = 0; j < emails.length(); j++) {
						jsonObj = emails.getJSONObject(j);
						if (jsonObj.has("Address")) {
							String email = jsonObj.getString("Address");
							if (name == null || name.length() == 0)
								name = email.substring(0, email.indexOf("@"));
							email = email.toLowerCase();
							if (isEmailAddress(email))
								contacts.add(new Contact(name, email));
						}
					}

				}
			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("Gmail protocol has changed", e);
		}
	}

}

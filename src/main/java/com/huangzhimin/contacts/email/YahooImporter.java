package com.huangzhimin.contacts.email;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;


/**
 * 导入Yahoo联系人
 * 
 * @author flyerhzm
 * 
 */
public class YahooImporter extends EmailImporter {
	// 预登录Url
	private String beforeLoginUrl = "http://mail.cn.yahoo.com/";

	// 登录Url
	private String loginUrl = "https://edit.bjs.yahoo.com/config/login";

	// 联系人Url
	private String contactsUrl = "http://cn.address.yahoo.com/yab/cn?VPC=contact_list";

	/**
	 * 构造函数
	 * 
	 * @param email
	 * @param password
	 */
	public YahooImporter(String email, String password) {
		super(email, password);
	}

	/**
	 * 登录yahoo邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			String content = doGet(beforeLoginUrl);
			String challenge = getInputValue(".challenge", content);

			NameValuePair[] params = new NameValuePair[] {
					new NameValuePair(".intl", getInputValue(".intl", content)),
					new NameValuePair(".done", getInputValue(".done", content)),
					new NameValuePair(".src", getInputValue(".src", content)),
					new NameValuePair(".cnrid",
							getInputValue(".cnrid", content)),
					new NameValuePair(".challenge", challenge),
					new NameValuePair("login", email),
					new NameValuePair("passwd", password) };
			content = doPost(loginUrl, params, beforeLoginUrl);

			client.getState().addCookie(
					new Cookie("mail.cn.yahoo.com", "cn_challenge", challenge, "/", null, false));
			String redirectUrl = getJSRedirectLocation(content);
			doGet(redirectUrl);
		} catch (Exception e) {
			throw new ContactsException("Yahoo protocol has changed", e);
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
			String content = doGet(contactsUrl);
			List<Contact> contacts = new ArrayList<Contact>();
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(new ByteArrayInputStream(content
					.getBytes())));
			NodeList nodes = parser.getDocument().getElementsByTagName("td");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getChildNodes().getLength() == 5) {
					String username = node.getFirstChild().getNextSibling()
							.getFirstChild().getFirstChild().getNodeValue();
					i++;
					String email = nodes.item(i).getFirstChild()
							.getNextSibling().getFirstChild().getNextSibling()
							.getFirstChild().getNodeValue();
					contacts.add(new Contact(username, email));

				}
			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("Yahoo protocol has changed", e);
		}
	}

}

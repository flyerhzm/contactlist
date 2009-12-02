package com.huangzhimin.contacts.email;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.httpclient.NameValuePair;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;


/**
 * 导入Tom联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class TomImporter extends EmailImporter {
	// 登录Url
	private String loginUrl = "http://login.mail.tom.com/cgi/login";

	// 联系人列表Url
	private String contactsUrl = "http://bjapp6.mail.tom.com/cgi/ldvcapp?funcid=address&sid=%sid&tempname=address%2Faddress.htm&showlist=all&ifirstv=all&listnum=0";

	// 邮箱首页的body内容
	private String indexPage = null;

	// sid的Pattern
	private Pattern SidPattern = Pattern.compile("sid=([^\"]*)\"");

	/**
	 * 构造函数
	 * 
	 * @param email
	 * @param password
	 */
	public TomImporter(String email, String password) {
		super(email, password, "GBK");
	}

	/**
	 * 登录tom邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			NameValuePair params[] = { new NameValuePair("type", "0"),
					new NameValuePair("style", "10"),
					new NameValuePair("user", getUsername(email)),
					new NameValuePair("pass", password),
					new NameValuePair("verifycookie", "y") };
			indexPage = doPost(loginUrl, params, "http://mail.tom.com");
		} catch (Exception e) {
			throw new ContactsException("tom protocol has changed", e);
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
			String sid = getSid(indexPage, "folder");
			String content = doGet(contactsUrl.replaceFirst("%sid", sid));
			List<Contact> contacts = new ArrayList<Contact>();
			DOMParser parser = new DOMParser();
			InputSource is = new InputSource(new ByteArrayInputStream(content
					.getBytes("GBK")));
			is.setEncoding("GBK");
			parser.parse(is);
			NodeList nodes = parser.getDocument().getElementsByTagName("td");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				 if (node.getAttributes().getNamedItem("class") != null &&
                                        node.getAttributes().getNamedItem("class").getNodeValue().equals("Addr_Td_Name")) {
					String username = node.getTextContent().trim();
					i++;
					String email = nodes.item(i).getTextContent().trim();
					contacts.add(new Contact(username, email));
				}
			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("tom protocol has changed", e);
		}
	}

	/**
	 * 获取登录之后的sid
	 * 
	 * @param content
	 *            首页的页面body
	 * @param frameName
	 *            包含sid的frame的name
	 * @return sid
	 */
	private String getSid(String content, String frameName) {
		int indexMid = content.indexOf("name=\"" + frameName + "\"");
		int indexBegin = content.substring(0, indexMid).lastIndexOf("<");
		int indexEnd = content.indexOf("<", indexMid);

		Matcher matcher = SidPattern.matcher(content.substring(indexBegin,
				indexEnd));
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

}

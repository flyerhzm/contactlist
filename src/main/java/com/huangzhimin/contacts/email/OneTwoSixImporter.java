package com.huangzhimin.contacts.email;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;


/**
 * 导入126联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class OneTwoSixImporter extends EmailImporter {
	// 登录url
	private String loginUrl = "http://reg.163.com/login.jsp?type=1&product=mail126&url=http://entry.mail.126.com/cgi/ntesdoor?hid%3D10010102%26lightweight%3D1%26language%3D0%26style%3D3";

	// 联系人列表url
	private String contactsUrl = "http://tg1a64.mail.126.com/jy3/address/addrlist.jsp?sid=%sid";

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
	public OneTwoSixImporter(String email, String password) {
		super(email, password, "GBK");
	}

	/**
	 * 登录126邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			NameValuePair params[] = { new NameValuePair("domain", "126.com"),
					new NameValuePair("language", "0"),
					new NameValuePair("user", getUsername(email)),
					new NameValuePair("username", email),
					new NameValuePair("password", password),
					new NameValuePair("style", "3") };

			Calendar calendar = Calendar.getInstance();
			calendar.set(2099, 11, 31);

			client.getState().addCookies(
					new Cookie[] {
							new Cookie(".126.com", "ntes_mail_firstpage",
									"normal", "/", calendar.getTime(), false),
							new Cookie(".126.com", "logType", "jy", "/",
									calendar.getTime(), false),
							new Cookie(".126.com", "NETEASE_SSN",
									getUsername(email), "/",
									calendar.getTime(), false),
							new Cookie(".126.com", "ntes_mail_noremember",
									"true", "/", calendar.getTime(), false) });
			String responseStr = doPost(loginUrl, params, "http://www.126.com/");
			
			String redirectUrl1 = getJSRedirectLocation(responseStr);
			redirectUrl1 = redirectUrl1.replaceAll("\\|", "%7C");
			responseStr = doGet(redirectUrl1, loginUrl);
			
			String redirectUrl2 = getJSRedirectLocation(responseStr);
			indexPage = doGet(redirectUrl2, redirectUrl1);
		} catch (Exception e) {
			throw new ContactsException("126 protocol has changed", e);
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
			String content = doGet(contactsUrl.replaceFirst("%sid", sid) + "&gid=all");
			List<Contact> contacts = new ArrayList<Contact>();
			DOMParser parser = new DOMParser();
			InputSource is = new InputSource(new ByteArrayInputStream(content
					.getBytes("GBK")));
			is.setEncoding("GBK");
			parser.parse(is);
			NodeList nodes = parser.getDocument().getElementsByTagName("td");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getFirstChild().getNodeName()
						.equalsIgnoreCase("input")) {
					i++;
					String username = nodes.item(i).getFirstChild()
							.getFirstChild().getNodeValue();
					i++;
					String email = nodes.item(i).getFirstChild()
							.getFirstChild().getNodeValue();
					contacts.add(new Contact(username, email));
				}
			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("126 protocol has changed", e);
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

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
 * 导入Yeah联系人列表
 * 
 * @author flyerhzm
 * 
 */
public class YeahImporter extends EmailImporter {
	// 登录url
	private String loginUrl = "http://reg.163.com/logins.jsp?type=1&url=http://entry.yeah.net/cgi/ntesdoor?lightweight%3D1%26verifycookie%3D1%26style%3D9";

	// 联系人列表url
	private String contactsUrl = "http://g1a8.mail.yeah.net/jy3/address/addrlist.jsp?sid=%sid";

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
	public YeahImporter(String email, String password) {
		super(email, password, "GBK");
	}

	/**
	 * 登录yeah邮箱
	 * 
	 * @throws ContactsException
	 */
	public void doLogin() throws ContactsException {
		try {
			NameValuePair params[] = { new NameValuePair("username", email),
					new NameValuePair("user", getUsername(email)),
					new NameValuePair("password", password),
					new NameValuePair("style", "9") };

			String responseStr = doPost(loginUrl, params,
					"http://www.yeah.net/");

			String redirectUrl1 = getJSRedirectLocation(responseStr);
			redirectUrl1 = redirectUrl1.replaceAll("\\|", "%7C");
			responseStr = doGet(redirectUrl1, loginUrl);

			removeCookies(new String[] { "URSJESSIONID" });
			Calendar calendar = Calendar.getInstance();
			calendar.set(2099, 11, 31);
			Cookie[] cookies = client.getState().getCookies();
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("NTES_SESS")) {
					client.getState().addCookie(
							new Cookie(".yeah.net", "NTES_SESS", cookie
									.getValue(), cookie.getPath(), cookie
									.getExpiryDate(), cookie.getSecure()));
				}
			}
			client.getState().addCookies(
					new Cookie[] {
							new Cookie(".yeah.net", "logType", "9", "/",
									calendar.getTime(), false),
							new Cookie(".yeah.net", "ntes_mail_noremember",
									"true", "/", calendar.getTime(), false) });
			String redirectUrl2 = getJSRedirectLocation(responseStr);
			indexPage = doGet(redirectUrl2, redirectUrl1);
		} catch (Exception e) {
			throw new ContactsException("yeah protocal has changed", e);
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
				if (node.getFirstChild().getNodeName()
						.equalsIgnoreCase("input")) {
					i++;
					String username = nodes.item(i).getFirstChild()
							.getFirstChild().getNodeValue();
					i++;
					String email = nodes.item(i).getFirstChild()
							.getFirstChild().getNodeValue();
					if (username != null) {
						contacts.add(new Contact(username, email));
					}
				}
			}
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("yeah protocol has changed", e);
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

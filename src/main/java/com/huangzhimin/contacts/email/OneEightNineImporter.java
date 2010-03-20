package com.huangzhimin.contacts.email;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.NameValuePair;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 导入189联系人列表
 * 
 * @author flyerhzm
 */
public class OneEightNineImporter extends EmailImporter {
    // 登录url
    private String preLoginUrl = "http://webmail6.189.cn/webmail/UDBLogin";

    /**
     * 构造函数
     * 
     * @param email
     * @param password
     */
    public OneEightNineImporter(String email, String password) {
        super(email, password);
    }

    /**
     * 登录189邮箱
     * @throws com.huangzhimin.contacts.exception.ContactsException
     */
    @Override
    protected void doLogin() throws ContactsException {
        try {
            String responseStr = doGet(preLoginUrl, "http://webmail6.189.cn/webmail/");
            String loginUrl = lastUrl.substring(0, lastUrl.indexOf("PassportLogin")) + getFormUrl(responseStr);
            NameValuePair params[] = {
                new NameValuePair("__EVENTTARGET", ""),
                new NameValuePair("__EVENTARGUMENT", ""),
                new NameValuePair("__VIEWSTATE", getInputValue("__VIEWSTATE", responseStr)),
                new NameValuePair("__EVENTVALIDATION", getInputValue("__EVENTVALIDATION", responseStr)),
                new NameValuePair("txtUserId", getUsername(email)),
                new NameValuePair("txtPwd", password),
                new NameValuePair("ibtn_Login", ""),
                new NameValuePair("HiddenReg", getInputValue("HiddenReg", responseStr)),
                new NameValuePair("HiddenErrMsg", ""),
                new NameValuePair("TimeMsg", "")
            };
            responseStr = doPost(loginUrl, params, lastUrl);
            String redirectUrl = getHrefUrl(responseStr, "/webmail/logon.do");
            doGet(lastUrl.substring(0, lastUrl.indexOf("/webmail/")) + redirectUrl, loginUrl);
        } catch (Exception e) {
			throw new ContactsException("189 protocol has changed", e);
		}
    }

    /**
	 * 进入联系人列表页面，并读取所有的联系人信息
	 *
	 * @return 所有的联系人信息
     * @throws com.huangzhimin.contacts.exception.ContactsException
     */
    @Override
    protected List<Contact> parseContacts() throws ContactsException {
        try {
            int page = 1;
            List<Contact> contacts = new ArrayList<Contact>();
            while (true) {
                boolean empty = true;
                String contactsUrl = lastUrl.substring(0, lastUrl.indexOf("/webmail/")) + "/webmail/addressBookList.do?groupId=-1&page=" + page;
                String content = doGet(contactsUrl);
                DOMParser parser = new DOMParser();
                InputSource is = new InputSource(new ByteArrayInputStream(content.getBytes()));
                parser.parse(is);
                NodeList nodes = parser.getDocument().getElementsByTagName("td");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getAttributes().getNamedItem("class") != null) {
                        if (node.getAttributes().getNamedItem("class").getNodeValue().equals("mtb1-td2")) {
                            String username_text = node.getTextContent();
                            String username = username_text.substring(
                                    username_text.lastIndexOf(";>") + ";>".length(),
                                    username_text.lastIndexOf("</a>")).trim();
                            i += 2;
                            node = nodes.item(i);
                            String email_text = node.getTextContent();
                            String email = email_text.substring(
                                    email_text.lastIndexOf("document.write(\"") + "document.write(\"".length(),
                                    email_text.lastIndexOf("\""));
                            i++;
                            contacts.add(new Contact(username, email));
                            empty = false;
                        }
                        if (node.getAttributes().getNamedItem("class").getNodeValue().equals("tx")) {
                            // another style for 189
                            String username_text = node.getTextContent();
                            String username = username_text.substring(
                                    username_text.lastIndexOf("document.write(\"") + "document.write(\"".length(),
                                    username_text.lastIndexOf("\""));
                            i += 2;
                            node = nodes.item(i);
                            String email_text = node.getTextContent();
                            String email = email_text.substring(
                                    email_text.lastIndexOf("document.write(\"") + "document.write(\"".length(),
                                    email_text.lastIndexOf("\""));
                            i++;
                            contacts.add(new Contact(username, email));
                            empty = false;
                        }
                    }
                }
                if (empty) break;
                page++;
            }
            return contacts;
        } catch (Exception e) {
            throw new ContactsException("189 protocol has changed", e);
        }
    }

}

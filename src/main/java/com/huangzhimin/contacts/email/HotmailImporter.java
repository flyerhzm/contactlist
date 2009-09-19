package com.huangzhimin.contacts.email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;
import com.huangzhimin.contacts.utils.UnicodeChinese;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入Hotmail联系人
 * 
 * @author flyerhzm
 * 
 */
public class HotmailImporter extends EmailImporter {
    // 预登录Url

    private String beforeLoginUrl = "http://login.live.com/login.srf?id=2";

    // 邮箱首页的body内容
    private String indexPage = null;

    /**
     * 构造函数
     *
     * @param email
     * @param password
     */
    public HotmailImporter(String email, String password) {
        super(email, password);
    }

    /**
     * 登录hotmail邮箱
     *
     * @throws ContactsException
     */
    public void doLogin() throws ContactsException {
        try {
            String content = doGet(beforeLoginUrl);

            client.getState().addCookie(
                    new Cookie("login.live.com", "CkTst", "G" + new Date().getTime()));
            String actionUrl = getFormUrl(content);
            String pwdpad = "IfYouAreReadingThisYouHaveTooMuchFreeTime";
            pwdpad = pwdpad.substring(0, pwdpad.length() - password.length());
            NameValuePair[] params = new NameValuePair[]{
                new NameValuePair("login", email),
                new NameValuePair("passwd", password),
                new NameValuePair("LoginOptions", "2"),
                new NameValuePair("PPSX", getInputValue("PPSX", content)),
                new NameValuePair("PPFT", getInputValue("PPFT", content)),
                new NameValuePair("PwdPad", pwdpad)};
            content = doPost(actionUrl, params, beforeLoginUrl);

            String redirectUrl = getJSRedirectLocation(content);
            content = doGet(redirectUrl);

            indexPage = doGet(getIframeSrc(content));
        } catch (Exception e) {
            throw new ContactsException("Hotmail protocol has changed", e);
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
            int curPage = 1;
            List<Contact> contacts = new ArrayList<Contact>();
            String contactsUrl = getHrefUrl(indexPage, "ContactMainLight").replace("&#63;", "?").replace("&#61;", "=");
            String content = doGet(lastUrl.substring(0, lastUrl.indexOf("mail/")) + "mail/" + contactsUrl);
            while (true) {
                JSONObject jsonObj = parseJSON(content, "cxp_ic_control_data = ", ";");
                String[] names = JSONObject.getNames(jsonObj);
                for (String name : names) {
                    JSONArray jsonContact = jsonObj.getJSONArray(name);
                    String username = UnicodeChinese.transform(jsonContact.getString(3).replace("&#64;", "@"));
                    String email = null;
                    if (jsonContact.getString(6).contains("@")) {
                        email = jsonContact.getString(6);
                    }
                    if (jsonContact.getString(7).contains("@")) {
                        email = jsonContact.getString(7);
                    }
                    if (email != null) {
                        Contact contact = new Contact(username, email);
                        contacts.add(contact);
                    }
                }
                curPage++;
                content = unescape(content);
                int index = content.indexOf("ContactMainLight.aspx?ContactsSortBy=FileAs&Page=" + curPage);
                if (index < 0) {
                    break;
                } else {
                    String nextPageUrl = getHrefUrl(content,
                            "ContactMainLight.aspx?ContactsSortBy=FileAs&Page=" + curPage);
                    content = doGet(lastUrl.substring(0, lastUrl.indexOf("mail/")) + "mail/" + nextPageUrl);
                }
            }
            return contacts;
        } catch (Exception e) {
            throw new ContactsException("Hotmail protocol has changed", e);
        }
    }

    /**
     * 返回iframe的src值
     * 
     * @param content 网页body的内容
     * @return iframe的src值
     */
    private String getIframeSrc(String content) throws ContactsException {
        Pattern p = Pattern.compile("^.*src=\"([^\\s\"]+)\"");
        int index = content.indexOf("<iframe") + 5;
        content = content.substring(index,
                index + 300 <= content.length() ? index + 300 : content.length());
        Matcher matcher = p.matcher(content);
        if (!matcher.find()) {
            throw new ContactsException("Can't find iframe src");
        }
        return unescape(matcher.group(1));
    }

    private String unescape(String content) {
        return content.replaceAll("&#58;", ":")
                .replaceAll("&#47;", "/")
                .replaceAll("&#63;", "?")
                .replaceAll("&#38;", "&")
                .replaceAll("&#61;", "=");
    }
    
}

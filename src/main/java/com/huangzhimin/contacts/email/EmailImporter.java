package com.huangzhimin.contacts.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.ContactsImporter;
import com.huangzhimin.contacts.exception.ContactsException;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 邮箱联系人导入的抽象类
 * 
 * @author flyerhzm
 * 
 */
public abstract class EmailImporter implements ContactsImporter {

    // 邮箱地址
    protected String email;

    // 密码
    protected String password;
    // 编码
    protected String encoding;

    // 模拟浏览器
    protected HttpClient client;

    // 最后一次请求的response url
    protected String lastUrl;
    // 一次性从数据流读取的最大byte数
    private int max_bytes = 4096;
    protected Pattern emailPattern = Pattern.compile("^[0-9a-z]([-_.~]?[0-9a-z])*@[0-9a-z]([-.]?[0-9a-z])*\\.[a-z]{2,4}$");
    static Logger logger = Logger.getLogger(EmailImporter.class.getName());

    /**
     * 构造函数
     *
     * @param email
     * @param password
     */
    public EmailImporter(String email, String password) {
        this(email, password, "UTF-8");
    }

    /**
     * 构造函数
     *
     * @param email
     * @param password
     * @param encoding
     */
    public EmailImporter(String email, String password, String encoding) {
        this.email = email;
        this.password = password;
        this.encoding = encoding;

        client = new HttpClient();
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter("http.protocol.content-charset",
                encoding);
        client.getParams().setParameter("http.protocol.single-cookie-header",
                true);

    }

    /**
     * 获取联系人列表
     *
     * @return 联系人列表
     * @throws ContactsException
     */
    public List<Contact> getContacts() throws ContactsException {
        int i = 3;    // 如果发生错误，重连3次
        while (true) {
            try {
                doLogin();
                return parseContacts();
            } catch (Exception e) {
                if (--i == 0) {
                    throw new ContactsException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 登录邮箱
     *
     * @throws ContactsException
     */
    protected abstract void doLogin() throws ContactsException;

    /**
     * 进入联系人列表页面，并读取所有的联系人信息
     *
     * @return 所有的联系人信息
     * @throws ContactsException
     */
    protected abstract List<Contact> parseContacts() throws ContactsException;

    /**
     * 进行HTTP Post请求
     *
     * @param actionUrl
     *            post请求的url
     * @param params
     *            post请求的参数
     *
     * @return Post应答体
     * @throws HttpException
     * @throws IOException
     */
    protected String doPost(String actionUrl, NameValuePair[] params)
            throws HttpException, IOException {
        return doPost(actionUrl, params, "");
    }

    /**
     * 进行HTTP Post请求
     *
     * @param actionUrl
     *            post请求的url
     * @param params
     *            post请求的参数
     * @param referer
     *            HTPP REFERER
     *
     * @return Post应答体
     * @throws HttpException
     * @throws IOException
     */
    protected String doPost(String actionUrl, NameValuePair[] params,
            String referer) throws HttpException, IOException {
        PostMethod method = new PostMethod(actionUrl);
        setHeaders(method);
        method.setRequestHeader("Referer", referer);
        method.setRequestHeader("Content-Type",
                "application/x-www-form-urlencoded");
        method.setRequestBody(params);
        logPostRequest(method);
        client.executeMethod(method);
        String responseStr = readInputStream(method.getResponseBodyAsStream());
        logPostResponse(method, responseStr);
        method.releaseConnection();
        if (method.getResponseHeader("Location") != null) {
            if (method.getResponseHeader("Location").getValue().startsWith(
                    "http")) {
                return doGet(method.getResponseHeader("Location").getValue());
            } else {
                return doGet("http://" + getResponseHost(method) + method.getResponseHeader("Location").getValue());
            }
        } else {
            lastUrl = method.getURI().toString();
            return responseStr;
        }
    }

    protected String doSoapPost(String url, String body, String soapAction) throws HttpException, IOException {
        PostMethod method = new PostMethod(url);
        setHeaders(method);
        method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        if (soapAction != null)
            method.setRequestHeader("SOAPAction", soapAction);
        method.setRequestEntity(new StringRequestEntity(body, "text/xml", "UTF-8"));
        logPostRequest(method);
        client.executeMethod(method);
        String responseStr = readInputStream(method.getResponseBodyAsStream());
        logPostResponse(method, responseStr);
        method.releaseConnection();
        lastUrl = method.getURI().toString();
        return responseStr;
    }

    /**
     * 获取Post应答消息的Host
     *
     * @param method
     *            Post方法
     * @return 应答消息的Host
     * @throws URIException
     */
    private String getResponseHost(PostMethod method) throws URIException {
        String url = method.getURI().toString();
        return url.split("/")[2];
    }

    /**
     * 进行HTTP GET请求
     *
     * @param url
     *            GET请求url
     * @return GET应答体
     * @throws HttpException
     * @throws IOException
     */
    protected String doGet(String url) throws HttpException, IOException {
        return doGet(url, "");
    }

    /**
     * 进行HTTP GET请求
     *
     * @param url
     *            GET请求url
     * @param referer
     *            HTPP REFERER
     * @return GET应答体
     * @throws HttpException
     * @throws IOException
     */
    protected String doGet(String url, String referer) throws HttpException,
            IOException {
        GetMethod method = new GetMethod(url);
        setHeaders(method);
        method.setRequestHeader("Referer", referer);
        logGetRequest(method);
        client.executeMethod(method);
        String responseStr = readInputStream(method.getResponseBodyAsStream());
        logGetResponse(method, responseStr);
        method.releaseConnection();
        lastUrl = method.getURI().toString();
        return responseStr;
    }

    /**
     * 获取js中的重定向地址ַ
     *
     * @param content
     *            html内容
     * @return 重定向的地址ַ
     */
    protected String getJSRedirectLocation(String content) {
        String name = "window.location.replace(\"";
        int index = content.indexOf(name) + name.length();
        content = content.substring(index);
        content = content.substring(0, content.indexOf("\""));
        return content;
    }

    /**
     * 记录Get请求信息
     *
     * @param method
     *            Get操作
     * @throws URIException
     */
    private void logGetRequest(GetMethod method) throws URIException {
        logger.debug("do get request: " + method.getURI().toString());
        logger.debug("header:\n" + getHeadersStr(method.getRequestHeaders()));
        logger.debug("cookie:\n" + getCookieStr());
    }

    /**
     * 记录Get应答信息
     *
     * @param method
     *            Get操作
     * @param responseStr
     *            Get应答体
     * @throws URIException
     */
    private void logGetResponse(GetMethod method, String responseStr)
            throws URIException {
        logger.debug("do get response: " + method.getURI().toString());
        logger.debug("header: \n" + getHeadersStr(method.getResponseHeaders()));
        logger.debug("body: \n" + responseStr);
    }

    /**
     * 保留部分Cookie，删除剩下的。
     *
     * @param cookieNames
     *            需要保留的cookie的名字
     */
    protected void retainCookies(String[] cookieNames) {
        Cookie[] cookies = client.getState().getCookies();
        ArrayList<Cookie> retainCookies = new ArrayList<Cookie>();
        for (Cookie cookie : cookies) {
            if (Arrays.binarySearch(cookieNames, cookie.getName()) >= 0) {
                retainCookies.add(cookie);
            }
        }
        client.getState().clearCookies();
        client.getState().addCookies(retainCookies.toArray(new Cookie[0]));
    }

    /**
     * 删除部分Cookie。
     *
     * @param cookieNames
     *            需要删除的cookie的名字
     */
    protected void removeCookies(String[] cookieNames) {
        Cookie[] cookies = client.getState().getCookies();
        ArrayList<Cookie> retainCookies = new ArrayList<Cookie>();
        for (Cookie cookie : cookies) {
            if (Arrays.binarySearch(cookieNames, cookie.getName()) < 0) {
                retainCookies.add(cookie);
            }
        }
        client.getState().clearCookies();
        client.getState().addCookies(retainCookies.toArray(new Cookie[0]));
    }

    /**
     * 将输入流按照特定的编码转换成字符串
     *
     * @param is
     *            输入流
     * @return 字符串
     * @throws IOException
     */
    private String readInputStream(InputStream is) throws IOException {
        byte[] b = new byte[max_bytes];
        StringBuilder builder = new StringBuilder();
        int bytesRead = 0;
        while (true) {
            bytesRead = is.read(b, 0, max_bytes);
            if (bytesRead == -1) {
                return builder.toString();
            }
            builder.append(new String(b, 0, bytesRead, encoding));
        }
    }

    /**
     * 记录HTTP请求的信息
     *
     * @param method
     *            Post操作
     * @throws URIException
     */
    private void logPostRequest(PostMethod method) throws URIException {
        logger.debug("do post request: " + method.getURI().toString());
        logger.debug("header:\n" + getHeadersStr(method.getRequestHeaders()));
        logger.debug("body:\n" + getPostBody(method.getParameters()));
        logger.debug("cookie:\n" + getCookieStr());
    }

    /**
     * 记录HTTP应答的信息
     *
     * @param method
     *            Post操作
     * @param responseStr
     *            应答体
     * @throws URIException
     */
    private void logPostResponse(PostMethod method, String responseStr)
            throws URIException {
        logger.debug("do post response:" + method.getURI().toString());
        logger.debug("header:\n" + getHeadersStr(method.getResponseHeaders()));
        logger.debug("body:\n" + responseStr);
    }

    /**
     * 设置Http请求的Header
     *
     * @param method
     *            Http Method
     */
    private void setHeaders(HttpMethod method) {
        method.setRequestHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;");
        method.setRequestHeader("Accept-Language", "zh-cn");
        method.setRequestHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
        method.setRequestHeader("Accept-Charset", encoding);
        method.setRequestHeader("Keep-Alive", "300");
        method.setRequestHeader("Connection", "Keep-Alive");
        method.setRequestHeader("Cache-Control", "no-cache");
    }

    /**
     * 将Header[]转换为String
     *
     * @param headers
     *            Http头信息
     * @return String形式的Http头信息
     */
    private String getHeadersStr(Header[] headers) {
        StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            builder.append(header.getName()).append(": ").append(
                    header.getValue()).append("\n");
        }
        return builder.toString();
    }

    /**
     * 得到String形式的post请求体
     *
     * @param postValues
     *            Post请求的键值对
     * @return String形式的post请求体
     */
    private String getPostBody(NameValuePair[] postValues) {
        StringBuilder builder = new StringBuilder();
        for (NameValuePair pair : postValues) {
            builder.append(pair.getName()).append(": ").append(pair.getValue()).append("\n");
        }
        return builder.toString();
    }

    /**
     * 得到String形式的cookie
     *
     * @return String形式的cookie
     */
    private String getCookieStr() {
        Cookie[] cookies = client.getState().getCookies();
        StringBuilder builder = new StringBuilder();
        for (Cookie cookie : cookies) {
            builder.append(cookie.getDomain()).append(":").append(
                    cookie.getName()).append("=").append(cookie.getValue()).append(";").append(cookie.getPath()).append(";").append(
                    cookie.getExpiryDate()).append(";").append(
                    cookie.getSecure()).append(";\n");
        }
        return builder.toString();
    }

    /**
     * 获取Email地址的用户名
     *
     * @param email
     *            email地址
     * @return 用户名
     */
    protected String getUsername(String email) {
        return email.split("@")[0];
    }

    /**
     * 将一段字符串解析为JSONObject
     *
     * @param content 字符串内容
     * @param startTag json字符串的起始标签
     * @return JSON对象
     * @throws org.json.JSONException
     */
    protected JSONObject parseJSON(String content, String startTag) throws JSONException {
        String json = content.substring(content.indexOf(startTag) + startTag.length());
        JSONTokener jsonTokener = new JSONTokener(json);
        Object o = jsonTokener.nextValue();
        return (JSONObject) o;
    }

    /**
     * 将一段字符串解析为JSONObject
     *
     * @param content 字符串内容
     * @param startTag json字符串的起始标签
     * @param endTag json字符串的终止标签
     * @return JSON对象
     * @throws org.json.JSONException
     */
    protected JSONObject parseJSON(String content, String startTag, String endTag) throws JSONException {
        String sub_content = content.substring(content.indexOf(startTag) + startTag.length());
        String json = sub_content.substring(0, sub_content.indexOf(endTag));
        JSONTokener jsonTokener = new JSONTokener(json);
        Object o = jsonTokener.nextValue();
        return (JSONObject) o;
    }

    /**
     * 判断是否是正确的email地址
     *
     * @param email
     *            email地址
     * @return 判断是否正确
     */
    protected boolean isEmailAddress(String email) {
        return emailPattern.matcher(email).matches();
    }

    /**
     * 从网页的body中获取form的actionֵ
     *
     * @param content
     *            网页的body
     * @return form的action值ֵ
     * @throws ContactsException
     */
    protected String getFormUrl(String content) throws ContactsException {
        Pattern p = Pattern.compile("^.*action=\"([^\\s\"]+)\"");
        int index = content.indexOf("<form") + 5;
        content = content.substring(index,
                index + 200 <= content.length() ? index + 200 : content.length());
        Matcher matcher = p.matcher(content);
        if (!matcher.find()) {
            throw new ContactsException("Can't find from url");
        }
        return matcher.group(1);
    }

    /**
     * 获取Href的Url
     *
     * @param content
     *            网页的body内容
     * @param hrefPrefix
     *            href的起始字符串
     * @return href的url
     */
    protected String getHrefUrl(String content, String hrefPrefix) {
        content = content.substring(content.indexOf(hrefPrefix));
        int endIndex = content.indexOf("\"");
        if (endIndex == -1) {
            endIndex = content.indexOf("'");
        }
        String href = content.substring(0, endIndex);
        return href;
    }

    /**
     * 获取html中input的值ֵ
     *
     * @param name
     *            input的name
     * @param content
     *            html内容
     * @return input的值ֵ
     * @throws ContactsException
     */
    protected String getInputValue(String name, String content)
            throws ContactsException {
        Pattern p = Pattern.compile("^.*?value=\"([^\\s\"]+)\"");
        int index = content.indexOf(name);
        int start = content.substring(index - 200 > 0 ? index - 200 : 0, index).lastIndexOf("<input") + (index - 200 > 0 ? index - 200 : 0);
        int end = content.substring(
                index,
                index + 200 <= content.length() ? index + 200 : content.length()).indexOf(">") + index;
        Matcher matcher = p.matcher(content.substring(start, end));
        if (!matcher.find()) {
            throw new ContactsException("Can't find input value");
        }
        return matcher.group(1);
    }
}

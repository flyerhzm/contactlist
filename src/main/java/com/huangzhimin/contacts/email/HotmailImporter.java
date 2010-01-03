package com.huangzhimin.contacts.email;

import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.exception.ContactsException;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;

/**
 * 导入Hotmail联系人
 * 
 * @author flyerhzm
 * 
 */
public class HotmailImporter extends EmailImporter {

    private String securityToken = null;

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
            String loginData = doSoapPost(loginRequestUrl(), loginRequestXml(), null);
            loginResponseHandle(loginData);
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
            String contactsData = doSoapPost(contactsRequestUrl(), contactsRequestXml(), contactsRequestAction());

            return contactsResponseHandle(contactsData);
        } catch (Exception e) {
            throw new ContactsException("Hotmail protocol has changed", e);
        }
    }

    private String loginRequestXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml += "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2003/06/secext\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\" xmlns:wssc=\"http://schemas.xmlsoap.org/ws/2004/04/sc\" xmlns:wst=\"http://schemas.xmlsoap.org/ws/2004/04/trust\">";
        xml += "<Header>";
        xml += "<ps:AuthInfo xmlns:ps=\"http://schemas.microsoft.com/Passport/SoapServices/PPCRL\" Id=\"PPAuthInfo\">";
        xml += "<ps:HostingApp>{3:B}</ps:HostingApp>";
        xml += "<ps:BinaryVersion>4</ps:BinaryVersion>";
        xml += "<ps:UIVersion>1</ps:UIVersion>";
        xml += "<ps:Cookies></ps:Cookies>";
        xml += "<ps:RequestParams>AQAAAAIAAABsYwQAAAAzMDg0</ps:RequestParams>";
        xml += "</ps:AuthInfo>";
        xml += "<wsse:Security>";
        xml += "<wsse:UsernameToken Id=\"user\">";
        xml += "<wsse:Username>" + email + "</wsse:Username>";
        xml += "<wsse:Password>" + password + "</wsse:Password>";
        xml += "</wsse:UsernameToken>";
        xml += "</wsse:Security>";
        xml += "</Header>";
        xml += "<Body>";
        xml += "<ps:RequestMultipleSecurityTokens xmlns:ps=\"http://schemas.microsoft.com/Passport/SoapServices/PPCRL\" Id=\"RSTS\">";
        xml += "<wst:RequestSecurityToken Id=\"RST0\">";
        xml += "<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>";
        xml += "<wsp:AppliesTo>";
        xml += "<wsa:EndpointReference>";
        xml += "<wsa:Address>http://Passport.NET/tb</wsa:Address>";
        xml += "</wsa:EndpointReference>";
        xml += "</wsp:AppliesTo>";
        xml += "</wst:RequestSecurityToken>";
        xml += "<wst:RequestSecurityToken Id=\"RST1\">";
        xml += "<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>";
        xml += "<wsp:AppliesTo>";
        xml += "<wsa:EndpointReference>";
        xml += "<wsa:Address>contacts.msn.com</wsa:Address>";
        xml += "</wsa:EndpointReference>";
        xml += "</wsp:AppliesTo>";
        xml += "<wsse:PolicyReference URI=\"MBI\">";
        xml += "</wsse:PolicyReference>";
        xml += "</wst:RequestSecurityToken>";
        xml += "<wst:RequestSecurityToken Id=\"RST2\">";
        xml += "<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>";
        xml += "<wsp:AppliesTo>";
        xml += "<wsa:EndpointReference>";
        xml += "<wsa:Address>storage.msn.com</wsa:Address>";
        xml += "</wsa:EndpointReference>";
        xml += "</wsp:AppliesTo>";
        xml += "<wsse:PolicyReference URI=\"MBI\">";
        xml += "</wsse:PolicyReference>";
        xml += "</wst:RequestSecurityToken>";
        xml += "</ps:RequestMultipleSecurityTokens>";
        xml += "</Body>";
        xml += "</Envelope>";
        return xml;
    }

    private String loginRequestUrl() {
        String url = "";
        if (email.indexOf("@msn.com") == -1) {
            url = "https://login.live.com/RST.srf";
        } else {
            url = "https://msnia.login.live.com/pp650/RST.srf";
        }
        return url;
    }

    private void loginResponseHandle(String data) throws Exception {
        if (data.indexOf("FailedAuthentication") >= 0) {
            throw new ContactsException("failed authentication");
        }

        if (data.indexOf("<wsse:BinarySecurityToken") < 1) {
            throw new ContactsException("failed authentication");
        }

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(data));

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("BinarySecurityToken")) {
                if (xpp.getAttributeValue(null, "Id").equals("Compact1")) {
                    xpp.next();
                    securityToken = xpp.getText().replace("&", "&amp;");
                }
            }
            xpp.next();
            eventType = xpp.getEventType();
        }
    }

    private String contactsRequestXml() {
        String xml = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'\n";
        xml += "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n";
        xml += "xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n";
        xml += "xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'>\n";
        xml += "<soap:Header>\n";
        xml += "<ABApplicationHeader xmlns='http://www.msn.com/webservices/AddressBook'>\n";
        xml += "<ApplicationId>CFE80F9D-180F-4399-82AB-413F33A1FA11</ApplicationId>\n";
        xml += "<IsMigration>false</IsMigration>\n";
        xml += "<PartnerScenario>Initial</PartnerScenario>\n";
        xml += "</ABApplicationHeader>\n";
        xml += "<ABAuthHeader xmlns='http://www.msn.com/webservices/AddressBook'>\n";
        xml += "<ManagedGroupRequest>false</ManagedGroupRequest>\n";
        xml += "<TicketToken>" + securityToken + "</TicketToken>\n";
        xml += "</ABAuthHeader>\n";
        xml += "</soap:Header>\n";
        xml += "<soap:Body>\n";
        xml += "<ABFindAll xmlns='http://www.msn.com/webservices/AddressBook'>\n";
        xml += "<abId>00000000-0000-0000-0000-000000000000</abId>\n";
        xml += "<abView>Full</abView>\n";
        xml += "<deltasOnly>false</deltasOnly>\n";
        xml += "<lastChange>0001-01-01T00:00:00.0000000-08:00</lastChange>\n";
        xml += "</ABFindAll>\n";
        xml += "</soap:Body>";
        xml += "</soap:Envelope>";

        return xml;
    }

    private String contactsRequestUrl() {
        return "http://contacts.msn.com/abservice/abservice.asmx";
    }

    private String contactsRequestAction() {
        return "http://www.msn.com/webservices/AddressBook/ABFindAll";
    }

    private List<Contact> contactsResponseHandle(String data) throws Exception  {
        List<Contact> contacts = new ArrayList<Contact>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(data));

        int eventType = xpp.getEventType();

        String username = null;
        String email = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && "Contact".equals(xpp.getName())) {
                while (true) {
                    if (eventType == XmlPullParser.END_TAG && "Contact".equals(xpp.getName())) {
                        break;
                    }

                    if (eventType == XmlPullParser.START_TAG && "ContactEmail".equals(xpp.getName())) {
                        while (true) {
                            if (eventType == XmlPullParser.END_TAG && "ContactEmail".equals(xpp.getName())) {
                                break;
                            }

                            if (eventType == XmlPullParser.START_TAG && "email".equals(xpp.getName())) {
                                xpp.next();
                                email = xpp.getText();
                            }
                            xpp.next();
                            eventType = xpp.getEventType();
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG && "passportName".equals(xpp.getName())) {
                        xpp.next();
                        email = xpp.getText();
                    }

                    if (eventType == XmlPullParser.START_TAG && "displayName".equals(xpp.getName())) {
                        xpp.next();
                        username = xpp.getText();

                        Contact contact = new Contact(username, email);
                        contacts.add(contact);
                    }

                    xpp.next();
                    eventType = xpp.getEventType();
                }
            }
            xpp.next();
            eventType = xpp.getEventType();
        }

        return contacts;
    }

}

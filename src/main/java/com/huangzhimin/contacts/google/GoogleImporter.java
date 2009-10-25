package com.huangzhimin.contacts.google;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.util.AuthenticationException;
import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.ContactsImporter;
import com.huangzhimin.contacts.exception.ContactsException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author flyerhzm
 */
public class GoogleImporter implements ContactsImporter {

	// 用户名
	private String email;

	// 密码
	private String password;

    public GoogleImporter(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public List<Contact> getContacts() throws ContactsException {
        ContactsService service = new ContactsService("contactlist");
        try {
            service.setUserCredentials(email, password);
        } catch (AuthenticationException e) {
            throw new ContactsException("login failed", e);
        }
        try {
            URL feedUrl = new URL("http://www.google.com/m8/feeds/contacts/" + email + "/full");
            Query query = new Query(feedUrl);
            query.setMaxResults(Integer.MAX_VALUE);
            ContactFeed resultFeed = service.query(query, ContactFeed.class);
            List<Contact> contacts = new ArrayList<Contact>();
            for (ContactEntry entry : resultFeed.getEntries()) {
                for (Email email : entry.getEmailAddresses()) {
                    String address = email.getAddress();
                    String name = null;
                    if (entry.hasName()) {
                        name = entry.getName().getFullName().getValue();
                    } else {
                        name = getUsername(address);
                    }
                    contacts.add(new Contact(name, address));
                }
            }
            return contacts;
        } catch (Exception e) {
			throw new ContactsException("gmail protocol has changed", e);
        }
    }

    private String getUsername(String email) {
        return email.split("@")[0];
    }

}

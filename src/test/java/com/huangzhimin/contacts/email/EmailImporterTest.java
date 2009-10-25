/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.huangzhimin.contacts.email;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.ContactsImporter;
import com.huangzhimin.contacts.exception.ContactsException;
import com.huangzhimin.contacts.google.GoogleImporter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author flyerhzm
 */
public class EmailImporterTest {

    private static Properties props = new Properties();

    public EmailImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            InputStream in = new FileInputStream(path + "/email.properties");
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void gmail() {
		ContactsImporter importer = new GoogleImporter(props.getProperty("gmail.username"), props.getProperty("gmail.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("gmail contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			fail("gmail contacts get failed. " + e.getMessage());
		}
    }

    @Test
    public void hotmail() {
		ContactsImporter importer = new HotmailImporter(props.getProperty("hotmail.username"), props.getProperty("hotmail.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("hotmail contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
            e.printStackTrace();
			fail("hotmail contacts get failed. ");
		}
    }

    @Test
    public void livecn() {
		ContactsImporter importer = new HotmailImporter(props.getProperty("livecn.username"), props.getProperty("livecn.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("livecn contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
            e.printStackTrace();
			fail("livecn contacts get failed. ");
		}
    }

    @Test
    public void onesixthree() {
		ContactsImporter importer = new OneSixThreeImporter(props.getProperty("onesixthree.username"), props.getProperty("onesixthree.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("163 contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("163 contacts get failed. ");
		}
    }

    @Test
    public void onetwosix() {
		ContactsImporter importer = new OneTwoSixImporter(props.getProperty("onetwosix.username"), props.getProperty("onetwosix.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("126 contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("126 contacts get failed. ");
		}
    }

    @Test
    public void sina() {
		ContactsImporter importer = new SinaImporter(props.getProperty("sina.username"), props.getProperty("sina.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("sina contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("sina contacts get failed. ");
		}
    }

    @Test
    public void sohu() {
		ContactsImporter importer = new SohuImporter(props.getProperty("sohu.username"), props.getProperty("sohu.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("sohu contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("sohu contacts get failed. ");
		}
    }

    @Test
    public void tom() {
		ContactsImporter importer = new TomImporter(props.getProperty("tom.username"), props.getProperty("tom.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("tom contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("tom contacts get failed. ");
		}
    }

    @Test
    public void yahoo() {
		ContactsImporter importer = new YahooImporter(props.getProperty("yahoo.username"), props.getProperty("yahoo.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("yahoo contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("yahoo contacts get failed. ");
		}
    }

    @Test
    public void yahoocn() {
		ContactsImporter importer = new YahooImporter(props.getProperty("yahoocn.username"), props.getProperty("yahoocn.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("yahoocn contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("yahoocn contacts get failed. ");
		}
    }

    @Test
    public void yahoocomcn() {
		ContactsImporter importer = new YahooImporter(props.getProperty("yahoocomcn.username"), props.getProperty("yahoocomcn.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("yahoocomcn contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("yahoocomcn contacts get failed. ");
		}
    }

    @Test
    public void yeah() {
		ContactsImporter importer = new YeahImporter(props.getProperty("yeah.username"), props.getProperty("yeah.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("yeah contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("yeah contacts get failed. ");
		}
    }

    @Test
    public void oneeightnine() {
		ContactsImporter importer = new OneEightNineImporter(props.getProperty("oneeightnine.username"), props.getProperty("oneeightnine.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("189 contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("189 contacts get failed. ");
		}
    }

    @Test
    public void onethreenine() {
		ContactsImporter importer = new OneThreeNineImporter(props.getProperty("onethreenine.username"), props.getProperty("onethreenine.password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("139 contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
			e.printStackTrace();
			fail("139 contacts get failed. ");
		}
    }

}
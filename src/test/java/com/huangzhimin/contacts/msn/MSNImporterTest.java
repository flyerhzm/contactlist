/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.huangzhimin.contacts.msn;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.ContactsImporter;
import com.huangzhimin.contacts.exception.ContactsException;
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
public class MSNImporterTest {

    private static Properties props = new Properties();

    public MSNImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            InputStream in = new FileInputStream(path + "/msn.properties");
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void msn() {
		ContactsImporter importer = new MSNImporter(props.getProperty("username"), props.getProperty("password"));
		try {
			List<Contact> contacts = importer.getContacts();
			assertTrue("msn contacts are empty", contacts.size() > 0);
		} catch (ContactsException e) {
            e.printStackTrace();
			fail("msn contacts get failed. ");
		}
    }

}
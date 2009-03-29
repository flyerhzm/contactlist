/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.huangzhimin.contacts.utils;

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
public class UnicodeChineseTest {

    public UnicodeChineseTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    public void getByUnicode() {
        assertEquals("黄", UnicodeChinese.getByUnicode("&#40644;"));
    }

    @Test
    public void transform() {
		assertEquals("黄 志敏", UnicodeChinese.transform("&#40644; &#24535;&#25935;"));
    }

}
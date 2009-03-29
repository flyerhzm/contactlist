package com.huangzhimin.contacts;

import java.util.List;

import com.huangzhimin.contacts.exception.ContactsException;



/**
 * 导入联系人的接口
 * 
 * @author flyerhzm
 * 
 */
public interface ContactsImporter {
	public List<Contact> getContacts() throws ContactsException;
}

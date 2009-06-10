package com.huangzhimin.contacts.msn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.huangzhimin.contacts.Contact;
import com.huangzhimin.contacts.ContactsImporter;
import com.huangzhimin.contacts.exception.ContactsException;


import rath.msnm.BuddyList;
import rath.msnm.MSNMessenger;
import rath.msnm.NotificationProcessor;
import rath.msnm.UserStatus;
import rath.msnm.entity.MsnFriend;

/**
 * 导入MSN联系人
 * 
 * @author flyerhzm
 * 
 */
public class MSNImporter implements ContactsImporter {

	// 用户名
	private String username;

	// 密码
	private String password;

	private MSNMessenger msn;

	/**
	 * 构造函数
	 * 
	 * @param username
	 * @param password
	 */
	public MSNImporter(String username, String password) {
		this.username = username;
		this.password = password;
		msn = new MSNMessenger();
	}

	/**
	 * 获取MSN联系人列表
	 * 
	 * return MSN联系人列表
	 * 
	 * @throws ContactsException
	 */
	public List<Contact> getContacts() throws ContactsException {
		try {
			login();
			List<Contact> contacts = new ArrayList<Contact>();
			BuddyList list = msn.getBuddyGroup().getAllowList();
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				MsnFriend friend = (MsnFriend) iter.next();
				contacts.add(new Contact(new String(friend.getFriendlyName()
						.getBytes(), "UTF-8"), friend.getLoginName()));
			}
			logout();
			return contacts;
		} catch (Exception e) {
			throw new ContactsException("msn protocol has changed", e);
		}
	}

	/**
	 * 登录msn
	 */
	private void login() {
		msn.setInitialStatus(UserStatus.OFFLINE);
		msn.login(username, password);
	}

	/**
	 * 登出msn
	 */
	private void logout() {
		fixedLogout(msn);
	}

	// 修正后的logout:
	public void fixedLogout(MSNMessenger messenger) {
		if (messenger != null) {
			Thread leakedThread = null;
			try {
				leakedThread = getLeakedThread(messenger);
				messenger.logout();
			} catch (Exception ignore) {

			} finally {
				if (leakedThread != null) {
					if (!leakedThread.isInterrupted()) {
						leakedThread.interrupt();
					}
				}
			}
		}
	}

	/**
	 * current MSNMessenger do not terminate internal callback thread if
	 * messenger not logined.
	 */
	private Thread getLeakedThread(MSNMessenger messenger) {
		try {
			Field nsField = MSNMessenger.class.getDeclaredField("ns");
			nsField.setAccessible(true);
			NotificationProcessor ns = (NotificationProcessor) nsField
					.get(messenger);
			if (ns == null)
				return null;
			Field callbackField = NotificationProcessor.class
					.getDeclaredField("callbackCleaner");
			callbackField.setAccessible(true);
			return (Thread) callbackField.get(ns);
		} catch (SecurityException e) {
			throw new RuntimeException("unexpected", e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("unexpected", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("unexpected", e);
		}
	}
}

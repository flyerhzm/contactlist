package com.huangzhimin.contacts;

import com.huangzhimin.contacts.email.GmailImporter;
import com.huangzhimin.contacts.email.HotmailImporter;
import com.huangzhimin.contacts.email.OneSixThreeImporter;
import com.huangzhimin.contacts.email.OneTwoSixImporter;
import com.huangzhimin.contacts.email.SinaImporter;
import com.huangzhimin.contacts.email.SohuImporter;
import com.huangzhimin.contacts.email.TomImporter;
import com.huangzhimin.contacts.email.YahooImporter;
import com.huangzhimin.contacts.email.YeahImporter;
import com.huangzhimin.contacts.msn.MSNImporter;

/**
 * ContactsImporter工厂类
 * 
 * @author flyerhzm
 * 
 */
public class ContactsImporterFactory {

	/**
	 * 获取Hotmail Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return Hotmail Importer实例
	 */
	public static ContactsImporter getHotmailContacts(String email,
			String password) {
		return new HotmailImporter(email, password);
	}

	/**
	 * 获取Gmail Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return Gmail Importer实例
	 */
	public static ContactsImporter getGmailContacts(String email,
			String password) {
		return new GmailImporter(email, password);
	}

	/**
	 * 获取Yahoo Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return Yahoo Importer实例
	 */
	public static ContactsImporter getYahooContacts(String email,
			String password) {
		return new YahooImporter(email, password);
	}

	/**
	 * 获取163 Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return 163 Importer实例
	 */
	public static ContactsImporter getOneSixThreeContacts(String email,
			String password) {
		return new OneSixThreeImporter(email, password);
	}

	/**
	 * 获取126 Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return 126 Importer实例
	 */
	public static ContactsImporter getOneTwoSixContacts(String email,
			String password) {
		return new OneTwoSixImporter(email, password);
	}

	/**
	 * 获取sina Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return sina Importer实例
	 */
	public static ContactsImporter getSinaContacts(String email, String password) {
		return new SinaImporter(email, password);
	}

	/**
	 * 获取sohu Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return sohu Importer实例
	 */
	public static ContactsImporter getSohuContacts(String email, String password) {
		return new SohuImporter(email, password);
	}

	/**
	 * 获取tom Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return tom Importer实例
	 */
	public static ContactsImporter getTomContacts(String email, String password) {
		return new TomImporter(email, password);
	}

	/**
	 * 获取yeah Importer实例
	 * 
	 * @param email
	 *            email地址
	 * @param password
	 *            密码
	 * @return yeah Importer实例
	 */
	public static ContactsImporter getYeahContacts(String email, String password) {
		return new YeahImporter(email, password);
	}

	/**
	 * 获取MSN Importer实例
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return MSN Importer实例
	 */
	public static ContactsImporter getMSNContacts(String username,
			String password) {		
		return new MSNImporter(username, password);
	}
}

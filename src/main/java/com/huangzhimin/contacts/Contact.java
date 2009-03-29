package com.huangzhimin.contacts;

/**
 * 联系人，包括用户名和邮箱地址
 * 
 * @author flyerhzm
 *
 */
public class Contact {
  static final long serialVersionUID = 1226317456890l;
  private String username;
  private String email;

  public Contact() {}

  public Contact(Contact model) {
    this.username = model.username;
    this.email = model.email;
  }

  public Contact(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public void setUsername(String username) {
    this.username = username;
  }
  public String getUsername() {
    return this.username;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getEmail() {
    return this.email;
  }
}

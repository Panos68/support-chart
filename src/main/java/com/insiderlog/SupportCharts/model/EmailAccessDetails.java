package com.insiderlog.SupportCharts.model;

public class EmailAccessDetails {
  private final String password;
  private final String host;
  private final String mailStoreType;
  private final String username;

  public EmailAccessDetails(String password, String host, String mailStoreType, String username) {
    this.password = password;
    this.host = host;
    this.mailStoreType = mailStoreType;
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public String getHost() {
    return host;
  }

  public String getMailStoreType() {
    return mailStoreType;
  }

  public String getUsername() {
    return username;
  }
}

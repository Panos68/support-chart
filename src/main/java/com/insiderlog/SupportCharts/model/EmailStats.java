package com.insiderlog.SupportCharts.model;

import com.insiderlog.SupportCharts.enums.Type;

import java.util.HashMap;

public class EmailStats {
  private final HashMap<Integer, Integer> emailsSent = new HashMap<>();
  private Type type;

  public EmailStats(Type type) {
    this.type = type;
    for (int i = 1; i <= 12; i++) {
      emailsSent.put(i, 0);
    }
  }

  public EmailStats() {
  }

  public HashMap<Integer, Integer> getEmailsSent() {
    return emailsSent;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}

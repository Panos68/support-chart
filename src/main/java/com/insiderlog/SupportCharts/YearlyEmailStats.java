package com.insiderlog.SupportCharts;

public class YearlyEmailStats extends EmailStats {
  private int year;

  public YearlyEmailStats(int year, Type type) {
    this.year = year;
    super.setType(type);
    for (int i = 1; i <= 12; i++) {
      super.getEmailsSent().put(i, 0);
    }
  }

  public YearlyEmailStats() {
  }

  public int getYear() {
    return year;
  }
}

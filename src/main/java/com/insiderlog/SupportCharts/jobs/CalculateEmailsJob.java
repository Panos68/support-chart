package com.insiderlog.SupportCharts.jobs;

import com.insiderlog.SupportCharts.services.SupportChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CalculateEmailsJob {
  private final SupportChartService supportChartService;

  @Autowired
  public CalculateEmailsJob(SupportChartService supportChartService) {
    this.supportChartService = supportChartService;
  }

  @Scheduled(cron = "0 0 9 * * ?")
  public void calculateEmails() {
    supportChartService.calculateToJsonFile();
  }
}

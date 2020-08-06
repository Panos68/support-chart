package com.insiderlog.SupportCharts.controllers;

import com.insiderlog.SupportCharts.EmailStats;
import com.insiderlog.SupportCharts.YearlyEmailStats;
import com.insiderlog.SupportCharts.services.SupportChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/supportChart")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SupportChartController {

  private final SupportChartService supportChartService;

  @Autowired
  public SupportChartController(SupportChartService supportChartService) {
    this.supportChartService = supportChartService;
  }

  @PostMapping()
  @ResponseBody
  public ResponseEntity.BodyBuilder calculateEmails() {
    supportChartService.calculateToJsonFile();

    return ResponseEntity.ok();
  }

  @GetMapping(value = "/yearly")
  @ResponseBody
  public ResponseEntity<List<YearlyEmailStats>> getYearlyEmailsStats() throws IOException {

    return ResponseEntity.ok(supportChartService.getYearlyEmailStats());
  }

  @GetMapping(value = "/total")
  @ResponseBody
  public ResponseEntity<List<EmailStats>> getTotalEmailsStats() throws IOException {

    return ResponseEntity.ok(supportChartService.getTotalEmailStats());
  }
}

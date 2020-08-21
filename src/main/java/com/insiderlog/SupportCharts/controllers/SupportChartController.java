package com.insiderlog.SupportCharts.controllers;

import com.insiderlog.SupportCharts.model.EmailStats;
import com.insiderlog.SupportCharts.model.YearlyEmailStats;
import com.insiderlog.SupportCharts.services.SupportChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/api/supportChart")
public class SupportChartController {

  private final SupportChartService supportChartService;

  @Autowired
  public SupportChartController(SupportChartService supportChartService) {
    this.supportChartService = supportChartService;
  }

  @PostMapping(value = "/calculate/{reset}")
  @ResponseBody
  public ResponseEntity.BodyBuilder calculateEmails(@PathVariable boolean reset) {
    supportChartService.calculateToJsonFile(reset);

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

  @GetMapping(value = "/login")
  @ResponseBody
  public ResponseEntity<Boolean> getIsPinCorrect(@RequestParam String givenPin) throws IOException {

    return ResponseEntity.ok(supportChartService.isPinCorrect(givenPin));
  }
}

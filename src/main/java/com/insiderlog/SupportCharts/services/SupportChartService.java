package com.insiderlog.SupportCharts.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insiderlog.SupportCharts.model.EmailAccessDetails;
import com.insiderlog.SupportCharts.model.EmailStats;
import com.insiderlog.SupportCharts.enums.Type;
import com.insiderlog.SupportCharts.model.YearlyEmailStats;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class SupportChartService {
  private final List<YearlyEmailStats> yearlyEmailStats = new ArrayList<>();
  private final List<EmailStats> emailStats = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Value("${password}")
  private String password;

  public void calculateToJsonFile() {
    EmailAccessDetails seAccessDetails = new EmailAccessDetails(password, "imap.one.com",
      "imap", "support@insiderlog.se");
    calculateEmails(seAccessDetails, Type.SE);

    EmailAccessDetails comAccessDetails = new EmailAccessDetails(password, "imap.one.com",
      "imap", "support@insiderlog.com");
    calculateEmails(comAccessDetails, Type.COM);

    try {
      writeJSONFile("yearlySupportEmailNumbers.json", objectMapper.writeValueAsString(yearlyEmailStats));
      writeJSONFile("totalSupportEmails.json", objectMapper.writeValueAsString(emailStats));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  private void writeJSONFile(String fileName, String jsonString) {
    try (FileWriter file = new FileWriter(fileName)) {
      file.write(jsonString);
      file.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void calculateEmails(EmailAccessDetails emailAccessDetails, Type type) {
    try {
      Properties properties = new Properties();
      MailSSLSocketFactory sf = new MailSSLSocketFactory();
      sf.setTrustAllHosts(true);
      properties.put("mail.imap.ssl.trust", "*");
      properties.put("mail.imap.ssl.socketFactory", sf);
      properties.put("mail.imap.com", emailAccessDetails.getHost());
      properties.put("mail.imap.starttls.enable", "true");
      properties.put("mail.imap.auth", "true");  // If you need to authenticate

      // Use the following if you need SSL
      properties.put("mail.imap.socketFactory.port", 993);
      properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      properties.put("mail.imap.socketFactory.fallback", "false");

      Session emailSession = Session.getDefaultInstance(properties);
      emailSession.setDebug(true);

      //2) create the IMAP store object and connect with the Imap server
      IMAPStore emailStore = (IMAPStore) emailSession.getStore(emailAccessDetails.getMailStoreType());

      emailStore.connect(emailAccessDetails.getHost(), emailAccessDetails.getUsername(), emailAccessDetails.getPassword());

      //3) create the folder object and open it
      Folder emailFolder = emailStore.getFolder("Inbox");
      emailFolder.open(Folder.READ_ONLY);

      //4) retrieve the messages from the folder in an array and print it
      Message[] messages = emailFolder.getMessages();
      for (Message message : messages) {
        Calendar cal = Calendar.getInstance();
        if (message.getReceivedDate() != null) {
          cal.setTime(message.getReceivedDate());
          int month = cal.get(Calendar.MONTH);
          int year = cal.get(Calendar.YEAR);
          fillEmailHashMaps(month, year, type);
          fillEmailHashMaps(month, year, Type.TOTAL);
        }
      }

    } catch (GeneralSecurityException | MessagingException e) {
      e.printStackTrace();
    }
  }

  private void fillEmailHashMaps(int month, int year, Type type) {
    Optional<YearlyEmailStats> optionalYearlyEmailStats = this.yearlyEmailStats.stream().filter(e -> e.getType().equals(type) && e.getYear() == year).findFirst();
    YearlyEmailStats yearlyEmailStat = optionalYearlyEmailStats.orElseGet(() -> new YearlyEmailStats(year, type));
    yearlyEmailStat.getEmailsSent().merge(month + 1, 1, Integer::sum);
    if (!optionalYearlyEmailStats.isPresent()) {
      yearlyEmailStats.add(yearlyEmailStat);
    }
    Optional<EmailStats> optionalEmailStats = this.emailStats.stream().filter(e -> e.getType().equals(type)).findFirst();
    EmailStats emailStat = optionalEmailStats.orElseGet(() -> new EmailStats(type));
    emailStat.getEmailsSent().merge(month + 1, 1, Integer::sum);
    if (!optionalEmailStats.isPresent()) {
      emailStats.add(emailStat);
    }
  }

  public List<EmailStats> getTotalEmailStats() throws IOException {
    File file = new File("totalSupportEmails.json");

    return objectMapper.readValue(file, new TypeReference<List<EmailStats>>() {
    });
  }

  public List<YearlyEmailStats> getYearlyEmailStats() throws IOException {
    File file = new File("yearlySupportEmailNumbers.json");

    return objectMapper.readValue(file, new TypeReference<List<YearlyEmailStats>>() {
    });
  }
}

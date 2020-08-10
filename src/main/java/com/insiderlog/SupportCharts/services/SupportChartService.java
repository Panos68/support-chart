package com.insiderlog.SupportCharts.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insiderlog.SupportCharts.enums.Type;
import com.insiderlog.SupportCharts.model.EmailAccessDetails;
import com.insiderlog.SupportCharts.model.EmailStats;
import com.insiderlog.SupportCharts.model.YearlyEmailStats;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SupportChartService {
  private List<YearlyEmailStats> yearlyEmailStats = new ArrayList<>();
  private List<EmailStats> emailStats = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Date date;
  @Value("${password}")
  private String password;

  public void calculateToJsonFile(boolean reset) {
    if (!reset) {
      initializeLists();
    }

    EmailAccessDetails seAccessDetails = new EmailAccessDetails(password, "imap.one.com",
      "imap", "support@insiderlog.se");
    calculateEmails(seAccessDetails, Type.SE);

    EmailAccessDetails comAccessDetails = new EmailAccessDetails(password, "imap.one.com",
      "imap", "support@insiderlog.com");
    calculateEmails(comAccessDetails, Type.COM);


    try {
      writeJSONFile("lastCalculatedTime.json", new Date().toString());
      writeJSONFile("yearlySupportEmailNumbers.json", objectMapper.writeValueAsString(yearlyEmailStats));
      writeJSONFile("totalSupportEmails.json", objectMapper.writeValueAsString(emailStats));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  private void initializeLists() {
    try {
      InputStream input = new FileInputStream("lastCalculatedTime.json");

      //get latest updated time
      String result = IOUtils.toString(input, StandardCharsets.UTF_8);
      SimpleDateFormat formatter = new SimpleDateFormat("EE MMM dd HH:mm:ss zzzz yyyy", Locale.US);
      date = formatter.parse(result);

      input = new FileInputStream("yearlySupportEmailNumbers.json");
      yearlyEmailStats = objectMapper.readValue(input, new TypeReference<List<YearlyEmailStats>>() {
      });

      input = new FileInputStream("totalSupportEmails.json");
      emailStats = objectMapper.readValue(input, new TypeReference<List<EmailStats>>() {
        }
      );
    } catch (IOException | ParseException e) {
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

      properties.put("mail.imap.socketFactory.port", 993);
      properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      properties.put("mail.imap.socketFactory.fallback", "false");

      Session emailSession = Session.getDefaultInstance(properties);
      emailSession.setDebug(true);

      IMAPStore emailStore = (IMAPStore) emailSession.getStore(emailAccessDetails.getMailStoreType());

      emailStore.connect(emailAccessDetails.getHost(), emailAccessDetails.getUsername(), emailAccessDetails.getPassword());

      Folder emailFolder = emailStore.getFolder("Inbox");
      emailFolder.open(Folder.READ_ONLY);

      List<Message> messages = Arrays.asList(emailFolder.getMessages());
      messages = filterMessagesToNewReceived(emailFolder, messages);

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

  private List<Message> filterMessagesToNewReceived(Folder emailFolder, List<Message> messages) throws MessagingException {
    if (date != null) {
      Stream<Message> messageStream = Arrays.stream(emailFolder.getMessages()).filter(message -> {
        try {
          return message.getReceivedDate().after(date);
        } catch (MessagingException e) {
          e.printStackTrace();
        }
        return false;
      });
      messages = messageStream.collect(Collectors.toList());
    }
    return messages;
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

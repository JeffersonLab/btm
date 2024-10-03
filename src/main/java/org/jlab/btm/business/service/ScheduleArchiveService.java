package org.jlab.btm.business.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.smoothness.business.exception.UserFriendlyException;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ScheduleArchiveService {

  private static final String SCHEDULE_DIR = "schedules";

  private static final Logger logger = Logger.getLogger(ScheduleArchiveService.class.getName());

  @PermitAll
  public void publish(MonthlySchedule schedule, String html) throws UserFriendlyException {
    String path =
        System.getProperty("user.home") + System.getProperty("file.separator") + SCHEDULE_DIR;

    File dir = new File(path);
    dir.mkdir();

    if (!dir.exists()) {
      throw new UserFriendlyException("Unable to access directory to store archived schedules");
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM");

    String filename =
        formatter.format(schedule.getStartDay()).toLowerCase()
            + "-v"
            + schedule.getVersion()
            + ".html";

    File file = new File(path, filename);

    Writer writer = null;

    logger.log(Level.FINEST, "Writing file to: {0}", file);

    try {
      writer =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
      writer.write(html);
    } catch (IOException e) {
      throw new UserFriendlyException("Unable to write schedule to a file", e);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Unable to close schedule file writer", e);
        }
      }
    }
  }
}

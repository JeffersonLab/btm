package org.jlab.btm.business.service;

import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.jlog.exception.LogCertificateException;
import org.jlab.jlog.exception.LogIOException;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.*;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LogbookService {

    private final static Logger logger
            = Logger.getLogger(LogbookService.class.getName());

    @Resource
    private SessionContext context;

    @PermitAll
    public String checkAuthorized() {
        String username = context.getCallerPrincipal().getName();
        if (username == null || username.isEmpty() || username.equalsIgnoreCase("ANONYMOUS")) {
            throw new EJBAccessException(
                    "You must be authenticated to perform the requested operation.  Your session may have expired.  Please re-login.");
        }

        return username;
    }

    @PermitAll
    public long createCrewChiefTimesheetEntry(String subject, String html) throws IOException,
            LogIOException, LogCertificateException {

        String username = checkAuthorized(); /*Must be logged in*/

        String logbookHostname = System.getenv("LOGBOOK_HOSTNAME");

        String logbooks = System.getenv("LOGBOOK_OPS_BOOKS_CSV");

        if (logbookHostname == null) {
            throw new IOException("logbook server not configured");
        }

        if (logbooks == null) {
            throw new IOException("logbooks not configured");
        }

        Properties config = Library.getConfiguration();

        config.setProperty("SUBMIT_URL", "https://" + logbookHostname + "/incoming");

        logger.log(Level.INFO, "Sending elog to logbook: {}", logbooks);

        LogEntry entry = new LogEntry(subject, logbooks);

        entry.setBody(html, Body.ContentType.HTML);

        entry.setTags("BeamAccounting");

        LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
        extension.setAuthor(username);

        return entry.submitNow();
    }
}

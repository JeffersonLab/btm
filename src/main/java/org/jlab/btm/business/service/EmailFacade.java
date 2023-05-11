package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.Staff;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
@Stateless
public class EmailFacade extends AbstractService<Staff> {

    @Resource(name = "mail/jlab")
    private Session mailSession;

    public EmailFacade() {
        super(Staff.class);
    }

    @PermitAll
    public void sendHTMLEmail(Address[] toAddresses, String subject, String html) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress("btm@jlab.org"));

        message.setRecipients(Message.RecipientType.TO, toAddresses);
        message.setSubject(subject);

        message.setContent(html, "text/html; charset=UTF-8");

        message.saveChanges();

        Transport tr = mailSession.getTransport();
        tr.connect();
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    private void sendPlainTextEmail(Address from, Address[] toAddresses, String subject, String body) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(from);

        message.setRecipients(Message.RecipientType.TO, toAddresses);
        message.setSubject(subject);

        message.setText(body, "UTF-8");

        message.saveChanges();

        Transport tr = mailSession.getTransport();
        tr.connect();
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    @PermitAll
    public Address[] getFeedbackRecipients() throws AddressException {
        List<Address> addressList = new ArrayList<>();

        String recipientListStr = "ryans@jlab.org";

        if (recipientListStr != null && !recipientListStr.isEmpty()) {
            String[] tokenArray = recipientListStr.split(",");

            for (String token : tokenArray) {
                if (!token.trim().isEmpty()) {
                    Address address = new InternetAddress(token.trim());
                    addressList.add(address);
                }
            }
        }

        return addressList.toArray(new Address[]{});
    }

    @PermitAll
    public void sendFeedbackEmail(String subject, String body) {
        String username = checkAuthenticated();

        try {
            Address fromAddress = new InternetAddress(username + "@jlab.org");

            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("subject must not be empty");
            }

            if (body == null || body.isEmpty()) {
                throw new IllegalArgumentException("message must not be empty");
            }


            subject = "BTM Feedback: " + subject;
            Address[] toAddresses = getFeedbackRecipients();

            sendPlainTextEmail(fromAddress, toAddresses, subject, body);
        } catch (AddressException e) {
            throw new IllegalArgumentException("Invalid address", e);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Unable to send email", e);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return null;
    }
}

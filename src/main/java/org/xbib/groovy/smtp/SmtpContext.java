package org.xbib.groovy.smtp;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.Properties;

/**
 */
public class SmtpContext {

    Properties properties;

    Session session;

    void close() throws MessagingException {
    }
}

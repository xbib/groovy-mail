package org.xbib.groovy.imap;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class ImapContext {

    Properties properties;

    Session session;

    Store store;

    void close() throws MessagingException {
        if (store != null) {
            store.close();
        }
    }
}

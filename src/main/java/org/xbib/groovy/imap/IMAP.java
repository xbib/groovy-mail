package org.xbib.groovy.imap;

import groovy.lang.Closure;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Properties;

/**
 * A wrapper class for IMAP functionality to Groovy.
 */
public class IMAP {

    private static final String DEFAULT_URL = "imap://localhost:143/";

    private final String url;

    private final String username;

    private final String password;

    private IMAP(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static IMAP newInstance() {
        return new IMAP(DEFAULT_URL, null, null);
    }

    public static IMAP newInstance(String url) {
        return new IMAP(url, null, null);
    }

    public static IMAP newInstance(String url, String username, String password) {
        return new IMAP(url, username, password);
    }

    public Boolean exist(String folderName) throws Exception {
        WithContext<Boolean> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            return folder.exists();
        };
        return performWithContext(action);
    }

    public void eachFolder(String folderName, String folderPattern, Closure closure) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.open(Folder.READ_ONLY);
                Folder[] folders = folder.list(folderPattern);
                for (Folder f : folders) {
                    closure.call(f);
                }
                folder.close(false);
            }
            return null;
        };
        performWithContext(action);
    }

    public void create(String folderName) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (!folder.exists()) {
                folder.create(Folder.HOLDS_MESSAGES | Folder.READ_WRITE);
            }
            folder.close(false);
            return null;
        };
        performWithContext(action);
    }

    public void delete(String folderName) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.delete(true);
            }
            folder.close(true);
            return null;
        };
        performWithContext(action);
    }

    public void expunge(String folderName) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.expunge();
            }
            folder.close(false);
            return null;
        };
        performWithContext(action);
    }

    public Integer messageCount(String folderName) throws Exception {
        WithContext<Integer> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                return folder.getMessageCount();
            }
            return null;
        };
        return performWithContext(action);
    }

    public void eachMessage(String folderName, Closure closure) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                for (Message message : messages) {
                    closure.call(message);
                }
                folder.close(false);
            }
            return null;
        };
        performWithContext(action);
    }

    public void eachMessage(String folderName, int start, int end, Closure closure) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages(start, end);
                for (Message message : messages) {
                    closure.call(message);
                }
                folder.close(false);
            }
            return null;
        };
        performWithContext(action);
    }

    public void eachSearchedMessage(String folderName, Flags flags, Closure closure) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.open(Folder.READ_ONLY);
                FlagTerm flagTerm = new FlagTerm(flags, false);
                Message[] messages = folder.search(flagTerm);
                for (Message message : messages) {
                    closure.call(message);
                }
                folder.close(false);
            }
            return null;
        };
        performWithContext(action);
    }

    public void eachSearchedMessage(String folderName, SearchTerm searchTerm, Closure closure) throws Exception {
        WithContext<Object> action = ctx -> {
            Folder folder = ctx.store.getFolder(folderName);
            if (folder.exists()) {
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.search(searchTerm);
                for (Message message : messages) {
                    closure.call(message);
                }
                folder.close(false);
            }
            return null;
        };
        performWithContext(action);
    }

    private <T> T performWithContext(WithContext<T> action) throws Exception {
        ImapContext ctx = null;
        try {
            if (url != null) {
                ctx = new ImapContext();
                ctx.properties = createEnvironment(url);
                ctx.session = Session.getDefaultInstance(ctx.properties, null);
                ctx.store = ctx.session.getStore("imap");
                ctx.store.connect(username, password);
                return action.perform(ctx);
            } else {
                return null;
            }
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    private static Properties createEnvironment(String urlSpec) throws MalformedURLException {
        URI uri = URI.create(urlSpec);
        Properties env = new Properties();
        env.setProperty("mail.store.protocol", "imap");
        env.setProperty("mail.imap.host", uri.getHost());
        env.setProperty("mail.imap.port", Integer.toString(uri.getPort()));
        boolean secure = uri.getScheme().equals("imaps") || 993 == uri.getPort();
        env.setProperty("mail.imap.ssl.enable", secure ? "true" : "false");
        return env;
    }
}

package org.xbib.groovy.smtp;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.net.URI;
import java.util.Date;
import java.util.Properties;

/**
 */
public class SMTP {

    private static final String DEFAULT_URL = "smtp://localhost:25/";

    private final String url;

    private final String username;

    private final String password;

    private SMTP(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static SMTP newInstance() {
        return new SMTP(DEFAULT_URL, null, null);
    }

    public static SMTP newInstance(String url) {
        return new SMTP(url, null, null);
    }

    public static SMTP newInstance(String url, String username, String password) {
        return new SMTP(url, username, password);
    }

    public String getURL() {
        return url;
    }

    public void send(String subject, String from, String to, String text) throws Exception {
        Address[] toAddr = { new InternetAddress(to) };
        send(subject, new InternetAddress(from), null, toAddr, null, null, text);
    }

    public void send(String subject, Address from, Address[] to, String text) throws Exception {
        send(subject, from, null, to, null, null, text);
    }

    public void send(String subject,
                     Address from, Address[] replyTo,
                     Address[] to, Address[] cc, Address[] bcc,
                     String text) throws Exception {
        Multipart multipart = new MimeMultipart("mixed");
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        mimeBodyPart.setText(text);
        multipart.addBodyPart(mimeBodyPart);
        send(subject, from, replyTo, to, cc, bcc, multipart);
    }

    public void send(String subject,
                     Address from, Address[] replyTo,
                     Address[] to, Address[] cc, Address[] bcc,
                     Multipart multipart) throws Exception {
        WithContext<Object> action = ctx -> {
            Message message = new MimeMessage(ctx.session);
            message.setSentDate(new Date());
            message.setFrom(from);
            message.setSubject(subject);
            if (replyTo != null) {
                message.setReplyTo(replyTo);
            }
            message.setRecipients(Message.RecipientType.TO, to);
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, cc);
            }
            if (bcc != null) {
                message.setRecipients(Message.RecipientType.BCC, bcc);
            }
            message.setContent(multipart);
            Transport.send(message);
            return null;
        };
        performWithContext(action);
    }

    private <T> T performWithContext(WithContext<T> action) throws Exception {
        SmtpContext ctx = null;
        try {
            if (url != null) {
                ctx = new SmtpContext();
                ctx.properties = createEnvironment(url);
                ctx.session = username != null ?
                        Session.getDefaultInstance(ctx.properties, new SMTPAuthenticator()) :
                        Session.getDefaultInstance(ctx.properties);
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

    private static Properties createEnvironment(String urlSpec) {
        URI uri = URI.create(urlSpec);
        Properties env = new Properties();
        env.setProperty("mail.smtp.auth",  "false");
        env.setProperty("mail.smtp.host", uri.getHost());
        env.setProperty("mail.smtp.port", Integer.toString(uri.getPort()));
        boolean secure = uri.getScheme().equals("smtps") || 995 == uri.getPort();
        env.setProperty("mail.smtp.ssl.enable", secure ? "true" : "false");
        env.setProperty("mail.debug",  "true");
        return env;
    }

    private class SMTPAuthenticator extends Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}

package org.xbib.groovy.smtp;

/**
 * The Context for {@link SMTP}.
 *
 * @param <T> the type parameter
 */
public interface WithContext<T> {

    T perform(SmtpContext ctx) throws Exception;
}

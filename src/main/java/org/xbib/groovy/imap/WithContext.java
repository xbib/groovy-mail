package org.xbib.groovy.imap;

/**
 *
 * @param <T>
 */
public interface WithContext<T> {

    T perform(ImapContext ctx) throws Exception;
}

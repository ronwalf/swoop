/*
 * Created on Oct 28, 2005
 */
package org.mindswap.swoop.utils.exceptions;

/**
 * @author Evren Sirin
 *
 */
public class UserCanceledException extends RuntimeException {
    public UserCanceledException() {
    }

    public UserCanceledException(String message) {
        super( message );
    }
}

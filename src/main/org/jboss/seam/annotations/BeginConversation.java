//$Id$
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as beginning a conversation, if none
 * exists, and if the method returns without throwing 
 * an exception.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface BeginConversation {}

//$Id$
package org.jboss.seam.interceptors;

import java.lang.reflect.Method;

import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;
import javax.ejb.Remove;

import org.jboss.logging.Logger;
import org.jboss.seam.annotations.Around;

/**
 * Removes components from the Seam context after invocation
 * of an EJB @Remove method.
 * 
 * @author Gavin King
 */
@Around({ValidationInterceptor.class, BijectionInterceptor.class})
public class RemoveInterceptor extends AbstractInterceptor
{
   
   private static final Logger log = Logger.getLogger(RemoveInterceptor.class);

   @AroundInvoke
   public Object removeIfNecessary(InvocationContext invocation) throws Exception
   {
      Object result;
      try
      {
         result = invocation.proceed();
      }
      catch (Exception exception)
      {
         removeIfNecessary( invocation.getBean(), invocation.getMethod(), true );
         throw exception;
      }

      removeIfNecessary( invocation.getBean(), invocation.getMethod(), false );
      return result;
   }

   private void removeIfNecessary(Object bean, Method method, boolean exception)
   {
      boolean wasRemoved = method.isAnnotationPresent(Remove.class) &&
            ( !exception || !method.getAnnotation(Remove.class).retainIfException() );
      if ( wasRemoved )
      {
         component.getScope().getContext().remove( component.getName() );
         log.info("Stateful component was removed");
      }
   }

 

}

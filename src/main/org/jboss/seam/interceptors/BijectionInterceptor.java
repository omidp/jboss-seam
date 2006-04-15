//$Id$
package org.jboss.seam.interceptors;

import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Before invoking the component, inject all dependencies. After
 * invoking, outject dependencies back into their context.
 * 
 * @author Gavin King
 */
public class BijectionInterceptor extends AbstractInterceptor
{
   
   private static final Log log = LogFactory.getLog(BijectionInterceptor.class);

   @AroundInvoke
   public Object bijectTargetComponent(InvocationContext invocation) throws Exception
   {
      if (component.needsInjection()) //only needed to hush the log message
      {
         if ( log.isTraceEnabled() )
         {
            log.trace("injecting dependencies of: " + component.getName());
         }
         component.inject(invocation.getBean()/*, true*/);
      }
      
      Object result = invocation.proceed();
      
      if (component.needsOutjection()) //only needed to hush the log message
      {
         if ( log.isTraceEnabled() )
         {
            log.trace("outjecting dependencies of: " + component.getName());
         }
         component.outject(invocation.getBean());
      }
      
      return result;
   }

}

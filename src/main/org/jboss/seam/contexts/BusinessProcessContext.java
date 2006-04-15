/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.ProcessInstance;
import org.jbpm.context.exe.ContextInstance;

/**
 * Exposes a jbpm variable context instance for reading/writing.
 *
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author Gavin King
 * @version $Revision$
 */
public class BusinessProcessContext implements Context {

   private static final Log log = LogFactory.getLog(BusinessProcessContext.class);

   private final Map<String, Object> additions = new HashMap<String, Object>();
   private final Set<String> removals = new HashSet<String>();

   public ScopeType getType()
   {
      return ScopeType.BUSINESS_PROCESS;
   }

   public BusinessProcessContext() {}
   
   private ContextInstance getContextInstance()
   {
      org.jbpm.graph.exe.ProcessInstance processInstance = getProcessInstance();
      return processInstance==null ? null : processInstance.getContextInstance(); 
   }

   public Object get(String name) {
      Object result = additions.get(name);
      if (result!=null) return result;
      if ( removals.contains(name) ) return null;
      ContextInstance context = getContextInstance();
      return context==null ? null : context.getVariable(name);
   }

   public void set(String name, Object value) {
      if (value==null)
      {
         //yes, we need this
         remove(name);
      }
      else
      {
         removals.remove(name);
         additions.put(name, value);
      }
   }

   public boolean isSet(String name) {
      return get(name)!=null;
   }
   
   public void remove(String name) {
      additions.remove(name);
      removals.add(name);
   }

   public String[] getNames() {
      Set<String> results = getNamesFromContext();
      results.addAll( additions.keySet() ); //after, to override
      return results.toArray(new String[]{});
   }

   private Set<String> getNamesFromContext() {
      HashSet<String> results = new HashSet<String>();
      ContextInstance context = getContextInstance();
      if ( context!=null ) 
      {
         results.addAll( context.getVariables().keySet() );
         results.removeAll(removals);
      }
      return results;
   }

   public Object get(Class clazz)
   {
      return get( Seam.getComponentName(clazz) );
   }
   
   public void clear()
   {
      additions.clear();
      removals.addAll( getNamesFromContext() );
   }
   

   public void flush()
   {
      org.jbpm.graph.exe.ProcessInstance processInstance = getProcessInstance();
      if ( processInstance==null )
      {
         log.debug( "no process instance to persist business process state" );
      }
      else if ( !additions.isEmpty() || !removals.isEmpty() )
      {
         log.debug( "flushing to process instance: " + processInstance.getId() );

         ContextInstance contextInstance = processInstance.getContextInstance();

         for ( Map.Entry<String, Object> entry: additions.entrySet() )
         {
            contextInstance.setVariable( entry.getKey(), entry.getValue() );
         }
         additions.clear();

         for ( String name: removals )
         {
            contextInstance.deleteVariable(name);
         }
         removals.clear();
      }
   }

   private org.jbpm.graph.exe.ProcessInstance getProcessInstance()
   {
      Init init = Init.instance(); //may be null in some tests
      if ( init==null || !init.isJbpmInstalled() ) 
      {
         return null;
      }
      else
      {
          return ProcessInstance.instance();
      }
   }

}

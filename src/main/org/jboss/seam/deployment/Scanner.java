//$Id$
package org.jboss.seam.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Scanner
{

   private static final Log log = LogFactory.getLog(Scanner.class);

   private String resourceName;
   private ClassLoader classLoader;
   
   public Scanner()
   {
      this("seam.properties", Thread.currentThread().getContextClassLoader());
   }
   
   public Scanner(String resourceName, ClassLoader classLoader)
   {
      this.resourceName = resourceName;
      this.classLoader = classLoader;      
   }
   
   public static String filenameToClassname(String filename)
   {
      return filename.substring(0, filename.lastIndexOf(".class"))
            .replace('/', '.').replace('\\', '.');
   }
   
   public Set<Class<?>> getClasses()
   {
      Set<Class<?>> result = new HashSet<Class<?>>();
      Enumeration<URL> urls;
      try
      {
         urls = classLoader.getResources(resourceName);
      }
      catch (IOException ioe) {
         log.warn("could not read: " + resourceName, ioe);
         return result;
      }
      
      while (urls.hasMoreElements())
      {
         try
         {
            String urlPath = urls.nextElement().getFile();
            urlPath = URLDecoder.decode(urlPath, "UTF-8");
            if ( urlPath.startsWith("file:") )
            {
               // On windows urlpath looks like file:/C: on Linux file:/home
               // substring(5) works for both
               urlPath = urlPath.substring(5);
            }
            if ( urlPath.indexOf('!')>0 )
            {
               urlPath = urlPath.substring(0, urlPath.indexOf('!'));
            }
            else
            {
               urlPath = new File(urlPath).getParent();
            }
            log.info("scanning: " + urlPath);
            File file = new File(urlPath);
            if ( file.isDirectory() )
            {
               handleDirectory(result, file, null);
            }
            else
            {
               handleArchive(result, file);
            }
         }
         catch (IOException ioe) {
            log.warn("could not read entries", ioe);
         }
      }
      return result;
   }

   private void handleArchive(Set<Class<?>> result, File file) throws ZipException, IOException
   {
      log.debug("archive: " + file);
      ZipFile zip = new ZipFile(file);
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while ( entries.hasMoreElements() )
      {
         ZipEntry entry = entries.nextElement();
         String name = entry.getName();
         log.debug("found: " + name);
         handleItem(result, name);
      }
   }

   private void handleDirectory(Set<Class<?>> result, File file, String path)
   {
      log.debug("directory: " + file);
      for ( File child: file.listFiles() )
      {
         String newPath = path==null ? 
                  child.getName() : path + '/' + child.getName();
         if ( child.isDirectory() )
         {
            handleDirectory( result, child, newPath );
         }
         else
         {
            handleItem( result, newPath );
         }
      }
   }

   private void handleItem(Set<Class<?>> result, String name)
   {
      if ( name.endsWith(".class") && !name.startsWith("org/jboss/seam/core") )
      {
         String classname = filenameToClassname( name );
         try
         {
            result.add( classLoader.loadClass( classname ) );
         }
         catch (ClassNotFoundException cnfe) {
            log.debug( "could not load class: " + classname, cnfe );
         }
      }
   }
}

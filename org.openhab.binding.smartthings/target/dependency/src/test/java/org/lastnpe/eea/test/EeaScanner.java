package org.lastnpe.eea.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans for eea-files in classpath. The classpath components are idenfified by their eea-for-gav file.
 * So only projects/jars are inspected that contains such a file.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class EeaScanner {

  List<String> eeaFiles;

  /**
   * Returns all eea file names that are found in classpath. The files are
   * returned relatively. e.g. 'java/lang/Object.eea'.
   */
  public List<String> getEeaFiles() throws IOException {
    if (eeaFiles == null) {
      List<String> ret = new ArrayList<>();
      Enumeration<URL> res = SelfTest.class.getClassLoader().getResources("eea-for-gav");
      while (res.hasMoreElements()) {
        URL url = res.nextElement();
        if ("file".equals(url.getProtocol())) {
          File file = new File(URLDecoder.decode(url.getPath().replace("+", "%2b"), "UTF-8"));
          scanDirectory(ret, file.getParentFile());
        } else if ("jar".equals(url.getProtocol())) {
          String path = url.getPath();
          path = path.substring(5, path.length() - 13); // cuts out "jar:" ... "!/eea-for-gav"
          File file = new File(URLDecoder.decode(path.replace("+", "%2b"), "UTF-8"));
          scanJar(ret, file);
        }
      }
      eeaFiles = Collections.unmodifiableList(ret);
    }
    return eeaFiles;
  }

  /**
   * Scans a directory for .eea files. This code path is used when the test is
   * invoked from IDE
   */
  protected void scanDirectory(List<String> ret, File dir) throws MalformedURLException {
    String rootDiskFolder = dir.getAbsolutePath();
    if (!rootDiskFolder.endsWith(File.separator)) {
      rootDiskFolder = rootDiskFolder + File.separator;
    }
    scanDirectory(ret, dir, rootDiskFolder);
  }

  private void scanDirectory(List<String> ret, File dir, String rootDiskFolder) throws MalformedURLException {
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        scanDirectory(ret, file, rootDiskFolder);
      } else if (file.getName().endsWith(".eea")) {
        ret.add(file.getAbsolutePath().substring(rootDiskFolder.length()).replace(File.separatorChar, '/'));
      }
    }
  }

  protected void scanJar(List<String> ret, File file) throws IOException {
    try (JarFile jarFile = new JarFile(file)) {
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().endsWith(".eea")) {
          ret.add(entry.getName());
        }
      }
    }
  }
}

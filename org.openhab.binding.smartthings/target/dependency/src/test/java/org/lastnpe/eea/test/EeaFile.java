package org.lastnpe.eea.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Represents the EEA file. The EEA file could either be parsed by name
 */
public class EeaFile {

  enum State {
    READ_CLASS, READ_ENTRY, READ_SIGNATURE1, READ_SIGNATURE2
  }

  /**
   * This represents an entry - a definition of a method/constant with old and new
   * signature.
   */
  public static class Entry {
    public Entry(String name) {
      this.name = name;
    }

    private final String name;
    private String signature1;
    private String signature2;
    private boolean important;

    @Override
    public String toString() {
      return name + "\n " + signature1 + "\n " + signature2;
    }

    public String getName() {
      return name;
    }

    public String getSignature1() {
      return signature1;
    }

    public String getSignature2() {
      return signature2;
    }

    public boolean isImportant() {
      return important;
    }
  }

  private String className;

  private Map<String, Entry> entries = new TreeMap<>();

  /**
   * Parses the eea from eea-file. Specify a resourcename in classpath. Note, that
   * the resource name should start with '/'
   */
  public static EeaFile parseEeaFile(String resourceName) throws IOException {
    EeaFile result = new EeaFile();
    State state = State.READ_CLASS;
    int i = 0;
    Entry entry = null;
    try (InputStream is = EeaFile.class.getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = reader.readLine()) != null) {
        i++;
        if (line.trim().length() == 0) {
          continue;
        }
        switch (state) {

        case READ_CLASS:
          if (!line.startsWith("class ")) {
            throw new IllegalStateException("expected that line starts with 'class'");
          }
          result.className = line.substring(6);
          state = State.READ_ENTRY;
          break;

        case READ_ENTRY:
          if (line.startsWith(" ")) {
            throw new IllegalStateException("expected that line starts with entry");
          }
          if (entry != null) {
            result.addEntry(entry);
          }
          entry = new Entry(line);
          state = State.READ_SIGNATURE1;
          break;

        case READ_SIGNATURE1:
          if (!line.startsWith(" ")) {
            throw new IllegalStateException("expected that line contains signature");
          }
          entry.signature1 = line.substring(1);
          state = State.READ_SIGNATURE2;
          break;

        case READ_SIGNATURE2:
          if (!line.startsWith(" ")) {
            throw new IllegalStateException("expected that line contains signature");
          }
          entry.signature2 = line.substring(1);
          ;
          state = State.READ_ENTRY;
          break;
        }

      }
    } catch (IllegalStateException ie) {
      throw new IllegalStateException("Error in file " + resourceName + " line " + i + ": " + ie.getMessage(), ie);
    }
    if (entry != null) {
      result.addEntry(entry);
    }
    return result;
  }

  /**
   * Parses the EEA file by reading the bytecode using asm. Specify a resourcename
   * in classpath. Note, that the resource name should start with '/'
   */
  public static EeaFile parseClassFile(String resourceName) throws IOException {
    EeaFile result = new EeaFile();

    try (InputStream is = EeaFile.class.getResourceAsStream(resourceName)) {
      ClassReader cr = new ClassReader(is);
      cr.accept(new ClassVisitor(Opcodes.ASM7) {

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
          result.className = name;
          super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
          result.visit(access, name, desc, signature);
          return super.visitField(access, name, desc, signature, value);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
          result.visit(access, name, desc, signature);
          return super.visitMethod(access, name, desc, signature, exceptions);
        }
      }, 0);
    }
    return result;
  }

  private void visit(int access, String name, String desc, String signature) {
    // CHECKME: There is no need to declare private methods in eea file.
    // if ((Opcodes.ACC_PRIVATE & access) == 0) {
    Entry entry = new Entry(name);
    entry.signature1 = signature != null ? signature : desc;
    entry.signature2 = entry.signature1;
    
    // detect if method is public and object or arrays are involved
    entry.important = (Opcodes.ACC_PUBLIC & access) > 0
        && (entry.signature1.contains(";") || entry.signature1.contains("[")); 
    addEntry(entry);
    // }
  }

  /**
   * Returns the parsed information as eea-compatible string. this is a good place to work on.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ").append(className).append('\n');
    for (Entry entry : entries.values()) {
      if (entry.isImportant()) {
        sb.append(entry).append('\n');
      }
    }
    return sb.toString();
  }

  public String getClassName() {
    return className;
  }

  private void addEntry(Entry entry) {
    String key = entry.getName() + entry.getSignature1();
    if (entries.put(key, entry) != null) {
      throw new IllegalStateException(entry + " defined twice");
    }
  }

  /**
   * Return the exact entry
   */
  public Entry getEntry(String name, String signature) {
    return entries.get(name + signature);
  }

  /**
   * Return all entries.
   */
  public Collection<Entry> getEntries() {
    return entries.values();
  }

  /**
   * Returns all entries matching to this name.
   */
  public List<Entry> getEntries(String name) {
    List<Entry> ret = new ArrayList<>();
    for (Entry entry : entries.values()) {
      if (entry.getName().equals(name)) {
        ret.add(entry);
      }
    }
    return ret;
  }
}

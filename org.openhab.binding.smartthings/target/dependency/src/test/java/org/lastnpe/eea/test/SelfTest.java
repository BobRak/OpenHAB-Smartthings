package org.lastnpe.eea.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

/**
 * This is a 'self test'. It checks if the .eea files will match to the .class files.
 * 
 * (it only validates the first line of signature, not the second line, which contains null information)
 * @author Roland Praml, FOCONIS AG
 *
 */
public class SelfTest {

  /**
   * Should we list missing methods.
   */
  private static boolean LIST_MISSING = false;
  private final EeaScanner scanner = new EeaScanner();

  @Test
  public void testEeaConsistency() throws Exception {
    boolean ok = true;
    for (String eea : scanner.getEeaFiles()) {
      ok &= checkEea(eea);
    }
    if (!ok) {
      fail("There were eea inconsistences detected");
    }
  }

  protected boolean checkEea(String eeaPath) {
    boolean ok = true;
    ;
    try {
      EeaFile eea = EeaFile.parseEeaFile("/" + eeaPath);
      EeaFile cls = EeaFile.parseClassFile("/" + eeaPath.replace(".eea", ".class"));

      if (!cls.getClassName().equals(eea.getClassName())) {
        ok = false;
        System.out.println("[FAIL] " + eeaPath + ": Class name does not match");
      }

      for (EeaFile.Entry eeaEntry : eea.getEntries()) {
        // check if we find each corresponding entry in the class
        EeaFile.Entry clsEntry = cls.getEntry(eeaEntry.getName(), eeaEntry.getSignature1());
        
        if (clsEntry == null) {
          System.out.println("[FAIL] " + eeaPath + ": Did not find: " + eeaEntry.getName() + " " + eeaEntry.getSignature1());
          ok = false;
          
          List<EeaFile.Entry> candidates = cls.getEntries(eeaEntry.getName());
          if (candidates.isEmpty()) {
            System.out.println("No Candidates found");
          } else {
            System.out.println("Candidates:");
            for (EeaFile.Entry candidate : candidates) {
              System.out.println(candidate);
            }
          }
        }
      }

      if (ok && LIST_MISSING) {
        for (EeaFile.Entry entry : cls.getEntries()) {
          if (entry.isImportant()) {
            if (eea.getEntry(entry.getName(), entry.getSignature1()) == null) {
              System.out.println("[WARN] " + eeaPath + ": Should be checked:");
              System.out.println(entry);
            }
          }
        }
      }
      return ok;
    } catch (Exception e) {
      System.out.println("[FAIL] " + eeaPath + ": " + e.getMessage());
      return false;
    }
  }

}

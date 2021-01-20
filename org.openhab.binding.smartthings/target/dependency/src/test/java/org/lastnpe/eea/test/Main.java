package org.lastnpe.eea.test;

import java.io.IOException;
import java.time.OffsetDateTime;

public class Main {

  // modify this line and run the class in eclipse (right click/run java application)
  private static final Class<?> CLASS_TO_TEST = OffsetDateTime.class;
  
  public static void main(String[] args) throws IOException {
    EeaFile eea = EeaFile.parseClassFile("/" + CLASS_TO_TEST.getName().replace('.','/') + ".class");
    System.out.println(eea);
  }

}

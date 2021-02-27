package com.mflima.katscans.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/** @author Marcelo Lima */
public class FileAwareInputStream extends FileInputStream {

  private final File file;

  public FileAwareInputStream(File file) throws FileNotFoundException {
    super(file);

    this.file = file;
  }

  public File getFile() {
    return file;
  }
}

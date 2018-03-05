package edu.unh.letsmeet.engine;

import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream that flushes stream instead of closing on close().
 * Use forceClose() to close the underlying stream.
 */
public class UncloseableOutputStream extends OutputStream {
  private final OutputStream delegate;

  public UncloseableOutputStream(OutputStream delegate) {
    this.delegate = delegate;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  @Override
  public void write(byte[] b) throws IOException {
    delegate.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    delegate.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }

  @Override
  public void close() throws IOException {
    delegate.flush();
  }

  @Override
  public void write(int b) throws IOException {
    delegate.write(b);
  }

  public void forceClose() throws IOException {
    delegate.close();
  }
}

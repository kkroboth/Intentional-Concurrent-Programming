package edu.unh.letsmeet.engine;


import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that may not be closed. Use forceClose() to close the
 * underlying stream.
 */
class UncloseableInputStream extends InputStream {
  private final InputStream delegate;

  public UncloseableInputStream(InputStream delegate) {
    this.delegate = delegate;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  @Override
  public long skip(long n) throws IOException {
    return delegate.skip(n);
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }

  @Override
  public void close() throws IOException {
    // don't close
  }

  @Override
  public void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Override
  public void reset() throws IOException {
    delegate.reset();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return delegate.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }


  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  public void forceClose() throws IOException {
    delegate.close();
  }
}

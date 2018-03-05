package edu.unh.letsmeet.engine;

import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.io.OutputStream;

import static edu.unh.letsmeet.engine.HttpServer.CRLF_BYTES;

// See: https://stackoverflow.com/a/2395224/702568
public class ChunkedOutputStream extends OutputStream {
  private final OutputStream delegate;

  public ChunkedOutputStream(OutputStream outputStream) {
    this.delegate = outputStream;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  @Override
  public void write(int i) throws IOException {
    write(new byte[]{(byte) i}, 0, 1);
  }

  @Override
  public void write(byte[] b, int offset, int length) throws IOException {
    writeHeader(length);
    delegate.write(CRLF_BYTES, 0, CRLF_BYTES.length);
    delegate.write(b, offset, length);
    delegate.write(CRLF_BYTES, 0, CRLF_BYTES.length);
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }

  @Override
  public void close() throws IOException {
    writeHeader(0);
    delegate.write(CRLF_BYTES, 0, CRLF_BYTES.length);
    delegate.write(CRLF_BYTES, 0, CRLF_BYTES.length);
    delegate.close();
  }

  private void writeHeader(int length) throws IOException {
    byte[] header = Integer.toHexString(length).getBytes();
    delegate.write(header, 0, header.length);
  }
}

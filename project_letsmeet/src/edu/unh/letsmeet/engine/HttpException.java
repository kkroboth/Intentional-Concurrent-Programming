package edu.unh.letsmeet.engine;

public class HttpException extends Exception {
  private final int status;
  private String body;

  public HttpException(int status, String message) {
    super(message);
    this.status = status;
  }

  public HttpException(int status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public HttpException(int status) {
    this.status = status;
  }

  public HttpException(int status, Throwable cause) {
    super(cause);
    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }

  public HttpException body(String body) {
    this.body = body;
    return this;
  }

  public String getBody() {
    return body;
  }
}

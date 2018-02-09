package edu.unh.letsmeet;

import edu.unh.letsmeet.engine.Server;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    Props props = Props.getInstance();
    Server server = new Server(props.getHost(), props.getPort());

    server.start();
    Thread.sleep(5000);
    server.stop();
  }
}

package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.Props;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

// TODO: Implement persistent connections (will add more server state!)
public class Server {
  private static final Logger logger = Logger.getLogger("Server");

  private final String host;
  private final int port;
  private final ServerSocket serverSocket;
  private final ICPExecutorService executorService;
  private volatile boolean started;

  /**
   * Creates server socket but does not bind address
   *
   * @param host
   * @param port
   * @throws IOException could not open the server socket
   */
  public Server(String host, int port, ICPExecutorService executorService) throws IOException {
    this.host = host;
    this.port = port;
    this.executorService = executorService;
    serverSocket = new ServerSocket();
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Uses fixed thread pool executor with number of threads from
   * 'server_threads' in props.
   */
  public Server(String host, int port) throws IOException {
    this(host, port, ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Props.getInstance().getServerThreads())
    ));
  }

  /**
   * Continue to accept socket connections and submit new Jobs
   * to handle them in the executor.
   *
   * @throws IOException Server connection could not be made
   */
  public void start() throws IOException {
    started = true;
    serverSocket.bind(new InetSocketAddress(host, port));

    new Thread(Task.ofThreadSafe(() -> {
      while (!serverSocket.isClosed()) {
        try {
          Socket socket = serverSocket.accept();
          executorService.execute(Task.ofThreadSafe(() -> handleRequest(socket)));
        } catch (IOException e) {
          // Don't handle IOException for when a server was closed.
          // Assuming IOException is coming from server being closed.
          if (!serverSocket.isClosed()) {
            logger.log(SEVERE, e.toString(), e);
          }
        }
      }
    }), "server-dispatcher").start();
  }

  public void stop() throws IOException {
    if (!started) throw new IllegalStateException("Server not started");
    serverSocket.close();
    executorService.shutdown();
  }

  /**
   * Handle Client connection
   *
   * @param connection
   */
  protected void handleRequest(Socket connection) {
    try {
      Request request = Request.parse(connection.getInputStream());
      System.out.println(request);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // For now return simple response (Hello World)
    StringBuilder builder = new StringBuilder();
    String body = "Hello World";
    builder.append("HTTP/1.1 200 OK").append("\r\n")
      .append("Content-Length: ").append(body.length()).append("\r\n")
      .append("Connection: close").append("\r\n").append("\r\n")
      .append(body);

//    while (true) {
//      Socket socket = serverSocket.accept();
//      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//      for (; ; ) {
//        System.out.println(reader.readLine());
//      }
//    }

    try {
      connection.getOutputStream().write(builder.toString().getBytes(StandardCharsets.UTF_8));
      connection.close();
    } catch (IOException e) {
      // TODO: Use logger
      e.printStackTrace();
    }
  }
}

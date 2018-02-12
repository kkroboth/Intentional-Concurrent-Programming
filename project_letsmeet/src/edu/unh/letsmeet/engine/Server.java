package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.Props;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.io.IOException;
import java.io.InputStream;
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
  private final RequestHandler handler;

  /**
   * Creates server socket but does not bind address
   *
   * @param host
   * @param port
   * @throws IOException could not open the server socket
   */
  public Server(String host, int port, ICPExecutorService executorService, RequestHandler handler) throws IOException {
    this.host = host;
    this.port = port;
    this.executorService = executorService;
    this.handler = handler;
    serverSocket = new ServerSocket();
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Uses fixed thread pool executor with number of threads from
   * 'server_threads' in props.
   */
  public Server(String host, int port, RequestHandler handler) throws IOException {
    this(host, port, ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Props.getInstance().getServerThreads())
    ), handler);
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
    InputStream inputStream;
    try {
      inputStream = connection.getInputStream();
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
      return;
    }


    Request request;
    Response response;
    try {
      request = Request.parse(inputStream);
      response = handler.handleRequest(request);
    } catch (HttpException e) {
      response = new Response.Builder(e.getStatus()).build();
    }


    try {
      connection.getOutputStream().write(response.createResponse().getBytes(StandardCharsets.UTF_8));
      connection.close();
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
    }
  }
}

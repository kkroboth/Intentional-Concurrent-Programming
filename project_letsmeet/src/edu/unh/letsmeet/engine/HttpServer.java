package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.Props;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.DisjointSemaphore;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;
import icp.lib.Permit;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

// TODO: Implement persistent connections (will add more server state!)
public class HttpServer implements ServerProvider {
  private static final Logger logger = Logger.getLogger("HttpServer");

  // Settings and props
  private final String host;
  private final int port;
  private final Settings settings;

  // Server
  private final ServerSocket serverSocket;
  private final RequestHandler handler;
  private final List<Middleware> middlewares; // immutable list of middleware -- order matters!

  private final ICPExecutorService executorService;
  private volatile boolean started;

  // Concurrent
  private final DisjointSemaphore connectionLimiter;

  /**
   * Creates server socket but does not bind address
   *
   * @param host
   * @param port
   * @throws IOException could not open the server socket
   */
  public HttpServer(String host, int port, ICPExecutorService executorService, RequestHandler handler,
                    List<Middleware> middlewares) throws IOException {
    this.host = host;
    this.port = port;
    this.handler = handler;
    this.executorService = executorService;
    this.settings = new Settings();

    serverSocket = new ServerSocket();
    if (middlewares == null) middlewares = Collections.emptyList();
    this.middlewares = Collections.unmodifiableList(middlewares);
    connectionLimiter = new DisjointSemaphore(Props.getInstance().getServerThreads());

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Uses fixed thread pool executor with number of threads from
   * 'server_threads' in props.
   */
  public HttpServer(String host, int port, RequestHandler handler,
                    List<Middleware> middlewares) throws IOException {
    this(host, port, ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Props.getInstance().getServerThreads())
    ), handler, middlewares);
  }

  /**
   * Continue to accept socket connections and submit new Jobs
   * to handle them in the executor.
   *
   * @throws IOException HttpServer connection could not be made
   */
  public void start() throws IOException {
    settings.freeze();
    started = true;
    serverSocket.bind(new InetSocketAddress(host, port));

    new Thread(Task.ofThreadSafe(() -> {
      connectionLimiter.registerAcquirer();

      while (!serverSocket.isClosed()) {
        try {
          Socket socket = serverSocket.accept();
          Permit permit = connectionLimiter.acquire();
          executorService.execute(Task.ofThreadSafe(() -> {
            try {
              connectionLimiter.registerReleaser();
              handleRequest(socket);
            } finally {
              connectionLimiter.release(permit);
            }
          }));
        } catch (IOException e) {
          // Don't handle IOException for when a server was closed.
          // Assuming IOException is coming from server being closed.
          if (!serverSocket.isClosed()) {
            logger.log(SEVERE, e.toString(), e);
          }
        } catch (InterruptedException e) {
          logger.log(WARNING, e.getMessage(), e);
        }
      }
    }), "server-dispatcher").start();
  }

  public void stop() throws IOException {
    if (!started) throw new IllegalStateException("HttpServer not started");
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
    Response response = null;
    Map<String, Object> session = new HashMap<>();
    try {
      request = Request.parse(inputStream);
      for (Middleware middleware : middlewares) {
        Response result = middleware.onRequest(this, session, request);
        if (result != null) {
          response = result;
          break;
        }
      }

      // Only if middleware did not create own response
      if (response == null) {
        response = handler.handleRequest(this, request, session);
        if (!middlewares.isEmpty()) {
          Response.Builder builder = new Response.Builder(response);
          middlewares.forEach(m -> m.onResponse(this, session, builder, request));
          response = builder.build();
        }
      }
    } catch (HttpException e) {
      if (e.getStatus() >= 500)
        logger.log(SEVERE, e.getCause().getMessage(), e.getCause());
      else
        logger.log(INFO, e.getCause().getMessage(), e.getCause());
      response = new Response.Builder(e.getStatus()).build();
    }

    try {
      connection.getOutputStream().write(response.createResponse());
      connection.close();
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public Settings getSettings() {
    return settings;
  }
}

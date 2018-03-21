package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.Props;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.DisjointSemaphore;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;
import icp.lib.Permit;
import icp.wrapper.ICPProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

// TODO: Implement persistent connections (will add more server state!)
public class HttpServer {
  private static final Logger logger = Logger.getLogger("HttpServer");

  public static final byte[] CRLF_BYTES = "\r\n".getBytes();
  public static final String CRLF = "\r\n";

  // Settings and props
  private final ServerProvider serverProvider;

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
   * @throws IOException could not open the server socket
   */
  public HttpServer(ServerProvider serverProvider, ICPExecutorService executorService, RequestHandler handler,
                    List<Middleware> middlewares) throws IOException {
    this.serverProvider = serverProvider;
    this.handler = handler;
    this.executorService = executorService;

    serverSocket = new ServerSocket();
    if (middlewares == null) middlewares = Collections.emptyList();
    else ICPProxy.setProxyPermission(middlewares, Permissions.getFrozenPermission());
    this.middlewares = ICPProxy.newFrozenInstance(List.class, Collections.unmodifiableList(middlewares));
    connectionLimiter = new DisjointSemaphore(Props.getInstance().getServerThreads());

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Uses fixed thread pool executor with number of threads from
   * 'server_threads' in props.
   */
  public HttpServer(ServerProvider serverProvider, RequestHandler handler,
                    List<Middleware> middlewares) throws IOException {
    this(serverProvider, ICPExecutors.newICPExecutorService(
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
    serverProvider.getSettings().freeze();
    started = true;
    serverSocket.bind(new InetSocketAddress(serverProvider.getHost(), serverProvider.getPort()));

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
    InputStream inputStream, uncloseableInputStream;
    OutputStream outputStream, uncloseableOutputStream;
    try {
      inputStream = connection.getInputStream();
      uncloseableInputStream = new UncloseableInputStream(inputStream);
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
      return;
    }


    Request request;
    Response response = null;
    //noinspection unchecked
    Map<String, Object> session = ICPProxy.newPrivateInstance(Map.class, new HashMap<>());
    try {
      request = Request.parse(uncloseableInputStream);
      for (Middleware middleware : middlewares) {
        Response result = middleware.onRequest(serverProvider, session, request);
        if (result != null) {
          response = result;
          break;
        }
      }

      // Only if middleware did not create own response
      if (response == null) {
        response = handler.handleRequest(serverProvider, request, session);
        if (!middlewares.isEmpty()) {
          Response.Builder builder = new Response.Builder(response);
          middlewares.forEach(m -> m.onResponse(serverProvider, session, builder, request));
          response = builder.build();
        }
      }
    } catch (HttpException e) {
      Level level = e.getStatus() >= 500 ? SEVERE : INFO;
      if (e.getCause() != null) {
        logger.log(level, e.getCause().getMessage(), e.getCause());
      } else {
        logger.log(level, e.getMessage());
      }

      // Add optional body message to response
      Response.Builder builder = Response.create(e.getStatus());
      if (e.getBody() != null) {
        builder.plain(e.getBody());
      }

      response = builder.build();
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
      response = Response.create(500).build();
    }

    try {
      outputStream = connection.getOutputStream();
      uncloseableOutputStream = new UncloseableOutputStream(outputStream);
      response.createResponse(uncloseableOutputStream);
      connection.close();
    } catch (IOException e) {
      logger.log(SEVERE, e.getMessage(), e);
    }
  }
}

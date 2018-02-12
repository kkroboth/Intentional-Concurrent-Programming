package edu.unh.letsmeet;

import edu.unh.letsmeet.command.CommandLineScanner;
import edu.unh.letsmeet.engine.Server;
import edu.unh.letsmeet.server.PagesRequestHandler;
import edu.unh.letsmeet.server.StaticFilesRequestHandler;
import edu.unh.letsmeet.users.DatabaseHelper;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger("Main");
  private Server server;

  public Main() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public synchronized void start() throws IOException {
    Props props = Props.getInstance();
    server = new Server(props.getHost(), props.getPort(), new PagesRequestHandler(
      props.getPagesDirectory(),
      PagesRequestHandler.buildUrls(
        "/", "index.html"
      ), new StaticFilesRequestHandler(
      props.getStaticDirectory(), "/static/")
    ));
    DatabaseHelper.getInstance(); // Starts database
    server.start();
  }

  public synchronized void stop() {
    try {
      server.stop();
      DatabaseHelper.getInstance().close();
    } catch (SQLException | IOException e) {
      logger.log(Level.SEVERE, e.toString(), e);
    }
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    Runtime.getRuntime().addShutdownHook(new Thread(Task.ofThreadSafe(main::stop)));
    main.start();


    CommandLineScanner cmdline = new CommandLineScanner();
    cmdline.parseInput();
  }

}

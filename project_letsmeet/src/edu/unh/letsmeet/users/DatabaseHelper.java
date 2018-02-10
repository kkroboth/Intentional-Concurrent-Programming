package edu.unh.letsmeet.users;

import com.amdelamar.jhash.Hash;
import icp.core.ICP;
import icp.core.Permissions;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

public final class DatabaseHelper {
  private static final Logger logger = Logger.getLogger("Database");
  private final Connection connection;

  private static class InstanceWrapper {
    private static final DatabaseHelper INSTANCE = new DatabaseHelper();
  }

  public static DatabaseHelper getInstance() {
    return InstanceWrapper.INSTANCE;
  }

  private DatabaseHelper() {
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:users.db");
      initDatabase();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Create tables if they do not already exist.
   */
  private void initDatabase() throws Exception {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_account " +
        "(name TEXT UNIQUE, hash TEXT)");
    } catch (SQLException e) {
      logger.log(SEVERE, e.toString(), e);
      throw new Exception("Database could not initialize");
    }
  }

  public void close() throws SQLException {
    connection.close();
  }

  /**
   * Create new user account with unique name and password.
   *
   * @param name     unique name to identify with
   * @param password password for account
   * @return True if account was created or false if could not create account
   */
  public boolean createAccount(String name, char[] password) {
    //language=SQLite
    String insert = "INSERT INTO user_account " +
      "VALUES(?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(insert)) {
      statement.setString(1, name);
      // Puts hash/salt/algorithm/parameters in one string
      // Good enough for this project...
      String hash = Hash.password(password).create();
      statement.setString(2, hash);
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      if (((SQLiteException) e).getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
        logger.log(WARNING, "createAccount: name ''{0}'' already exists", name);
      } else {
        logger.log(SEVERE, e.toString(), e);
      }
    }

    return false;
  }
}

package edu.unh.letsmeet.engine.security;

import edu.unh.letsmeet.engine.Cookie;
import edu.unh.letsmeet.engine.Middleware;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;
import icp.wrapper.ICPProxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Track browser sessions by using a session cookie for all requests.
 * Every session has a key-value map associated.
 * <p>
 * Sessions are added on demand and usually after a successful login.
 */
public class SessionManager implements Middleware, Serializable {
  private static final long serialVersionUID = -6449014629565276748L;
  private static final Logger logger = Logger.getLogger("SessionManager");

  public static final int SESSION_BYTE_LEN = 32;
  public static final String SESSION_COOKIE_NAME = "sessionid";
  public static final String META_CREATE_SESSION = "create-session";
  public static final String META_DETACHED_SESSION = "create-detached-session";

  private static final SecureRandom secureRandom = new SecureRandom();

  // Session cookie value -> Storage
  // Guarded-by: SessionMap (itself)
  // TODO: Use lock Object for synchronization (non-final field)
  private transient Map<String, SessionStorage> sessionMap;

  // Private -- Only used for serialization
  private Map<String, SessionStorage> sessionMapTarget;

  public static SessionManager readFromStorage(Path path) {
    if (Files.exists(path)) {
      try {
        ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path));
        return (SessionManager) in.readObject();
      } catch (IOException | ClassNotFoundException | ClassCastException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw new RuntimeException("Could not read sessions.dat");
      }
    } else {
      return new SessionManager(true);
    }
  }

  public static void saveToStorage(SessionManager sessions, Path path) throws IOException {
    Files.createDirectories(path.getParent());

    ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path));
    out.writeObject(sessions);
    out.flush();
    out.close();
  }


  {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

//  /**
//   * No-arg constructor for deserialization purposes.
//   */
//  public SessionManager() {
//  }

  /**
   * Normal constructor with not deserialization
   */
  protected SessionManager(boolean nonserialized) {
    sessionMapTarget = new HashMap<>(); // target is exported to instance only for serialization!
    //noinspection unchecked
    sessionMap = ICPProxy.newInstance(Map.class, sessionMapTarget,
      (p, t) -> ICP.setPermission(p, Permissions.getHoldsLockPermission(t)));

  }


  public Entry<String, SessionStorage> generateSessionStorage() {
    String cookie = Utils.generateSecureString(secureRandom, SESSION_BYTE_LEN);
    String expiration = LocalDate.now().plusMonths(1).toString();
    SessionStorage storage = new SessionStorage(true);
    storage.setProtected("expiration", expiration);
    synchronized (sessionMap) {
      sessionMap.put(cookie, storage);
    }
    return new AbstractMap.SimpleEntry<>(cookie, storage);
  }

  public static Entry<String, SessionStorage> generateDetachedSessionStorage() {
    String cookie = Utils.generateSecureString(secureRandom, SESSION_BYTE_LEN);
    String expiration = LocalDate.now().plusMonths(1).toString();
    SessionStorage storage = new SessionStorage(true);
    storage.setProtected("expiration", expiration);
    return new AbstractMap.SimpleEntry<>(cookie, storage);
  }

  /**
   * Retrieve session storage for session id or null if none exist
   *
   * @param sessionId
   * @return Storage or null
   */
  public SessionStorage getSessionStorage(String sessionId) {
    synchronized (sessionMap) {
      return sessionMap.get(sessionId);
    }
  }

  public void removeSession(String sessionId) {
    synchronized (sessionMap) {
      sessionMap.remove(sessionId);
    }
  }

  public void pruneExpired() {
    Iterator<Entry<String, SessionStorage>> iterator = sessionMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, SessionStorage> session = iterator.next();
      String expiration = session.getValue().getItem("expiration");

      LocalDate date;
      try {
        date = LocalDate.parse(expiration);
      } catch (DateTimeParseException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        continue;
      }

      if (LocalDate.now().isAfter(date)) {
        logger.fine("Removing expired session: " + session.getKey());
        iterator.remove();
      }
    }
  }

  @Override
  public Response onRequest(ServerProvider provider, Map<String, Object> meta, Request request) {
    // If session cookie is valid -- put session storage into meta
    Cookie sessionCookie = request.getCookie("sessionid");
    if (sessionCookie != null) {
      String sessionId = Utils.base64Decode(sessionCookie.getValue());
      SessionStorage storage = getSessionStorage(sessionId);
      if (storage == null) {
        logger.log(Level.WARNING, "Suspicious operation: sessionId invalid");
        meta.put("delete-session", true);
      } else {
        meta.put("session-key", sessionId);
        meta.put("session", storage);
      }
    }

    return null;
  }

  @Override
  public void onResponse(ServerProvider provider, Map<String, Object> meta, Response.Builder response, Request request) {
    if (((Boolean) meta.getOrDefault(META_CREATE_SESSION, false))) {
      // Remove previous session
      if (meta.containsKey("session-key")) {
        removeSession((String) meta.get("session-key"));
      }

      Map.Entry<String, SessionStorage> storageEntry = generateSessionStorage();
      response.addCookie(createCookie(storageEntry.getKey(), storageEntry.getValue()));
    } else if (((Boolean) meta.getOrDefault("delete-session", false))) {
      response.removeCookie(SESSION_COOKIE_NAME, "/");
    } else if (meta.containsKey(META_DETACHED_SESSION)) {
      // Register detached generated session storage
      @SuppressWarnings("unchecked") Entry<String, SessionStorage> session =
        (Entry<String, SessionStorage>) meta.get(META_DETACHED_SESSION);
      synchronized (sessionMap) {
        sessionMap.put(session.getKey(), session.getValue());
      }

      response.addCookie(createCookie(session.getKey(), session.getValue()));
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    //noinspection unchecked
    sessionMap = ICPProxy.newInstance(Map.class, sessionMapTarget,
      (p, t) -> ICP.setPermission(p, Permissions.getHoldsLockPermission(t)));
  }

  private static String createCookie(String sessionId, SessionStorage sessionStorage) {
    LocalDate date = LocalDate.parse(sessionStorage.getProtected("expiration"));
    return Utils.createSetCookie(SESSION_COOKIE_NAME,
      Utils.base64Encode(sessionId),
      "/",
      date.atStartOfDay(),
      "httponly");
  }

  // Guarded-by: Itself
  // Uses client-side locking
  public static class SessionStorage implements Serializable {
    private static final long serialVersionUID = 478132322503301305L;
    private transient Map<String, String> storage;
    private final Map<String, String> storageTarget;

    private static final String[] PROTECTED_KEYS = new String[]{"expiration"};

    {
      ICP.setPermission(this, Permissions.getThreadSafePermission());
    }

    SessionStorage(boolean nonserialized) {
      storageTarget = new HashMap<>();
      synchronized (this) {
        //noinspection unchecked
        storage = ICPProxy.newInstance(Map.class, storageTarget,
          (p, t) -> ICP.setPermission(p, Permissions.getHoldsLockPermission(this)));
      }
    }

    public synchronized String getItem(String key) {
      if (isKeyProtected(key)) throw new IllegalArgumentException("Protected key");
      return storage.get(key);
    }

    public synchronized String putItem(String key, String value) {
      if (isKeyProtected(key)) throw new IllegalArgumentException("Protected key");
      return storage.put(key, value);
    }

    synchronized void setProtected(String key, String value) {
      storage.put(key, value);
    }

    synchronized String getProtected(String key) {
      return storage.get(key);
    }

    public synchronized boolean hasItem(String key) {
      if (isKeyProtected(key)) throw new IllegalArgumentException("Protected key");
      return storage.containsKey(key);
    }

    private boolean isKeyProtected(String key) {
      return Stream.of(PROTECTED_KEYS).anyMatch(k -> k.equals(key));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      synchronized (this) {
        //noinspection unchecked
        storage = ICPProxy.newInstance(Map.class, storageTarget,
          (p, t) -> ICP.setPermission(p, Permissions.getHoldsLockPermission(this)));
      }
    }
  }
}

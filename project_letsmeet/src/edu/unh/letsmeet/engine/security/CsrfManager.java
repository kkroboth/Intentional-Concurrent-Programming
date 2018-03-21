package edu.unh.letsmeet.engine.security;

import edu.unh.letsmeet.engine.Middleware;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;
import edu.unh.letsmeet.engine.security.SessionManager.SessionStorage;
import icp.core.ICP;
import icp.core.Permissions;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CSRF Protection.
 * <p>
 * A hashed token is sent to browser and must be sent back for STATE CHANGING
 * requests.
 */
@Deprecated()
public class CsrfManager implements Middleware {
  // TODO: Quick hack -- still not done and shouldn't be used
  // relies on session storage to save token

  private static final Logger logger = Logger.getLogger("CsrfManager");

  public static final String CSRF_COOKIE_NAME = "csrftoken";

  private final boolean setCookie;

  /**
   * @param setCookie If true, will return csrftoken cookie in response when created
   */
  public CsrfManager(boolean setCookie) {
    this.setCookie = setCookie;

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response.Builder onRequest(ServerProvider provider, Map<String, Object> meta, Request request) {
    // Session storage middleware must come before csrf middleware
    if (!meta.containsKey("session")) return null;

    // if csrftoken doesn't exist, create it
    SessionStorage storage = (SessionStorage) meta.get("session");
    String token = storage.getItem("csrftoken");
    if (token == null) {
      try {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        token = Utils.base64Encode(key.getEncoded());

        // Race condition?
        // Check-then-put
        // TODO:
        storage.putItem("csrftoken", token);

        // 1 year expiration
        if (setCookie) {
          LocalDateTime date = LocalDateTime.now().plusYears(1);
          String cookie = Utils.createSetCookie(CSRF_COOKIE_NAME,
            token,
            "/",
            date);

          meta.put("add-csrf-cookie", cookie);
        }
      } catch (NoSuchAlgorithmException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return null;
  }

  @Override
  public void onResponse(ServerProvider provider, Map<String, Object> meta, Response.Builder response, Request request) {
    if (meta.containsKey("add-csrf-cookie")) {
      response.addCookie((String) meta.get("add-csrf-cookie"));
    }
  }
}

package edu.unh.letsmeet.api;

import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.network.http.HttpResponse;
import edu.unh.letsmeet.Constants;
import edu.unh.letsmeet.engine.Settings;
import icp.core.ICP;
import icp.core.Permissions;
import icp.wrapper.ICPProxy;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All registered Api endpoints
 */
public class ApiRegistry {
  private static final Logger logger = Logger.getLogger("ApiRegistry");
  private Map<String, Endpoint> endpoints;
  private Map<String, Endpoint> domainEndpointMap;
  private final HttpHelper httpHelper;

  public ApiRegistry(HttpHelper httpHelper) {
    this.httpHelper = httpHelper;
    //noinspection unchecked
    endpoints = ICPProxy.newPrivateInstance(Map.class, new HashMap<>());
    //noinspection unchecked
    domainEndpointMap = ICPProxy.newPrivateInstance(Map.class, new HashMap<>());

    // Add default apis
    // TODO: Move out and allow *user* to select which apis to register
    for (RequireAuthApi api : RequireAuthApi.values()) {
      endpoints.put(api.name().toLowerCase(), api);
    }

    for (PublicApi api : PublicApi.values()) {
      endpoints.put(api.name().toLowerCase(), api);
    }
  }

  public HttpHelper getHttpHelper() {
    return httpHelper;
  }

  /**
   * Add endpoint and override previous defined endpoint if one exists
   *
   * @param name     Identifier for endpoint
   * @param endpoint
   */
  public void addEndpoint(String name, Endpoint endpoint) {
    endpoints.put(name, endpoint);
    if (domainEndpointMap.put(endpoint.domain(), endpoint) != null) {
      throw new RuntimeException("Endpoint domain: " + endpoint.domain() + " already exists");
    }
  }

  public Endpoint getEndpoint(String name) {
    return endpoints.get(name);
  }

  public ApiHttpRequest startCall(String endpointName) {
    return startCall(endpoints.get(endpointName));
  }

  public ApiHttpRequest startCall(Endpoint endpoint) {
    return startCall(endpoint, null);
  }

  public ApiHttpRequest startCall(Endpoint endpoint, Extractor extractor) {
    Objects.requireNonNull(endpoint);
    return new ApiHttpRequest(endpoint, extractor);
  }

  public ApiHttpRequest startCall(String endpointName, Extractor extractor) {
    return startCall(endpoints.get(endpointName), extractor);
  }

  /**
   * Finalizes endpoints to immutable and performs api key lookups.
   */
  @SuppressWarnings("unchecked")
  public void completeRegistration(Settings settings) {
    ICPProxy.setProxyPermission(endpoints, Permissions.getFrozenPermission());
    ICPProxy.setProxyPermission(domainEndpointMap, Permissions.getFrozenPermission());
    endpoints = ICPProxy.newFrozenInstance(Map.class, Collections.unmodifiableMap(endpoints));
    domainEndpointMap = ICPProxy.newFrozenInstance(Map.class, Collections.unmodifiableMap(domainEndpointMap));

    Map<String, String> keys = settings.get(Constants.SETTING_APIKEYS);
    if (keys == null) {
      logger.warning("Settings does not contain api keys");
      return;
    }
    for (Map.Entry<String, Endpoint> entry : endpoints.entrySet()) {
      String name = entry.getKey().toLowerCase();
      Endpoint endpoint = entry.getValue();

      if (endpoint instanceof ReadableApiKey) {
        ReadableApiKey readableApiKey = ((ReadableApiKey) endpoint);

        // Check if class uses a custom name instead of its class's name for lookup
        String propName = "key_" + name;
        try {
          Class klass = readableApiKey.getClass();
          Field field = klass.getField("API_KEY_NAME");
          if (field.getType().equals(String.class)) {
            propName = (String) field.get(null);
          }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
          // don't change propName
        }

        if (!keys.containsKey(propName)) {
          logger.log(Level.WARNING, "Api key name: {0} is not in settings", name);
          continue;
        }

        readableApiKey.setApiKey(keys.get(propName));
      }
    }

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public static class BulkCall {
    private final Map<String, HttpResponse> calls;

    BulkCall() {
      //noinspection unchecked
      calls = ICPProxy.newPrivateInstance(Map.class, new HashMap<>());
    }

    public ApiHttpRequest startCall(String identifier, Endpoint endpoint, Extractor extractor) {
      return null;
    }
  }
}

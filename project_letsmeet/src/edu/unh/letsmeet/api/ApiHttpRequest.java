package edu.unh.letsmeet.api;

import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.network.http.HttpRequest;
import com.krobothsoftware.commons.network.http.HttpResponse;
import com.krobothsoftware.commons.network.http.Method;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.util.function.Consumer;


/**
 * Specific HttpRequest used with api calls.
 * <p>
 * Uses URLBuilder instead of java.net.URL.
 * <p>
 * Uses a String identifier for Method types instead of Method enum as there are
 * conflicts in naming. Avoids long import names.
 *
 * @see edu.unh.letsmeet.api
 */
public class ApiHttpRequest extends HttpRequest {
  private final URLBuilder urlBuilder;
  private final Endpoint endpoint;
  private Extractor extractor;

  ApiHttpRequest(Endpoint endpoint) {
    this(endpoint, null);
  }

  ApiHttpRequest(Endpoint endpoint, Extractor extractor) {
    super(Method.GET, null);
    this.endpoint = endpoint;
    this.urlBuilder = URLBuilder.ofEndpoint(endpoint);
    this.extractor = extractor;

    ICP.setPermission(urlBuilder, Permissions.getSamePermissionAs(this));
  }

  public ApiHttpRequest method(String method) {
    this.method = Method.valueOf(method.toUpperCase());
    return this;
  }

  public ApiHttpRequest buildUrl(Consumer<URLBuilder> consumer) {
    consumer.accept(urlBuilder);
    return this;
  }

  public URLBuilder buildUrl() {
    return urlBuilder;
  }

  @Override
  public ApiHttpResponse execute(HttpHelper httpHelper) throws IOException {
    endpoint.onRequest(this);
    this.url = urlBuilder.buildURL();
    HttpResponse response = super.execute(httpHelper);
    // Convert to ApiResponse and use optional extractor to parse json
    ApiHttpResponse apiResponse = new ApiHttpResponse(response.getConnection(),
      response.getStream(), response.getStatusCode(), response.getCharset());
    if (extractor != null) {
      apiResponse.parse(extractor);
    }

    return apiResponse;
  }
}

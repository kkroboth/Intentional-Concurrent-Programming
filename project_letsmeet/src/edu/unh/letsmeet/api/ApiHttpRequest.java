package edu.unh.letsmeet.api;

import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.network.http.HttpRequest;
import com.krobothsoftware.commons.network.http.HttpResponse;
import com.krobothsoftware.commons.network.http.Method;

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

  ApiHttpRequest(Endpoint endpoint) {
    super(Method.GET, null);
    this.endpoint = endpoint;
    this.urlBuilder = URLBuilder.ofEndpoint(endpoint);
  }

  public ApiHttpRequest method(String method) {
    this.method = Method.valueOf(method.toUpperCase());
    return this;
  }

  public ApiHttpRequest buildUrl(Consumer<URLBuilder> consumer) {
    consumer.accept(urlBuilder);
    return this;
  }

  @Override
  public HttpResponse execute(HttpHelper httpHelper) throws IOException {
    endpoint.onRequest(this);
    this.url = urlBuilder.buildURL();
    return super.execute(httpHelper);
  }
}

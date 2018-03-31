package edu.unh.letsmeet.api;

import com.google.gson.JsonElement;
import com.krobothsoftware.commons.network.http.HttpResponse;
import com.krobothsoftware.commons.util.UnclosableInputStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;

public class ApiHttpResponse extends HttpResponse {
  private JsonElement extractedJson;

  public ApiHttpResponse(HttpURLConnection connection, UnclosableInputStream input, int status, String charset) {
    super(connection, input, status, charset);
  }

  void parse(Extractor extractor) throws IOException {
    extractedJson = extractor.parseContent(getStream());
  }

  public Optional<JsonElement> getExtractedJson() {
    return Optional.ofNullable(extractedJson);
  }
}

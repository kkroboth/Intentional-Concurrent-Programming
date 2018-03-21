package edu.unh.letsmeet;

import edu.unh.letsmeet.api.ApiHelper;
import edu.unh.letsmeet.engine.ServerProvider;

public interface ServiceProvider extends ServerProvider {

  ApiHelper getApiHelper();
}

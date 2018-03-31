package edu.unh.letsmeet;

import edu.unh.letsmeet.api.ApiRegistry;
import edu.unh.letsmeet.engine.ServerProvider;

public interface ServiceProvider extends ServerProvider {

  ApiRegistry getApiRegistry();
}

package com.oup.apiproxy.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Service;;

@Service
@RefreshScope
public class DummyService {
	
	@Autowired
	private RouteLocator customRouteLocator;

}

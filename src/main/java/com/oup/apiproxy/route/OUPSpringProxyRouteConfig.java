package com.oup.apiproxy.route;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.discovery.EurekaClient;

@Configuration
@RefreshScope
public class OUPSpringProxyRouteConfig {
	Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	@Autowired
	private EurekaClient discoveryClient;

	@Value("${spring.application.name}")
	private String appName;

	@RefreshScope
	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		Builder bldr = builder.routes();
		discoveryClient.getApplications().getRegisteredApplications().forEach(item -> {
			String applicationName = item.getName();

			if (!applicationName.equalsIgnoreCase(appName))

			{
				logger.info("Binding service: " + applicationName);

				bldr.route(r -> r.path("/" + applicationName + "/**")
						.filters(f -> f.rewritePath("/" + applicationName + "/(?<path>.*)", "/$\\{path}"))
						.uri("lb://" + applicationName + "").id(applicationName));
			}
		});		
		
		//bldr.route(r -> r.path("/uam**")
		//		.filters(f -> f.rewritePath("/uam/(?<path>.*)", "/$\\{path}"))
		//		.uri("https://127.0.0.1").id("UAM"));
		return bldr.build();
	}
}

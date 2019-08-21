package com.oup.apiproxy.route;

import java.io.IOException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
@RefreshScope
public class OUPSpringProxyRouteConfig {
	Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment environment;

	@Value("${spring.application.name}")
	private String appName;

	@RefreshScope
	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) throws IOException, ApiException {
		logger.info("RELOADING ROUTES");
		Builder bldr = builder.routes();
		ApiClient client = Config.defaultClient();
		io.kubernetes.client.Configuration.setDefaultApiClient(client);

		CoreV1Api api = new CoreV1Api();
		api.listNamespacedService(environment.getActiveProfiles()[0], null, null, null, null, null, null, null, null,
				null).getItems().forEach(item -> {
					String applicationName = item.getMetadata().getName();
					if (!applicationName.equalsIgnoreCase(appName))

			{
						logger.info("Binding service: " + applicationName);

						bldr.route(r -> r.path("/" + applicationName + "/**")

								.filters(f -> f.rewritePath("/" + applicationName + "/(?<path>.*)", "/$\\{path}")
										.removeRequestHeader("Expect"))
								.uri("http://" + applicationName + "").id(applicationName));
						//----------------------------------------------------------------------------------------
						//Start: Build route explicitly to be called from api gateway						
						//----------------------------------------------------------------------------------------
						bldr.route(r -> r.path("/apigw/" + applicationName + "/**")

								.filters(f -> f.rewritePath("/apigw/" + applicationName + "/(?<path>.*)", "/$\\{path}")
										.removeRequestHeader("Expect"))
								.uri("http://" + applicationName + "").id(applicationName+"_apigw"));
						//----------------------------------------------------------------------------------------
						//End: Build route explicitly to be called from api gateway						
						//----------------------------------------------------------------------------------------

					}
				});
		
		return bldr.build();
	}
}

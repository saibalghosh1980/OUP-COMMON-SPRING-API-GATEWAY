package com.oup.apiproxy.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.oup.apiproxy.bl.UserRoleBL;
import com.oup.apiproxy.dto.Role;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
@RefreshScope
public class SecurityConfig {

	private ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<Object>() {
		public <T> T postProcess(T object) {
			return object;
			/*
			 * throw new IllegalStateException( ObjectPostProcessor.class.getName() +
			 * " is a required bean. Ensure you have used @EnableWebSecurity and @Configuration"
			 * );
			 */
		}
	};
	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment environment;

	@Autowired
	ApplicationContext context;

	@Autowired
	@Qualifier("springManagedUserBL")
	private UserRoleBL userRoleBL;

	@SuppressWarnings("deprecation")
	@Bean
	ReactiveAuthenticationManagerAdapter authenticationManager(ApplicationContext context) throws Exception {

		this.context = context;

		ObjectPostProcessor<Object> objectPostProcessor = this.objectPostProcessor; // context.getBean(ObjectPostProcessor.class);
		// LazyPasswordEncoder passwordEncoder = new LazyPasswordEncoder(context);

		AuthenticationManagerBuilder authenticationBuilder = new DefaultPasswordEncoderAuthenticationManagerBuilder(
				objectPostProcessor, new BCryptPasswordEncoder());

		authenticationBuilder.jdbcAuthentication().dataSource(dataSource)
				.usersByUsernameQuery(
						"SELECT username,password, IF(enabled,'true','false') enabled FROM users WHERE username = ?")
				.authoritiesByUsernameQuery(
						"SELECT username,trim(authority) AS authorities FROM authorities WHERE username = ?");

		return new ReactiveAuthenticationManagerAdapter(authenticationBuilder.build());

	}
	

	@RefreshScope
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) throws IOException, ApiException {
		
		AuthorizeExchangeSpec authorizeExchangeSpec = http.csrf().disable().authorizeExchange()
				.pathMatchers("**/*wsdl").permitAll()
				.pathMatchers("/**/*wsdl").permitAll()
				.pathMatchers("/actuator/**").permitAll() // NO SECURITY FOR ACTUATOR ENDPOINT
				.pathMatchers("**/actuator/**").permitAll().pathMatchers("**/prometheus/**").permitAll()
				.pathMatchers("**/refresh/**").permitAll().pathMatchers("/health/**").permitAll()
				.pathMatchers("**/hawtio/**").permitAll()
				.pathMatchers("/apigw/**").permitAll()
				// .pathMatchers("/actuator/**").hasAuthority("ROLE_ACTUATOR")
				// .pathMatchers("**/actuator/**").hasAuthority("ROLE_ACTUATOR")
				.pathMatchers("/uam/**").hasAuthority("ROLE_UAM_ADMIN");

		try {
			userRoleBL.addUserIfDoesNotExists(new com.oup.apiproxy.dto.User("USER_UAM_ADMIN", "Passw0rd@123"));
			userRoleBL.addRoleForUserIfDoesNotExists(new Role("USER_UAM_ADMIN", "ROLE_UAM_ADMIN"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ApiClient client = Config.defaultClient();
		io.kubernetes.client.Configuration.setDefaultApiClient(client);
		CoreV1Api api = new CoreV1Api();
		api.listNamespacedService(environment.getActiveProfiles()[0], null, null, null, null, null, null, null, null,
				null).getItems().forEach(svcItem -> {
					String item = svcItem.getMetadata().getName();
					authorizeExchangeSpec.pathMatchers("/" + item + "/actuator/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "/prometheus/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "/refresh/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "/env/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "/hawtio/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "**/jolokia/**").permitAll();
					authorizeExchangeSpec.pathMatchers("/" + item + "/**").hasAuthority("ROLE_" + item.toUpperCase());
					// ------------------Add user if not there for this
					// endpoint------------------------------
					try {
						userRoleBL.addUserIfDoesNotExists(
								new com.oup.apiproxy.dto.User("USER_" + item.toUpperCase(), "Passw0rd@123"));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// ------------------Add Role if not there for this
					// endpoint------------------------------
					try {
						userRoleBL.addRoleForUserIfDoesNotExists(
								new Role("USER_" + item.toUpperCase(), "ROLE_" + item.toUpperCase()));
						System.out.println("User and Role added with default pwd Passw0rd@123 ('USER_"
								+ item.toUpperCase() + "','ROLE_" + item.toUpperCase() + "');");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});

		return authorizeExchangeSpec.anyExchange().authenticated().and().httpBasic().and().build();
	}

	static class DefaultPasswordEncoderAuthenticationManagerBuilder extends AuthenticationManagerBuilder {
		private PasswordEncoder defaultPasswordEncoder;

		/**
		 * Creates a new instance
		 *
		 * @param objectPostProcessor
		 *            the {@link ObjectPostProcessor} instance to use.
		 */
		DefaultPasswordEncoderAuthenticationManagerBuilder(ObjectPostProcessor<Object> objectPostProcessor,
				PasswordEncoder defaultPasswordEncoder) {
			super(objectPostProcessor);
			this.defaultPasswordEncoder = defaultPasswordEncoder;
		}

		@Override
		public InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication()
				throws Exception {
			return super.inMemoryAuthentication().passwordEncoder(this.defaultPasswordEncoder);
		}

		@Override
		public JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> jdbcAuthentication() throws Exception {
			return super.jdbcAuthentication().passwordEncoder(this.defaultPasswordEncoder);
		}

		@Override
		public <T extends UserDetailsService> DaoAuthenticationConfigurer<AuthenticationManagerBuilder, T> userDetailsService(
				T userDetailsService) throws Exception {
			return super.userDetailsService(userDetailsService).passwordEncoder(this.defaultPasswordEncoder);
		}
	}

}

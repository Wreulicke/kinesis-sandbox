package com.example.helloworld;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.resources.ProtectedResource;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldService extends Application<HelloWorldConfiguration> {
	
	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
			new HibernateBundle<HelloWorldConfiguration>(Person.class) {
				@Override
				public PooledDataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
					return configuration.getDataSourceFactory();
				}
			};
	
	
	@Override
	public String getName() {
		return "hello-world";
	}
	
	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		bootstrap.addCommand(new RenderCommand());
//		bootstrap.addBundle(new AssetsBundle());
		bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
			@Override
			public PooledDataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
	}
	
	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) throws Exception {
		final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
		
		environment.jersey().register(new AuthDynamicFeature(
				new BasicCredentialAuthFilter.Builder<User>()
					.setAuthenticator(new ExampleAuthenticator())
					.setAuthorizer(new ExampleAuthorizer())
					.setRealm("SUPER SECRET STUFF")
					.buildAuthFilter()));
		
		final Template template = configuration.buildTemplate();
		environment.healthChecks().register("template", new TemplateHealthCheck(template));
		
		environment.jersey().register(new HelloWorldResource(template));
		environment.jersey().register(new ProtectedResource());
		environment.jersey().register(new PeopleResource(dao));
		environment.jersey().register(new PersonResource(dao));
		environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
	}
	
	public static void main(String[] args) throws Exception {
		new HelloWorldService().run(args);
	}
	
}

package com.github.wreulicke.dropwizard;

import com.github.wreulicke.dropwizard.auth.ExampleAuthenticator;
import com.github.wreulicke.dropwizard.auth.ExampleAuthorizer;
import com.github.wreulicke.dropwizard.core.User;
import com.github.wreulicke.dropwizard.health.SimpleHealthCheck;
import com.github.wreulicke.dropwizard.job.Job;
import com.github.wreulicke.dropwizard.job.JobDao;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldService extends Application<HelloWorldConfiguration> {

  private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
    new HibernateBundle<HelloWorldConfiguration>(Job.class) {
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
    // bootstrap.addBundle(new AssetsBundle());
    bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
      @Override
      public PooledDataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
        return configuration.getDataSourceFactory();
      }
    });

    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)));

    bootstrap.addBundle(hibernateBundle);
  }

  @Override
  public void run(HelloWorldConfiguration configuration, Environment environment) throws Exception {
    final JobDao dao = new JobDao(hibernateBundle.getSessionFactory());

    environment.jersey()
      .register(new AuthDynamicFeature(
        new BasicCredentialAuthFilter.Builder<User>()
          .setAuthenticator(new ExampleAuthenticator())
          .setAuthorizer(new ExampleAuthorizer())
          .setRealm("SUPER SECRET STUFF")
          .buildAuthFilter()));

    environment.healthChecks()
      .register("template", new SimpleHealthCheck());


    configuration.getJobConfigurer()
      .configure(environment, dao);

    environment.jersey()
      .register(new AuthValueFactoryProvider.Binder<>(User.class));

  }

  public static void main(String[] args) throws Exception {
    new HelloWorldService().run(args);
  }

}

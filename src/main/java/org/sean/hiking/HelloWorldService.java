package org.sean.hiking;

import org.sean.hiking.mapdata.MapDataResource;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.place.PlaceResource;
import org.sean.hiking.place.PlacesDao;
import org.sean.hiking.route.RouteManager;
import org.sean.hiking.route.RouteResource;
import org.sean.hiking.route.RoutesDao;
import org.sean.hiking.trip.TripDao;
import org.sean.hiking.trip.TripManager;
import org.sean.hiking.trip.TripResource;
import org.sean.hiking.user.UserDao;
import org.sean.hiking.user.UserManager;
import org.sean.hiking.user.UserResource;
import org.skife.jdbi.v2.DBI;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;

public class HelloWorldService extends Service<HelloWorldConfiguration> {
	public static DBI jdbi;
	
    public static void main(String[] args) throws Exception {
        new HelloWorldService().run(args);
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.setName("hello-world");
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) throws Exception {
        final String template = configuration.getTemplate();
        final String defaultName = configuration.getDefaultName();

        final DBIFactory factory = new DBIFactory();
        jdbi = factory.build(environment, configuration.getDatabaseConfiguration(), "mysql");
        final PlacesDao placesDao = jdbi.onDemand(PlacesDao.class);
        final RoutesDao routesDao = jdbi.onDemand(RoutesDao.class);
        final UserDao userDao = jdbi.onDemand(UserDao.class);
        final TripDao tripDao = jdbi.onDemand(TripDao.class);
        final UserManager userManager = new UserManager(userDao);
        final PlaceManager placeManager = new PlaceManager(placesDao);
        final RouteManager routeManager = new RouteManager(routesDao);
        final TripManager tripManager = new TripManager(tripDao, routeManager, placeManager, userManager);
        
        placeManager.setRouteManager(routeManager);
        routeManager.setPlaceManager(placeManager);
        routeManager.setTripManager(tripManager);
        
        environment.addResource(new PlaceResource(placeManager, routeManager, userManager));
        environment.addResource(new RouteResource(routeManager, placeManager, userManager));
        environment.addResource(new MapDataResource(placeManager, routeManager));
        environment.addResource(new TripResource(routeManager, placeManager, tripManager, userManager));
        environment.addResource(new UserResource(userManager));
        environment.addHealthCheck(new TemplateHealthCheck(template));
    }

}

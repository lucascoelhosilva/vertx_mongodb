package com.coelho.vertx_mongodb.verticles;

import com.coelho.vertx_mongodb.constants.Constants;
import com.coelho.vertx_mongodb.handlers.HealthHandler;
import com.coelho.vertx_mongodb.handlers.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class UserEndpointVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpointVerticle.class);

  private HttpServer httpServer;
  private MongoClient mongoClient;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    mongoClient = MongoClient.createShared(vertx, config().getJsonObject(Constants.MONGO_KEY));
  }

  @Override
  public void start(Future<Void> future) {
    LOGGER.info("Starting Endpoint Verticle");

    final UserHandler userHandler = new UserHandler(mongoClient);
    final HealthHandler healthHandler = new HealthHandler(getVertx(), mongoClient);

    Router subRouter = Router.router(getVertx());
//    subRouter.route().handler(RequestHelper::validateCustomerIdHeader);
    subRouter.get("/users").handler(userHandler::findAll);
    subRouter.get("/users/count").handler(userHandler::count);
    subRouter.get("/users/:id").handler(userHandler::findById);
    subRouter.delete("/users/:id").handler(userHandler::deleteById);
    subRouter.put("/users/:id").handler(userHandler::updateById);

    Router mainRouter = Router.router(getVertx());
    mainRouter.mountSubRouter(config().getString("basePath", Constants.BASE_PATH), subRouter);

    mainRouter.route("/health*").handler(healthHandler.health());

    httpServer = getVertx().createHttpServer().requestHandler(mainRouter::handle)
      .listen(config().getInteger("http.port", Constants.HTTP_SERVER_PORT), ar -> {
        if (ar.succeeded()) {
          future.complete();
          LOGGER.info("HTTP Server running at port {0}", String.valueOf(ar.result().actualPort()));
        } else {
          future.fail(ar.cause().getMessage());
        }
      });
  }

  @Override
  public void stop() {
    LOGGER.info("Stoping Endpoint Verticle");
    mongoClient.close();
    httpServer.close();
  }

}

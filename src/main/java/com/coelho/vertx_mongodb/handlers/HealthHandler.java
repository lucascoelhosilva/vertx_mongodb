package com.coelho.vertx_mongodb.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.mongo.MongoClient;

public class HealthHandler {

  private final Vertx vertx;
  private final MongoClient mongoClient;
  private final JsonObject pingMongoDB;

  public HealthHandler(Vertx vertx, MongoClient mongoClient) {
    this.vertx = vertx;
    this.mongoClient = mongoClient;
    this.pingMongoDB = new JsonObject().put("ping", 1);
  }

  public HealthCheckHandler health() {
    return HealthCheckHandler.create(vertx)
      .register("api-users", ar -> ar.complete(Status.OK()))
      .register("mongoDB", ar -> mongoClient.runCommand("ping", pingMongoDB, connection ->
      {
        if (connection.failed()) {
          ar.fail(connection.cause());
        } else {
          ar.complete(Status.OK());
        }
      }));
  }
}

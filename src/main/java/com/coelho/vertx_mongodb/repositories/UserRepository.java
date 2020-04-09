package com.coelho.vertx_mongodb.repositories;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class UserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

  private final MongoClient mongoClient;

  public UserRepository(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public void queryList(String collection, FindOptions findOptions, JsonObject jsonQuery, Handler<AsyncResult<List<JsonObject>>> handler) {
    LOGGER.info("[queryList] collection => " + collection);
    LOGGER.info("[queryList] jsonQuery => " + jsonQuery);
    LOGGER.info("[queryList] findOptions => " + findOptions.toJson());

    mongoClient.find(collection, jsonQuery, mongoHandler -> {
      if (mongoHandler.succeeded()) {
        handler.handle(Future.succeededFuture(mongoHandler.result()));
      } else {
        LOGGER.error("Failed to search: {0}", mongoHandler.cause().getMessage());
        handler.handle(Future.failedFuture(mongoHandler.cause()));
      }
    });
  }

  public void querySingle(String collection, JsonObject jsonQuery, Handler<AsyncResult<JsonObject>> handler) {
    LOGGER.info("[querySingle] collection => " + collection);
    LOGGER.info("[querySingle] jsonQuery => " + jsonQuery);

    mongoClient.findOne(collection, jsonQuery, null, mongoHandler -> {
      if (mongoHandler.succeeded()) {
        handler.handle(Future.succeededFuture(mongoHandler.result()));
      } else {
        LOGGER.error("Failed to search: {0}", mongoHandler.cause().getMessage());
        handler.handle(Future.failedFuture(mongoHandler.cause()));
      }
    });
  }

  public void delete(String collection, JsonObject jsonQuery, Handler<AsyncResult<Void>> handler) {
    LOGGER.info("[delete] collection => " + collection);
    LOGGER.info("[delete] jsonQuery => " + jsonQuery);

    mongoClient.findOneAndDelete(collection, jsonQuery, mongoHandler -> {
      if (mongoHandler.succeeded()) {
        LOGGER.debug("Removed user: {0}", mongoHandler.result());
        handler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Failed to delete: {0}", mongoHandler.cause().getMessage());
        handler.handle(Future.failedFuture(mongoHandler.cause()));
      }
    });
  }

  public void count(String collection, JsonObject jsonQuery, Handler<AsyncResult<String>> handler) {
    LOGGER.info("[count] collection => " + collection);
    LOGGER.info("[count] jsonQuery => " + jsonQuery);

    mongoClient.count(collection, jsonQuery, mongoHandler -> {
      if (mongoHandler.succeeded()) {
        handler.handle(Future.succeededFuture(mongoHandler.result().toString()));
      } else {
        LOGGER.error("Failed to count: {0}", mongoHandler.cause().getMessage());
        handler.handle(Future.failedFuture(mongoHandler.cause()));
      }
    });
  }

  public void create(String collection, JsonObject user) {
    mongoClient.save(collection, user, mongoResult -> {
      if (mongoResult.succeeded()) {
        LOGGER.debug("Persisted into collection: {0}", collection);
      } else {
        LOGGER.error("Failed to insert/update user: {0}", mongoResult.cause().getMessage());
      }
    });
  }

  public void update(String collection, String id, JsonObject user) {
    LOGGER.debug("user", user);
    final JsonObject query = new JsonObject().put("_id", id);

    mongoClient.findOneAndUpdate(collection, query, user, mongoResult -> {
      if (mongoResult.succeeded()) {
        LOGGER.info("Persisted into collection: {0}", collection);
      } else {
        LOGGER.info("Failed to insert/update user: {0}", mongoResult.cause().getMessage());
      }
    });
  }
}

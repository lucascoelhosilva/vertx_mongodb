package com.coelho.vertx_mongodb.handlers;

import com.coelho.vertx_mongodb.constants.Constants;
import com.coelho.vertx_mongodb.repositories.UserRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class UserHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

  private final UserRepository userRepository;

  public UserHandler(MongoClient mongoClient) {
    this.userRepository = new UserRepository(mongoClient);
  }

  public void findAll(RoutingContext rc) {
    userRepository.queryList(Constants.MONGO_COLLECTION, options(rc), buildFindQuery(rc), handler -> {
      if (handler.succeeded()) {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(Json.encodePrettily(handler.result()));
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void findById(RoutingContext rc) {
    userRepository.querySingle(Constants.MONGO_COLLECTION, buildFindQuery(rc), handler -> {
      if (handler.succeeded()) {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(Json.encodePrettily(handler.result()));
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void count(RoutingContext rc) {
    userRepository.count(Constants.MONGO_COLLECTION, buildFindQuery(rc), handler -> {
      if (handler.succeeded()) {
        rc.response().setStatusCode(HttpResponseStatus.OK.code()).end(handler.result());
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void deleteById(RoutingContext rc) {
    userRepository.delete(Constants.MONGO_COLLECTION, buildFindQuery(rc), handler -> {
      if (handler.succeeded()) {
        rc.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void create(RoutingContext rc) {
    LOGGER.info("Object ======= " + rc.getBodyAsJson());
    userRepository.create(Constants.MONGO_COLLECTION, rc.getBodyAsJson());
    rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
  }

  public void updateById(RoutingContext rc) {
    LOGGER.info("Object ======= " + rc.getBodyAsJson());
    JsonObject user = new JsonObject().put("$set", rc.getBodyAsJson());
    userRepository.update(Constants.MONGO_COLLECTION, rc.pathParam("id"), user);
    rc.response().setStatusCode(HttpResponseStatus.OK.code()).end();
  }

  private FindOptions options(RoutingContext rc) {
    FindOptions findOptions = new FindOptions();

    Optional<String> skipOpt = rc.queryParam("skip").stream().findFirst();
    findOptions.setSkip(skipOpt.map(Integer::valueOf).orElse(0));

    Optional<String> limitOpt = rc.queryParam("limit").stream().findFirst();
    findOptions.setLimit(limitOpt.map(Integer::valueOf).orElse(10));

    return findOptions;
  }

  private JsonObject buildFindQuery(RoutingContext rc) {
    LOGGER.debug("Request: {0}", rc.request().absoluteURI());

    JsonObject object = new JsonObject();

    Optional<String> userName = rc.queryParam("name").stream().findFirst();
    userName.ifPresent(s -> object.put("name", s));

    String userId = rc.pathParam("userId");
    if(userId != null && !userId.equals("")){
      object.put("_id", userId);
    }

    return object;
  }
}

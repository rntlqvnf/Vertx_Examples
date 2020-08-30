package com.yshajae.vertx.example2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.impl.HttpStatusException;

public class MainVerticle extends AbstractVerticle {
  private JWTAuth provider;

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initProvider();

    Router router = Router.router(vertx);
    
    router.route().handler(BodyHandler.create());
    router.route().handler(this::rootHandler);
    router.get("/login").handler(this::generateToken);

    router.get("/user").handler(JWTAuthHandler.create(provider));
    router.get("/user").handler(this::getUsers);
    router.get("/user/:id").handler(this::getById);

    router.route().failureHandler(this::failureHandler);

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void generateToken(RoutingContext context){
    String token = provider.generateToken(new JsonObject().put("name", "하재현").put("studentId", 20180000));
    context.response()
      .setStatusCode(200)
      .putHeader("content-type", "text/plain; charset=utf-8")
      .end(token);
  }

  private void rootHandler(RoutingContext context){
    context.put("name", "하재현").next();
  }

  private void getUsers(RoutingContext context) {
    JsonObject user = context.user().principal();

    context.response()
      .setStatusCode(200)                
      .putHeader("content-type", "text/plain; charset=utf-8")
      .end("AuthN pass : "+user.getString("name")+", "+user.getInteger("studentId"));
  }

  private void getById(RoutingContext context) {
    String id = context.pathParam("id");
    context.response()
      .setStatusCode(200)                
      .putHeader("content-type", "text/plain; charset=utf-8")
      .end("Got User " + id + " !" + "By " + context.get("name"));
  }

  private void initProvider(){
    provider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setPublicKey("ppppaaaassssswwwwoooorrrdddd")
        .setSymmetric(true)));
  }

  private void failureHandler(RoutingContext context){
    int statusCode = context.statusCode();
    String errorMessage;
    if(context.failure() instanceof HttpStatusException){
        errorMessage = "인증이 만료되었으니 다시 하시길 바랍니다.";
    }else{
        errorMessage = "Unkown Error";
    }
    JsonObject errInfo = new JsonObject().put("err", errorMessage);

    context.response()
      .setStatusCode(statusCode)                
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(errInfo.encodePrettily());
  }
}

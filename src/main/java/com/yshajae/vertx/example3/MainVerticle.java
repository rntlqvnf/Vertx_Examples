package com.yshajae.vertx.example3;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new ServiceVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/login").handler(this::login);

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void login(RoutingContext context){
    vertx.eventBus().request("login", context.getBodyAsJson(), reply -> {
      if(reply.succeeded()){
        context.response()
          .setStatusCode(200)                
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(new JsonObject().put("name", ((String) reply.result().body())).encodePrettily());
      }else{
        reply.cause().printStackTrace();
        context.response()
          .setStatusCode(400)                
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(new JsonObject().put("err", "로그인 실패").encodePrettily());
      }
    });
  }
}

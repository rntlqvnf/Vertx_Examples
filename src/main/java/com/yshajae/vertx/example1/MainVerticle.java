package com.yshajae.vertx.example1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    
    router.route().handler(BodyHandler.create()); 

    router.get("/user").handler(this::getUsers);
    router.get("/user/:id").handler(this::getById);

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void getUsers(RoutingContext context) {
    context.response()
      .setStatusCode(200)                
      .putHeader("content-type", "text/plain; charset=utf-8")
      .end("Got Users!");
  }
  private void getById(RoutingContext context) {
    String id = context.pathParam("id");
    context.response()
      .setStatusCode(200)                
      .putHeader("content-type", "text/plain; charset=utf-8")
      .end("Got User " + id + " !" );
  }
}

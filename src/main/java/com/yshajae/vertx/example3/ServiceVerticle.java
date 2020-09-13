package com.yshajae.vertx.example3;

import java.lang.reflect.Method;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

public class ServiceVerticle extends AbstractVerticle {
    MySQLPool client;
  
    @Override
    public void start() throws Exception {
      MySQLConnectOptions connectOptions = new MySQLConnectOptions()
        .setPort(3306)
        .setHost("localhost")
        .setDatabase("test")
        .setUser("root")
        .setPassword("root");
  
      PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(5);
  
      client = MySQLPool.pool(vertx, connectOptions, poolOptions);

      vertx.eventBus().consumer("login").handler((message) -> {
          try {
              Method m = this.getClass().getDeclaredMethod("login", Message.class);
              m.invoke(this, message);
          } catch (Exception e) {
              e.printStackTrace();
          }
      });
    }

    public void login(Message<JsonObject> message){
        String id = message.body().getString("id");
        String pass = message.body().getString("pass");

        client
        .preparedQuery(
            "SELECT name FROM users WHERE id=? AND pass=?"
        )
        .execute(Tuple.of(id, pass), result -> {
            if(result.succeeded()){
                if(result.result().size() == 1){
                    String name = result.result().iterator().next().getString("name");
                    message.reply(name);
                }else{
                    message.fail(-2, "유효하지 않은 로그인 정보");
                }
            }else{
                message.fail(-1, "쿼리 실패");
            }
        });
    }
}

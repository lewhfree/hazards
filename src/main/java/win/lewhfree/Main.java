package win.lewhfree;

import java.sql.SQLException;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql:testdb";

        Javalin app = Javalin.create(config -> {
            config.routes.get("/ident", ctx -> {
                ctx.status(201);
                System.out.println("Ident pinged");
            });
        }).start(6742);


        try {
            java.sql.Connection connection = java.sql.DriverManager.getConnection(url);
            System.out.println("Connected successfully");
        } catch (SQLException e) {
            System.out.println("Failed");
            e.printStackTrace();
        }

        while (true){

        }
    }
}
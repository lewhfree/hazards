package win.lewhfree;

import java.sql.*;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.javalin.json.JavalinJackson;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Call with an arg");
            System.exit(1);
        } else {
            System.out.println(args[0]);
        }

        switch (args[0]) {
            case "all":
                try {
                    frontend();
                    System.out.println("Nonblocking frontend");
                } catch(Exception e) {
                    System.exit(1);
                }
                break;
            default:
                System.out.println("Not an applicable arg");
                System.exit(1);
        }
    }

    static void frontend() throws Exception {
        String url = "jdbc:postgresql:testdb";
        final Connection connection;
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connected successfully");
        } catch (Exception e) {
            System.out.println("Failed");
            e.printStackTrace();
            throw new Exception(e);
        }

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson());
            config.routes.get("/ident", ctx -> {
                String make = ctx.queryParam("brand");
                List<Map<String, Object>> carreq = request(connection, make);
                ctx.json(carreq);
                ctx.status(201);
                System.out.println("Ident pinged");
            });
        }).start(6742);
        // put a hello world in the db

    }

    static List<Map<String, Object>> request(Connection connection, String brand) throws Exception{
        Statement st = connection.createStatement();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM cars WHERE brand = ?");
        ps.setString(1, brand);
        ResultSet rs = ps.executeQuery();
        List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

        while(rs.next()) {
            String branddb = rs.getString("brand");
            String model = rs.getString("model");
            Integer year = rs.getInt("year");
            values.add(Map.of("brand", branddb, "model", model, "year", year));
        }

        rs.close();
        st.close();
        return values;
    }
}
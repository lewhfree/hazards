package win.lewhfree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseBackend {
    private static Connection connection;
    private static ObjectMapper mapper;

    public static void setupDB() throws Exception{
        String url = "jdbc:postgresql:testdb";
        connection = DriverManager.getConnection(url);
    }

    public static void setupMapper() throws Exception {
        mapper = new ObjectMapper();
    }

    public static ObjectNode parseBoundedBox(float lon1, float lat1, float lon2, float lat2) throws Exception{
        String sql = """
                SELECT id, type, ST_AsGeoJSON(geometry) AS geojson, raw, source_id, source FROM data
                WHERE ST_Intersects(ST_MakeEnvelope(?, ?, ?, ?, 4326), geometry) = TRUE;
                """;
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setFloat(1, lon1);
        ps.setFloat(2, lat1);
        ps.setFloat(3, lon2);
        ps.setFloat(4, lat2);

        ResultSet rs = ps.executeQuery();

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode array = featureCollection.putArray("features");

        while (rs.next()) {
            String id = rs.getString("id");
            String type = rs.getString("type");
            String geojson_ret = rs.getString("geojson");
            String raw = rs.getString("raw");
            String source_id = rs.getString("source_id");
            String source = rs.getString("source");

            JsonNode geometryNode = mapper.readTree(geojson_ret); // pure geometry, untouched

            ObjectNode propertiesNode = mapper.createObjectNode();
            propertiesNode.put("id", id);
            propertiesNode.put("type", type);
            propertiesNode.put("raw", raw);
            propertiesNode.put("source_id", source_id);
            propertiesNode.put("source", source);

            ObjectNode featureNode = mapper.createObjectNode();
            featureNode.put("type", "Feature");
            featureNode.set("geometry", geometryNode);
            featureNode.set("properties", propertiesNode);

            array.add(featureNode);
        }

        rs.close();
        ps.close();


        return featureCollection;
    }
}

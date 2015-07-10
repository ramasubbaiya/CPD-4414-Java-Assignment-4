/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package product;

import com.mysql.jdbc.Connection;
import java.io.StringReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.ws.rs.*;
import org.json.simple.JSONArray;

/**
 *
 * @author c0652863
 */
@Path("/products")
public class ProductServlet {
    
    //getConnection method 
    private Connection getConnection() throws SQLException {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://ipro.lambton.on.ca/inventory";
            connection = (Connection) DriverManager.getConnection(jdbc, "products", "products");
        } catch (ClassNotFoundException ex) {
            System.err.println("Class not found" +ex.getMessage());
        }
        return connection;
    }

    @GET
    @Produces("application/json")
    public String doGet() {
        return getResults("SELECT * FROM product");
    }

    @GET
    @Produces("application/json")
    @Path("{id}")
    public String doGet(@PathParam("id") String productID) {
        return getResults("SELECT * FROM product WHERE productID = ?", productID);
    }

    @POST
    @Consumes("application/json")
    public void doPost(String str) {
        JsonParser jsonpraser = Json.createParser(new StringReader(str));
        Map<String, String> mapKeyValue = new HashMap<>();
        String key = "", value;
        while (jsonpraser.hasNext()) {
            JsonParser.Event evt = jsonpraser.next();
            switch (evt) {
                case KEY_NAME:
                    key = jsonpraser.getString();
                    break;
                case VALUE_STRING:
                    value = jsonpraser.getString();
                    mapKeyValue.put(key, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(jsonpraser.getInt());
                    mapKeyValue.put(key, value);
                    break;
            }
        }
        System.out.println(mapKeyValue);
        doPostOrPutOrDelete("INSERT INTO product (name, description, quantity) VALUES ( ?, ?, ?)",
                mapKeyValue.get("name"), mapKeyValue.get("description"), mapKeyValue.get("quantity"));
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public void doPut(@PathParam("id") String id, String str) {
        JsonParser jsonpraser = Json.createParser(new StringReader(str));
        Map<String, String> mapKayValue = new HashMap<>();
        String key = "", val;
        while (jsonpraser.hasNext()) {
            JsonParser.Event evt = jsonpraser.next();
            switch (evt) {
                case KEY_NAME:
                    key = jsonpraser.getString();
                    break;
                case VALUE_STRING:
                    val = jsonpraser.getString();
                    mapKayValue.put(key, val);
                    break;
                case VALUE_NUMBER:
                    val = jsonpraser.getString();
                    mapKayValue.put(key, val);
                    break;
            }
        }
        System.out.println(mapKayValue);
        doPostOrPutOrDelete("UPDATE PRODUCT SET name = ?, description = ?, quantity = ? WHERE productID = ?",
                mapKayValue.get("name"), mapKayValue.get("description"), mapKayValue.get("quantity"), id);

    }

    @DELETE
    @Path("{id}")
    public void doDelete(@PathParam("id") String id, String str) {
        doPostOrPutOrDelete("DELETE FROM product WHERE productID = ?", id);
    }

    private void doPostOrPutOrDelete(String query, String... params) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                preparedStatement.setString(i, params[i - 1]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        getResults("SELECT * FROM product");
    }

    private String getResults(String query, String... params) {
        String result = new String();
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                preparedStatement.setString(i, params[i - 1]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONArray productList = new JSONArray();
            while (resultSet.next()) {
                Map productMap = new LinkedHashMap();
                productMap.put("productID", resultSet.getInt("productID"));
                productMap.put("name", resultSet.getString("name"));
                productMap.put("description", resultSet.getString("description"));
                productMap.put("quantity", resultSet.getInt("quantity"));
                productList.add(productMap);
            }
            result = productList.toString();
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result.replace("},", "},\n");
    }

}

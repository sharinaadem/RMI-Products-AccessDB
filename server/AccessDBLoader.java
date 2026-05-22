package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessDBLoader
 * ─────────────────────────────────────────────────────────────
 * Reads product data from an MS Access database using UCanAccess.
 */
public class AccessDBLoader {

    public static List<ProductData> load(String dbPath) throws Exception {
        List<ProductData> products = new ArrayList<>();

        String url = "jdbc:ucanaccess://" + dbPath + ";memory=false";

        // Allow UCanAccess to register its CLASSPATH functions in HSQLDB.
        System.setProperty("hsqldb.method_class_names", "net.ucanaccess.converters.*");

        System.out.println("[AccessDBLoader] Connecting to MS Access DB: " + dbPath);

        try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement stmt = conn.prepareStatement(
                         "SELECT Name, Description, Price FROM Products");
             ResultSet rs = stmt.executeQuery()) {

            int row = 0;
            while (rs.next()) {
                row++;
                String name = rs.getString("Name");
                String description = rs.getString("Description");
                double price = rs.getDouble("Price");

                products.add(new ProductData(name, description, price));

                System.out.println("[AccessDBLoader] Row " + row + " -> "
                    + name + " | " + description + " | PHP " + price);
            }

            System.out.println("[AccessDBLoader] Total records loaded: " + products.size());
        }

        return products;
    }

    public static class ProductData {
        public final String name;
        public final String description;
        public final double price;

        public ProductData(String name, String description, double price) {
            this.name = name;
            this.description = description;
            this.price = price;
        }
    }
}

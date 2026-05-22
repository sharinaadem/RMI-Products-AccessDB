package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * AccessDBSaver
 * ─────────────────────────────────────────────────────────────
 * Writes product data to an MS Access database using UCanAccess.
 */
public class AccessDBSaver {

    public static void saveProducts(String dbPath, List<XmlProductLoader.ProductData> products, boolean clearFirst) throws Exception {
        String url = "jdbc:ucanaccess://" + dbPath + ";memory=false";

        // Allow UCanAccess to register its CLASSPATH functions in HSQLDB.
        System.setProperty("hsqldb.method_class_names", "net.ucanaccess.converters.*");

        System.out.println("[AccessDBSaver] Connecting to MS Access DB: " + dbPath);

        try (Connection conn = DriverManager.getConnection(url)) {
            if (clearFirst) {
                try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Products")) {
                    deleteStmt.executeUpdate();
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO Products (Name, Description, Price) VALUES (?, ?, ?)")) {

                for (XmlProductLoader.ProductData product : products) {
                    insertStmt.setString(1, product.name);
                    insertStmt.setString(2, product.description);
                    insertStmt.setDouble(3, product.price);
                    insertStmt.executeUpdate();
                }
            }
        }

        System.out.println("[AccessDBSaver] Saved " + products.size() + " product(s) to database.");
    }

}

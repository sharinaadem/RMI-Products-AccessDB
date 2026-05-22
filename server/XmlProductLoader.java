package server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * XmlProductLoader
 * ─────────────────────────────────────────────────────────────
 * Reads product data from an XML file.
 */
public class XmlProductLoader {

    public static List<ProductData> load(String xmlPath) throws Exception {
        List<ProductData> products = new ArrayList<>();

        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) {
            throw new IllegalArgumentException("XML file not found: " + xmlPath);
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList productNodes = doc.getElementsByTagName("product");
        for (int i = 0; i < productNodes.getLength(); i++) {
            Element productEl = (Element) productNodes.item(i);

            String name = getChildText(productEl, "name");
            String description = getChildText(productEl, "description");
            String priceText = getChildText(productEl, "price");
            double price = Double.parseDouble(priceText);

            products.add(new ProductData(name, description, price));
        }

        System.out.println("[XmlProductLoader] Total records loaded: " + products.size());
        return products;
    }

    private static String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
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

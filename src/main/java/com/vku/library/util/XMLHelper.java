package com.vku.library.util;

import com.vku.library.model.Book;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class XMLHelper {
    public static void exportBooks(List<Book> books, String filePath) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("Library");
            doc.appendChild(root);

            for (Book b : books) {
                Element bookXml = doc.createElement("Book");
                bookXml.setAttribute("id", String.valueOf(b.getId()));

                Element title = doc.createElement("Title");
                title.setTextContent(b.getTitle());

                Element qty = doc.createElement("Quantity");
                qty.setTextContent(String.valueOf(b.getQuantity()));

                bookXml.appendChild(title);
                bookXml.appendChild(qty);
                root.appendChild(bookXml);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(new File(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
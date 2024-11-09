package it.uniroma3.lucene.service;

import it.uniroma3.lucene.service.utils.FieldExtractorUtil;
import it.uniroma3.lucene.service.utils.HtmlValidatorUtil;
import it.uniroma3.lucene.service.utils.PropUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Indexer {


    public void index() {
        try {
            // Define where to save Lucene index
            Directory directory = FSDirectory.open(Paths.get(PropUtil.getProperty("lucene.index.path")));

            // Define an IndexWriter
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter writer = new IndexWriter(directory, config);

            // Directory containing HTML files
            String htmlDirPath = PropUtil.getProperty("html.directory.path");

            // List and process all HTML files in the directory
            try (Stream<Path> paths = Files.walk(Paths.get(htmlDirPath))) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".html"))
                        .forEach(path -> {
                            // Validate and process each HTML file
                            String filePath = path.toString();
                            if (HtmlValidatorUtil.isValidHtml(filePath)) {
                                String title = FieldExtractorUtil.extractTitle(filePath);
                                List<String> authors = FieldExtractorUtil.extractAuthor(filePath);


                                // Create a new Lucene document
                                Document doc = new Document();
                                doc.add(new TextField("title", title, Field.Store.YES));
                                for (String author : authors) {
                                    doc.add(new TextField("author", author, Field.Store.YES));
                                }

                                // Add the document to the index
                                try {
                                    System.out.println("Indexing: " + filePath);
                                    writer.addDocument(doc);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Invalid HTML: " + filePath);
                            }
                        });
            }

            writer.commit(); // Persist changes to the disk
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
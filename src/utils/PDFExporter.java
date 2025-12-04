package utils;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import models.Student;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFExporter {
    
    public static boolean exportTableToPDF(TableView<Student> tableView, File outputFile) {
        try {
            // Create printer job
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            
            if (printerJob == null) {
                return false;
            }
            
            // Configure page layout for PDF
            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(
                Paper.A4, 
                PageOrientation.LANDSCAPE, 
                Printer.MarginType.DEFAULT
            );
            
            // Create a printable node with the table
            VBox printNode = createPrintableContent(tableView);
            
            // Scale to fit page
            double scaleX = pageLayout.getPrintableWidth() / printNode.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() / printNode.getBoundsInParent().getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            Scale scaleTransform = new Scale(scale, scale);
            printNode.getTransforms().add(scaleTransform);
            
            // Print to PDF
            boolean success = printerJob.printPage(pageLayout, printNode);
            
            if (success) {
                printerJob.endJob();
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static VBox createPrintableContent(TableView<Student> tableView) {
        VBox content = new VBox(10);
        
        // Add title
        Text title = new Text("Student Allocation Report");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Add timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Text timestamp = new Text("Generated: " + dateFormat.format(new Date()));
        timestamp.setStyle("-fx-font-size: 12px;");
        
        // Clone the table for printing
        TableView<Student> printTable = new TableView<>();
        printTable.setItems(tableView.getItems());
        printTable.getColumns().addAll(tableView.getColumns());
        printTable.setPrefHeight(tableView.getItems().size() * 30 + 50);
        
        content.getChildren().addAll(title, timestamp, printTable);
        
        return content;
    }
    
    public static void exportStudentsToPDF(List<Student> students, File outputFile) throws Exception {
        // This method signature is kept for compatibility
        // In JavaFX, we'll use the print dialog instead
        throw new UnsupportedOperationException("Use exportTableToPDF with TableView instead");
    }
}

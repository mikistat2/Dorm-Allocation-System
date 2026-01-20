package utils;

import models.Student;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import java.util.Calendar;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFExporter {

    public static void exportStudentsToPDF(List<Student> students, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDDocumentInformation info = new PDDocumentInformation();
            info.setAuthor("Dorm Allocation Team - Elias");
            info.setTitle("Student Allocation Report");
            info.setCreationDate(Calendar.getInstance());
            document.setDocumentInformation(info);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            int rowHeight = 20;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // TITLE
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Student Allocation Report");
            contentStream.endText();
            yPosition -= 30;

            // TIMESTAMP
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Generated: " + dateFormat.format(new Date()));
            contentStream.endText();
            yPosition -= 30;

            // HEADERS
            drawTableHeaders(contentStream, margin, yPosition);
            yPosition -= rowHeight;

            // ROWS
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            for (Student student : students) {
                // simple page break check
                if (yPosition < margin) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin - 50; // New page start

                    drawTableHeaders(contentStream, margin, yPosition);
                    yPosition -= rowHeight;
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                }

                drawRow(contentStream, margin, yPosition, student);
                yPosition -= rowHeight;
            }

            contentStream.close();
            document.save(outputFile);
        }
    }

    private static void drawTableHeaders(PDPageContentStream contentStream, float margin, float y) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText("ID");
        contentStream.newLineAtOffset(60, 0); // Column 1 width
        contentStream.showText("Name");
        contentStream.newLineAtOffset(120, 0); // Column 2 width
        contentStream.showText("Gender");
        contentStream.newLineAtOffset(60, 0); // Column 3 width
        contentStream.showText("Building");
        contentStream.newLineAtOffset(80, 0); // Column 4 width
        contentStream.showText("Room");
        contentStream.endText();

        // Horizontal line
        contentStream.moveTo(margin, y - 5);
        contentStream.lineTo(margin + 450, y - 5);
        contentStream.stroke();
    }

    private static void drawRow(PDPageContentStream contentStream, float margin, float y, Student student)
            throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText(checkNull(student.getId()));
        contentStream.newLineAtOffset(60, 0);
        contentStream.showText(checkNull(student.getName()));
        contentStream.newLineAtOffset(120, 0);
        contentStream.showText(checkNull(student.getGender()));
        contentStream.newLineAtOffset(60, 0);
        contentStream.showText(checkNull(student.getAssignedBuilding()));
        contentStream.newLineAtOffset(80, 0);
        contentStream.showText(checkNull(student.getAssignedRoom()));
        contentStream.endText();
    }

    private static String checkNull(String s) {
        return s == null ? "-" : s;
    }
}

package com.example.serveur.service;

import com.example.serveur.model.Message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String CSV_SEPARATOR = "\t";
    private static final List<String> HEADERS = List.of(
            "date_commencement",
            "date_signalement",
            "site",
            "agent",
            "intervention",
            "evenement",
            "point_repere",
            "surface_m2",
            "description",
            "direction",
            "renfort",
            "longitude",
            "latitude"
    );
            private static final List<String> PDF_HEADERS = List.of(
                "Date debut",
                "Date signal",
                "Site",
                "Agent",
                "Intervention",
                "Evenement",
                "Point repere",
                "Surface",
                "Description",
                "Direction",
                "Renfort",
                "Longitude",
                "Latitude"
            );

    public String exportMessagesAsCSV(List<Message> messages) {
        StringWriter writer = new StringWriter();
        writer.write(String.join(CSV_SEPARATOR, HEADERS));
        writer.write("\n");

        for (Message message : messages) {
            writer.write(String.join(CSV_SEPARATOR, toRow(message)));
            writer.write("\n");
        }

        return writer.toString();
    }

    public byte[] exportMessagesAsXlsx(List<Message> messages) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Historique");

            Row headerRow = sheet.createRow(0);
            for (int columnIndex = 0; columnIndex < HEADERS.size(); columnIndex++) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(HEADERS.get(columnIndex));
            }

            int rowIndex = 1;
            for (Message message : messages) {
                Row row = sheet.createRow(rowIndex++);
                List<String> values = toRow(message);
                for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                    row.createCell(columnIndex).setCellValue(values.get(columnIndex));
                }
            }

            for (int columnIndex = 0; columnIndex < HEADERS.size(); columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export XLSX", e);
        }
    }

    public byte[] exportMessagesAsPdf(List<Message> messages) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float fontSize = 6.8f;
            float margin = 24f;
            float rowHeight = 14f;

            PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeA4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float tableWidth = pageWidth - (2 * margin);
            float[] columnRatios = new float[]{1.2f, 1.2f, 1.1f, 1.0f, 1.0f, 1.0f, 1.1f, 0.7f, 1.6f, 0.7f, 0.7f, 0.8f, 0.8f};
            float[] columnWidths = scaleColumnWidths(columnRatios, tableWidth);

            int rowIndex = 0;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float y = pageHeight - margin;
                y = drawPdfHeader(contentStream, fontBold, fontSize, margin, y, rowHeight, columnWidths);

                while (rowIndex < messages.size()) {
                    if (y - rowHeight < margin) {
                        contentStream.close();
                        page = new PDPage(landscapeA4);
                        document.addPage(page);
                        pageWidth = page.getMediaBox().getWidth();
                        pageHeight = page.getMediaBox().getHeight();
                        tableWidth = pageWidth - (2 * margin);
                        columnWidths = scaleColumnWidths(columnRatios, tableWidth);
                        contentStream = new PDPageContentStream(document, page);
                        y = pageHeight - margin;
                        y = drawPdfHeader(contentStream, fontBold, fontSize, margin, y, rowHeight, columnWidths);
                    }

                    List<String> values = shortenForPdf(toRow(messages.get(rowIndex)), 34);
                    y = drawPdfRow(contentStream, fontRegular, fontSize, margin, y, rowHeight, columnWidths, values);
                    rowIndex++;
                }
            } finally {
                contentStream.close();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export PDF", e);
        }
    }

    public byte[] exportMessages(List<Message> messages, String format) {
        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase();
        return switch (normalizedFormat) {
            case "xlsx" -> exportMessagesAsXlsx(messages);
            case "pdf" -> exportMessagesAsPdf(messages);
            default -> exportMessagesAsCSV(messages).getBytes(StandardCharsets.UTF_8);
        };
    }

    private List<String> toRow(Message message) {
        List<String> row = new ArrayList<>();
        row.add(formatDate(message.getDateCommencement()));
        row.add(formatDate(message.getDateSignalement()));
        row.add(valueOrEmpty(message.getUserApp() != null && message.getUserApp().getPatrouilleur() != null && message.getUserApp().getPatrouilleur().getSite() != null
                ? message.getUserApp().getPatrouilleur().getSite().getNom()
                : null));
        row.add(valueOrEmpty(message.getUserApp() != null && message.getUserApp().getPatrouilleur() != null ? message.getUserApp().getPatrouilleur().getNom() : null));
        row.add(valueOrEmpty(message.getIntervention() != null ? message.getIntervention().getIntervention() : null));
        row.add(valueOrEmpty(message.getEvenement() != null ? message.getEvenement().getNom() : null));
        row.add(valueOrEmpty(message.getPointRepere()));
        row.add(message.getSurfaceApproximative() != null ? trimTrailingZeros(message.getSurfaceApproximative()) : "");
        row.add(valueOrEmpty(message.getDescription()));
        row.add(valueOrEmpty(message.getDirection()));
        row.add(message.getRenfort() == null ? "" : (message.getRenfort() ? "oui" : "non"));
        row.add(message.getLongitude() != null ? trimTrailingZeros(message.getLongitude()) : "");
        row.add(message.getLatitude() != null ? trimTrailingZeros(message.getLatitude()) : "");
        return row;
    }

    private String formatDate(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : escapeCsvValue(value);
    }

    private String trimTrailingZeros(Number value) {
        return trimTrailingZeros(value.toString());
    }

    private String trimTrailingZeros(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(".")) {
            value = value.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return value;
    }

    private String escapeCsvValue(String value) {
        if (value.contains("\t") || value.contains("\n") || value.contains("\"") || value.contains("|")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<String> shortenForPdf(List<String> values, int maxLength) {
        List<String> shortened = new ArrayList<>(values.size());
        for (String value : values) {
            if (value == null) {
                shortened.add("");
            } else if (value.length() > maxLength) {
                shortened.add(value.substring(0, Math.max(0, maxLength - 3)) + "...");
            } else {
                shortened.add(value);
            }
        }
        return shortened;
    }

    private float[] scaleColumnWidths(float[] ratios, float totalWidth) {
        float sum = 0f;
        for (float ratio : ratios) {
            sum += ratio;
        }

        float[] widths = new float[ratios.length];
        for (int i = 0; i < ratios.length; i++) {
            widths[i] = (ratios[i] / sum) * totalWidth;
        }
        return widths;
    }

    private float drawPdfHeader(PDPageContentStream contentStream,
                                PDType1Font font,
                                float fontSize,
                                float startX,
                                float y,
                                float rowHeight,
                                float[] columnWidths) throws IOException {
        contentStream.setNonStrokingColor(236, 240, 246);
        contentStream.addRect(startX, y - rowHeight, sum(columnWidths), rowHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);
        contentStream.setStrokingColor(120, 120, 120);

        float x = startX;
        for (int i = 0; i < columnWidths.length; i++) {
            drawCell(contentStream, font, fontSize, x, y, columnWidths[i], rowHeight, PDF_HEADERS.get(i));
            x += columnWidths[i];
        }

        return y - rowHeight;
    }

    private float drawPdfRow(PDPageContentStream contentStream,
                             PDType1Font font,
                             float fontSize,
                             float startX,
                             float y,
                             float rowHeight,
                             float[] columnWidths,
                             List<String> values) throws IOException {
        contentStream.setStrokingColor(190, 190, 190);
        float x = startX;
        for (int i = 0; i < columnWidths.length; i++) {
            String value = i < values.size() ? values.get(i) : "";
            drawCell(contentStream, font, fontSize, x, y, columnWidths[i], rowHeight, value);
            x += columnWidths[i];
        }
        return y - rowHeight;
    }

    private void drawCell(PDPageContentStream contentStream,
                          PDType1Font font,
                          float fontSize,
                          float x,
                          float y,
                          float width,
                          float height,
                          String text) throws IOException {
        contentStream.addRect(x, y - height, width, height);
        contentStream.stroke();

        String safeText = text == null ? "" : text.replace("\n", " ").replace("\r", " ");
        String fitted = fitText(font, fontSize, safeText, width - 4);

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x + 2, y - (height * 0.72f));
        contentStream.showText(fitted);
        contentStream.endText();
    }

    private String fitText(PDType1Font font, float fontSize, String text, float maxWidth) throws IOException {
        String value = text == null ? "" : text;
        while (!value.isEmpty() && (font.getStringWidth(value) / 1000f) * fontSize > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.length() < text.length() && value.length() > 3) {
            return value.substring(0, value.length() - 3) + "...";
        }
        return value;
    }

    private float sum(float[] values) {
        float total = 0f;
        for (float value : values) {
            total += value;
        }
        return total;
    }
}

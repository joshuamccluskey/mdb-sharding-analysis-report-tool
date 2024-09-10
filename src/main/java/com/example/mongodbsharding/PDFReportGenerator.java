package com.example.mongodbsharding;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PDFReportGenerator {

    private static final DeviceRgb MONGODB_GREEN = new DeviceRgb(0, 237, 100);
    private static final DeviceRgb MONGODB_DARK_GREEN = new DeviceRgb(0, 192, 81);
    private static final DeviceRgb MONGODB_DARKER_GREEN = new DeviceRgb(0, 130, 55);

    public void generatePDFReport(ShardingStatus shardingStatus, String outputFile) throws IOException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4.rotate());
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());
        Document document = new Document(pdf);

        PdfFont boldFont = PdfFontFactory.createFont();
        PdfFont normalFont = PdfFontFactory.createFont();

        addHeader(document, boldFont, "MongoDB Sharding Status Report");

        addSection(document, boldFont, normalFont, "Shards", shardingStatus.getShards());
        addDatabasesSection(document, boldFont, normalFont, shardingStatus.getDatabases());

        document.close();
    }

    private void addHeader(Document document, PdfFont font, String title) throws IOException {
        // Create a table for the header
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1}))
                .useAllAvailableWidth();

        // Add title to the center
        Paragraph titleParagraph = new Paragraph(title)
                .setFont(font)
                .setFontSize(24)
                .setFontColor(MONGODB_DARKER_GREEN)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        headerTable.addCell(new Cell().add(titleParagraph).setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE));

        // Add leaf to the right
        Image leaf = new Image(ImageDataFactory.create(new ClassPathResource("img/leaf.png").getURL()));
        leaf.setWidth(60);
        headerTable.addCell(new Cell().add(leaf).setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE).setHorizontalAlignment(HorizontalAlignment.RIGHT));

        document.add(headerTable);
    }

    private void addSection(Document document, PdfFont boldFont, PdfFont normalFont, String title, Iterable<?> items) {
        document.add(new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(MONGODB_DARK_GREEN)
                .setBold());

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        for (Object item : items) {
            if (item instanceof ShardingStatus.Shard) {
                ShardingStatus.Shard shard = (ShardingStatus.Shard) item;
                table.addCell(createCell("ID", boldFont).setBackgroundColor(MONGODB_GREEN, 0.1f));
                table.addCell(createCell(shard.getId(), normalFont));
                table.addCell(createCell("Host", boldFont).setBackgroundColor(MONGODB_GREEN, 0.1f));
                table.addCell(createCell(shard.getHost(), normalFont));
            }
        }

        document.add(table);
    }

    private void addDatabasesSection(Document document, PdfFont boldFont, PdfFont normalFont, Iterable<ShardingStatus.Database> databases) {
        document.add(new Paragraph("Databases")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(MONGODB_DARK_GREEN)
                .setBold());

        for (ShardingStatus.Database db : databases) {
            document.add(new Paragraph(db.getName())
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setFontColor(MONGODB_DARK_GREEN));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1, 3}))
                    .useAllAvailableWidth();

            table.addHeaderCell(createHeaderCell("Collection", boldFont));
            table.addHeaderCell(createHeaderCell("Shard Key", boldFont));
            table.addHeaderCell(createHeaderCell("Chunk Count", boldFont));
            table.addHeaderCell(createHeaderCell("Shard Distribution", boldFont));

            for (ShardingStatus.ShardedCollection coll : db.getShardedCollections()) {
                table.addCell(createCell(coll.getName(), normalFont));
                table.addCell(createCell(coll.getShardKey().toString(), normalFont));
                table.addCell(createCell(String.valueOf(coll.getChunkCount()), normalFont));
                table.addCell(createShardDistributionCell(formatShardDistribution(coll.getShardDistribution()), normalFont));
            }

            document.add(table);
        }
    }

    private Cell createHeaderCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(MONGODB_DARKER_GREEN)
                .setBold();
    }

    private Cell createCell(String content, PdfFont font) {
        Paragraph p = new Paragraph(content)
                .setFont(font)
                .setFontColor(ColorConstants.BLACK);
        return new Cell()
                .add(p)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createShardDistributionCell(String content, PdfFont font) {
        Paragraph p = new Paragraph(content)
                .setFont(font)
                .setFontColor(ColorConstants.BLACK)
                .setFontSize(8);
        return new Cell()
                .add(p)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private String formatShardDistribution(Object shardDistribution) {
        if (shardDistribution == null) {
            return "N/A";
        }
        String distribution = shardDistribution.toString();
        // Remove curly braces and split by comma
        distribution = distribution.replaceAll("[{}]", "").replace(", ", "\n");
        return distribution;
    }

    private class FooterEventHandler implements IEventHandler {
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            Canvas canvas = new Canvas(pdfCanvas, pageSize);
            float x = pageSize.getWidth() / 2;
            float y = 30;

            // Add logo
            try {
                Image logo = new Image(ImageDataFactory.create(new ClassPathResource("img/logo.png").getURL()));
                logo.setWidth(40);
                logo.setFixedPosition(20, 20);
                canvas.add(logo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add page number
            canvas.showTextAligned(String.valueOf(pdfDoc.getPageNumber(page)), x, y, TextAlignment.CENTER);

            canvas.close();
        }
    }
}
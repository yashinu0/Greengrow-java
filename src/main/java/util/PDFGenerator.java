package util;

import Entities.Commande;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PDFGenerator {
    private static final String LOGO_PATH = "logo.png"; // Ajoutez votre logo si nécessaire
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    public static void generateInvoice(Commande commande, String outputPath) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        // En-tête
        addHeader(document);

        // Informations de la facture
        addInvoiceInfo(document, commande);

        // Détails de la commande
        addOrderDetails(document, commande);

        // Pied de page
        addFooter(document);

        document.close();
    }

    private static void addHeader(Document document) throws Exception {
        // Ajouter le logo si disponible
        try {
            Image logo = Image.getInstance(LOGO_PATH);
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
        } catch (Exception e) {
            // Logo non trouvé, on continue sans logo
        }

        Paragraph title = new Paragraph("FACTURE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private static void addInvoiceInfo(Document document, Commande commande) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Informations de la facture
        addTableCell(table, "Numéro de commande:", String.valueOf(commande.getIdCommande()), true);
        addTableCell(table, "Date:", commande.getDateCommande().format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)), true);
        addTableCell(table, "Statut:", commande.getStatutCommande(), true);
        addTableCell(table, "Mode de paiement:", commande.getModePaiementCommande(), true);

        document.add(table);
    }

    private static void addOrderDetails(Document document, Commande commande) throws Exception {
        Paragraph detailsTitle = new Paragraph("Détails de la commande", HEADER_FONT);
        detailsTitle.setSpacingBefore(20);
        detailsTitle.setSpacingAfter(10);
        document.add(detailsTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // En-têtes
        addTableHeader(table, "Description", "Montant");

        // Détails
        addTableCell(table, "Prix total", String.format("%.2f €", commande.getPrixTotalCommande()), false);

        // Total
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total TTC", HEADER_FONT));
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        table.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(
            String.format("%.2f €", commande.getPrixTotalCommande()), HEADER_FONT));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        table.addCell(totalValueCell);

        document.add(table);
    }

    private static void addFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30);
        footer.add(new Chunk("Merci de votre confiance !", NORMAL_FONT));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph terms = new Paragraph();
        terms.setSpacingBefore(20);
        terms.add(new Chunk("Conditions de paiement : paiement à réception de facture", NORMAL_FONT));
        terms.setAlignment(Element.ALIGN_LEFT);
        document.add(terms);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private static void addTableCell(PdfPTable table, String label, String value, boolean border) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (!border) labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (!border) valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
}

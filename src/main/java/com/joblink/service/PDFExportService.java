package com.joblink.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.joblink.model.Facture;
import com.joblink.model.Mission;
import com.joblink.model.Client;
import com.joblink.model.Freelance;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFExportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

    public static boolean exportFactureToPDF(Facture facture, Mission mission, Client client, Freelance freelance, String outputPath) {
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Titre
            Paragraph title = new Paragraph("FACTURE #" + facture.getIdFacture(), TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Ligne de séparation
            document.add(new Paragraph("_________________________________________________________________"));
            document.add(Chunk.NEWLINE);

            // Informations générales
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            // Colonne gauche - Client
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.NO_BORDER);
            clientCell.setPadding(10);
            clientCell.addElement(new Paragraph("FACTURÉ À", BOLD_FONT));
            clientCell.addElement(new Paragraph(client.getNom() + " " + client.getPrenom(), NORMAL_FONT));
            if (client.getNomEntreprise() != null) {
                clientCell.addElement(new Paragraph(client.getNomEntreprise(), NORMAL_FONT));
            }
            if (client.getAdresse() != null) {
                clientCell.addElement(new Paragraph(client.getAdresse(), NORMAL_FONT));
            }
            clientCell.addElement(new Paragraph(client.getEmail(), NORMAL_FONT));

            // Colonne droite - Freelance
            PdfPCell freelanceCell = new PdfPCell();
            freelanceCell.setBorder(Rectangle.NO_BORDER);
            freelanceCell.setPadding(10);
            freelanceCell.addElement(new Paragraph("ÉMIS PAR", BOLD_FONT));
            freelanceCell.addElement(new Paragraph(freelance.getNom() + " " + freelance.getPrenom(), NORMAL_FONT));
            freelanceCell.addElement(new Paragraph(freelance.getEmail(), NORMAL_FONT));
            freelanceCell.addElement(new Paragraph("Tarif journalier: " + freelance.getTarifJournalier() + " €", NORMAL_FONT));

            infoTable.addCell(clientCell);
            infoTable.addCell(freelanceCell);
            document.add(infoTable);

            // Date d'émission
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateEmission = new Paragraph("Date d'émission: " + facture.getDateEmission().format(formatter), NORMAL_FONT);
            dateEmission.setSpacingAfter(20);
            document.add(dateEmission);

            // Détails de la mission
            document.add(new Paragraph("DÉTAILS DE LA MISSION", BOLD_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable missionTable = new PdfPTable(2);
            missionTable.setWidthPercentage(100);
            missionTable.setSpacingAfter(20);

            addTableRow(missionTable, "Titre", mission.getTitre(), true);
            addTableRow(missionTable, "Description", mission.getDescription(), false);
            addTableRow(missionTable, "Budget", String.format("%.2f €", mission.getBudget()), false);
            addTableRow(missionTable, "Date limite", mission.getDateLimite().toString(), false);

            document.add(missionTable);

            // Montant total
            document.add(Chunk.NEWLINE);
            Paragraph totalLabel = new Paragraph("MONTANT TOTAL", BOLD_FONT);
            totalLabel.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalLabel);

            Font totalFont = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, new BaseColor(76, 175, 80));
            Paragraph totalAmount = new Paragraph(String.format("%.2f €", facture.getMontantTotal()), totalFont);
            totalAmount.setAlignment(Element.ALIGN_RIGHT);
            totalAmount.setSpacingAfter(30);
            document.add(totalAmount);

            // Footer
            document.add(new Paragraph("_________________________________________________________________"));
            Paragraph footer = new Paragraph("Généré avec JobLink - Plateforme de mise en relation Clients/Freelances",
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addTableRow(PdfPTable table, String label, String value, boolean header) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, header ? HEADER_FONT : BOLD_FONT));
        if (header) {
            labelCell.setBackgroundColor(new BaseColor(33, 150, 243));
        }
        labelCell.setPadding(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        if (header) {
            valueCell.setBackgroundColor(new BaseColor(33, 150, 243));
        }
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

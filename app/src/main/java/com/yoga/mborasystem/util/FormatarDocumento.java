package com.yoga.mborasystem.util;

import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.yoga.mborasystem.MainActivity;

public class FormatarDocumento {

    public static void addNewLineWithLeftAndRight(Document document, String textLeft, String textRight, Font textLeftFont, Font textRightFont) throws DocumentException {
        Chunk chunkTextLeft = new Chunk(textLeft, textLeftFont);
        Chunk chunkTextRight = new Chunk(textRight, textRightFont);
        Paragraph p = new Paragraph(chunkTextLeft);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkTextRight);
        document.add(p);
    }

    public static void addNewLineHorizontal(Document document, String text1, String text2, String text3, String text4, String text5, Font textFont, boolean isFiveColumn) throws DocumentException {
        Chunk chunkText1 = new Chunk(text1, textFont);
        Chunk chunkText2 = new Chunk(text2, textFont);
        Chunk chunkText3 = new Chunk(text3, textFont);
        Chunk chunkText4 = new Chunk(text4, textFont);

        Paragraph p = new Paragraph(chunkText1);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkText2);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkText3);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkText4);
        p.add(new Chunk(new VerticalPositionMark()));
        if (isFiveColumn) {
            Chunk chunkText5 = new Chunk(text5, textFont);
            p.add(chunkText5);
        }
        document.add(p);
    }

    public static void addLineSeparator(Document document) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(BaseColor.BLACK);
        addLineSpace(document);
        document.add(new Chunk(lineSeparator));
    }

    public static void addLineSpace(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
    }

    public static void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text, font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(align);
        document.add(paragraph);
    }

    public static void printPDF(Activity activity, Context context, String factura, String pasta) {
        PrintManager printManager;
        printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(activity, Common.getAppPath(pasta) + factura);
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            MainActivity.dismissProgressBar();
        }
    }
}

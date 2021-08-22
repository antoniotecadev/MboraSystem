package com.yoga.mborasystem.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import androidx.annotation.RequiresApi;

public class CriarFactura {

    public static void getPemissionAcessStoregeExternal(Activity activity, Context context, String facturaPath, Cliente cliente, Long idOperador, TextInputEditText txtNomeCliente, TextInputEditText desconto, int quantidade, int valorBase, String codigoQr, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        Dexter.withContext(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        createPdfFile(Common.getAppPath(context) + facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, quantidade, valorBase, codigoQr, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        createPdfFile(Common.getAppPath(context) + facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, quantidade, valorBase, codigoQr, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private static void createPdfFile(String path, Activity activity, Context context, Cliente cliente, Long idOperador, TextInputEditText txtNomeCliente, TextInputEditText desconto, int quantidade, int valorBase, String codigoQr, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        if (new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.setMargins(10, 5, 0, 0);
            document.open();
            document.addCreationDate();
            BarcodeQRCode my_qr_code = new BarcodeQRCode(codigoQr, 500, 500, null);
            Image qr_code_image = my_qr_code.getImage();
            qr_code_image.setAlignment(Element.ALIGN_CENTER);
//          BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 40.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, cliente.getNomeEmpresa(), Element.ALIGN_CENTER, titleFont);
            Font bairroRuaFont = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, cliente.getBairro() + "\n" + cliente.getRua(), Element.ALIGN_CENTER, bairroRuaFont);
            addLineSpace(document);
            Font font = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, context.getString(R.string.nif) + " " + cliente.getNifbi(), Element.ALIGN_LEFT, font);
            addNewItem(document, context.getString(R.string.tel) + " " + cliente.getTelefone() + " / " + cliente.getTelefonealternativo(), Element.ALIGN_LEFT, font);
            addNewItem(document, activity.getString(R.string.data) + Ultilitario.getDateCurrent(), Element.ALIGN_LEFT, font);
            addLineSpace(document);
            Font facturaReciboFont = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, activity.getString(R.string.factura_recibo), Element.ALIGN_CENTER, facturaReciboFont);
            addNewItem(document, activity.getString(R.string.clienteupper) + " " + txtNomeCliente.getText().toString(), Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            addNewItem(document, activity.getString(R.string.desc_prec_qtd_val), Element.ALIGN_CENTER, font);
            addLineSeparator(document);
            for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
                String preco = String.valueOf(produto.getValue().getPreco());
                String valor = String.valueOf(precoTotalUnit.get(produto.getKey()).intValue());
                addNewItem(document, produto.getValue().getNome() + " " + (produto.getValue().isIva() ? context.getString(R.string.iva_factura) : "") + " " + preco.substring(0, preco.length() - 2) + " " + precoTotalUnit.get(produto.getKey()).intValue() / produto.getValue().getPreco() + ". " + valor.substring(0, valor.length() - 2), Element.ALIGN_LEFT, font);
            }
            addLineSeparator(document);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.total), Ultilitario.formatPreco(String.valueOf(totalVenda)), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.valor_base), Ultilitario.formatPreco(String.valueOf(valorBase)), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.iva), Ultilitario.formatPreco(String.valueOf(valorIva)), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.desconto), Ultilitario.formatPreco(desconto.getText().toString()), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.total_desc), Ultilitario.formatPreco(String.valueOf(totalDesconto)), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.pago), Ultilitario.formatPreco(String.valueOf(valorPago)), font, font);
            addNewLineWithLeftAndRight(document, activity.getString(R.string.troco), Ultilitario.formatPreco(String.valueOf(troco)), font, font);
            addLineSeparator(document);
            addNewItem(document, activity.getString(R.string.pagamento) + " " + formaPagamento, Element.ALIGN_LEFT, font);
            addNewItem(document, activity.getString(R.string.operador) + (idOperador > 0 ? " MS" + idOperador : " MSA" + cliente.getId()), Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            addLineSpace(document);
            document.add(qr_code_image);
            addNewItem(document, codigoQr, Element.ALIGN_CENTER, font);
            addNewItem(document, activity.getString(R.string.obr_vol_semp), Element.ALIGN_CENTER, font);
            document.close();
            Toast.makeText(context, activity.getString(R.string.factura_guardada), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (DocumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static void addNewLineWithLeftAndRight(Document document, String textLeft, String textRight, Font textLeftFont, Font textRightFont) throws DocumentException {
        Chunk chunkTextLeft = new Chunk(textLeft, textLeftFont);
        Chunk chunkTextRight = new Chunk(textRight
                , textRightFont);
        Paragraph p = new Paragraph(chunkTextLeft);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkTextRight);
        document.add(p);
    }

    private static void addLineSeparator(Document document) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(BaseColor.BLACK);
        addLineSpace(document);
        document.add(new Chunk(lineSeparator));
    }

    private static void addLineSpace(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
    }

    private static void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text, font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(align);
        document.add(paragraph);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void printPDF(Activity activity, Context context, String factura) {
        PrintManager printManager;
        printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(activity, Common.getAppPath(context) + factura);
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

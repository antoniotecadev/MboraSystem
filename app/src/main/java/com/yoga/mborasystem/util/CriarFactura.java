package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.FormatarDocumento.addLineSeparator;
import static com.yoga.mborasystem.util.FormatarDocumento.addLineSpace;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewItem;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewLineWithLeftAndRight;
import static com.yoga.mborasystem.util.FormatarDocumento.printPDF;
import static com.yoga.mborasystem.util.Ultilitario.addFileContentProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Objects;

public class CriarFactura {

    public static void getPemissionAcessStoregeExternal(boolean isGuardar, Activity activity, Context context, String facturaPath, Cliente cliente, Long idOperador, AppCompatAutoCompleteTextView txtNomeCliente, TextInputEditText desconto, int valorBase, String ReferenciaFactura, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, String dataEmissao, String referenciaFactura) {
        Dexter.withContext(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        createPdfFile(isGuardar, Common.getAppPath("Facturas") + facturaPath, facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, valorBase, ReferenciaFactura, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit, dataEmissao, referenciaFactura);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        createPdfFile(isGuardar, Common.getAppPath("Facturas") + facturaPath, facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, valorBase, ReferenciaFactura, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit, dataEmissao, referenciaFactura);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private static void createPdfFile(boolean isGuardar, String path, String facturaPath, Activity activity, Context context, Cliente cliente, Long idOperador, AppCompatAutoCompleteTextView txtNomeCliente, TextInputEditText desconto, int valorBase, String ReferenciaFactura, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, String dataEmissao, String referenciaFactura) {
        MainActivity.getProgressBar();
        if (new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.setMargins(10, 5, 0, 0);
            document.open();
            document.addCreationDate();
            BarcodeQRCode my_qr_code = new BarcodeQRCode(ReferenciaFactura, 250, 250, null);
            Image qr_code_image = my_qr_code.getImage();
            qr_code_image.setAlignment(Element.ALIGN_CENTER);
//          BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, cliente.getNomeEmpresa(), Element.ALIGN_CENTER, titleFont);
            Font bairroRuaFont = new Font(Font.FontFamily.HELVETICA, 30.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, PreferenceManager.getDefaultSharedPreferences(context).getString("nomecomercial", "") + "\n" + cliente.getRua() + "\n" + cliente.getMunicipio() + " - " + cliente.getBairro() + "\n" + cliente.getProvincia(), Element.ALIGN_CENTER, bairroRuaFont);
            addLineSpace(document);
            Font font = new Font(Font.FontFamily.HELVETICA, 30.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document,"NIF: " + cliente.getNifbi(), Element.ALIGN_LEFT, font);
            addNewItem(document,"TEL: " + cliente.getTelefone() + " / " + cliente.getTelefonealternativo(), Element.ALIGN_LEFT, font);
            addNewItem(document,"DATA: " + (dataEmissao.isEmpty() ? Ultilitario.getDateCurrent() : dataEmissao), Element.ALIGN_LEFT, font);
            addLineSpace(document);
            Font facturaReciboFont = new Font(Font.FontFamily.HELVETICA, 30.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document,"FACTURA/RECIBO" + "\n" + referenciaFactura + "\n", Element.ALIGN_CENTER, facturaReciboFont);
            addNewItem(document,"CLIENTE: " + (txtNomeCliente.getText().toString().isEmpty() ? context.getString(R.string.csm_fnl) : TextUtils.split(txtNomeCliente.getText().toString(), "-")[0]), Element.ALIGN_LEFT, font);
            addNewItem(document,"NIF: " + (txtNomeCliente.getText().toString().isEmpty() ? context.getString(R.string.csm_fnl) : TextUtils.split(txtNomeCliente.getText().toString(), "-")[2]), Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            addNewItem(document, "Descrição Preço Quantidade Total", Element.ALIGN_CENTER, font);
            addLineSeparator(document);
            for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
                String preco = String.valueOf(produto.getValue().getPreco());
                String valor = String.valueOf(Objects.requireNonNull(precoTotalUnit.get(produto.getKey())).intValue());
                addNewItem(document, produto.getValue().getNome() + " (" + (produto.getValue().isIva() ? produto.getValue().getPercentagemIva() + "%" : produto.getValue().getCodigoMotivoIsencao()) + ") " + Ultilitario.formatPreco(preco).replaceAll("Kz", "") + " " + Objects.requireNonNull(precoTotalUnit.get(produto.getKey())) / produto.getValue().getPreco() + ". " + Ultilitario.formatPreco(valor).replaceAll("Kz", ""), Element.ALIGN_LEFT, font);
            }
            addLineSeparator(document);
            addNewLineWithLeftAndRight(document, "Total Geral", Ultilitario.formatPreco(String.valueOf(totalVenda)), font, font);
            addNewLineWithLeftAndRight(document, "Valor Base", Ultilitario.formatPreco(String.valueOf(valorBase)), font, font);
            addNewLineWithLeftAndRight(document, "Imposto", Ultilitario.formatPreco(String.valueOf(valorIva)), font, font);
            addNewLineWithLeftAndRight(document, "Desconto", Ultilitario.formatPreco(Objects.requireNonNull(desconto.getText()).toString()), font, font);
            addNewLineWithLeftAndRight(document, "Total Com Desconto", Ultilitario.formatPreco(String.valueOf(totalDesconto)), font, font);
            addNewLineWithLeftAndRight(document, "Total Pago", Ultilitario.formatPreco(String.valueOf(valorPago)), font, font);
            addNewLineWithLeftAndRight(document, "Troco", Ultilitario.formatPreco(String.valueOf(troco)), font, font);
            addLineSeparator(document);
            addNewItem(document,"Pagamento: " + formaPagamento, Element.ALIGN_LEFT, font);
            addNewItem(document,"Operador: " + (idOperador > 0 ? " MSU" + idOperador : " MSA0"), Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            document.add(qr_code_image);
            addNewItem(document, ReferenciaFactura, Element.ALIGN_CENTER, font);
            document.close();
            Toast.makeText(context, activity.getString(R.string.factura_guardada), Toast.LENGTH_LONG).show();
            addFileContentProvider(activity.getApplicationContext(), "/Facturas/" + facturaPath);
            if (!isGuardar)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    printPDF(activity, activity.getBaseContext(), facturaPath, "Facturas");
                } else {
                    Ultilitario.showToast(activity.getBaseContext(), Color.parseColor("#795548"), activity.getString(R.string.precisa_kitkat_maior), R.drawable.ic_toast_erro);
                }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            MainActivity.dismissProgressBar();
        }
    }
}

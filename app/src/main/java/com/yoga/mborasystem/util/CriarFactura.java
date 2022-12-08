package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.FormatarDocumento.addLineSeparator;
import static com.yoga.mborasystem.util.FormatarDocumento.addLineSpace;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewItem;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewLineHorizontal;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewLineWithLeftAndRight;
import static com.yoga.mborasystem.util.FormatarDocumento.printPDF;
import static com.yoga.mborasystem.util.Ultilitario.addFileContentProvider;
import static com.yoga.mborasystem.util.Ultilitario.getDataFormatMonth;
import static com.yoga.mborasystem.util.Ultilitario.getIntPreference;
import static com.yoga.mborasystem.util.Ultilitario.getRasaoISE;
import static com.yoga.mborasystem.util.Ultilitario.setIntPreference;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

    public static void getPemissionAcessStoregeExternal(boolean isSegundaVia, boolean isAnulado, boolean isAnuladoSegundaVia, String referencia, boolean isGuardar, Activity activity, Context context, String facturaPath, Cliente cliente, Long idOperador, AppCompatAutoCompleteTextView txtNomeCliente, TextInputEditText desconto, int percDesc, int valorBase, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, String dataEmissao, String referenciaFactura) {
        Dexter.withContext(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        createPdfFile(isSegundaVia, isAnulado, isAnuladoSegundaVia, referencia, isGuardar, Common.getAppPath("Facturas") + facturaPath, facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, percDesc, valorBase, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit, dataEmissao, referenciaFactura);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        createPdfFile(isSegundaVia, isAnulado, isAnuladoSegundaVia, referencia, isGuardar, Common.getAppPath("Facturas") + facturaPath, facturaPath, activity, context, cliente, idOperador, txtNomeCliente, desconto, percDesc, valorBase, valorIva, formaPagamento, totalDesconto, valorPago, troco, totalVenda, produtos, precoTotalUnit, dataEmissao, referenciaFactura);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private static void createPdfFile(boolean isSegundaVia, boolean isAnulado, boolean isAnuladoSegundaVia, String referencia, boolean isGuardar, String path, String facturaPath, Activity activity, Context context, Cliente cliente, Long idOperador, AppCompatAutoCompleteTextView txtNomeCliente, TextInputEditText desconto, int percDesc, int valorBase, int valorIva, String formaPagamento, int totalDesconto, int valorPago, int troco, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, String dataEmissao, String referenciaFactura) {
        MainActivity.getProgressBar();
        if (new File(path).exists())
            new File(path).delete();
        try {
            String nib = PreferenceManager.getDefaultSharedPreferences(context).getString("nib", "");
            String iban = PreferenceManager.getDefaultSharedPreferences(context).getString("iban", "");
            String textorodape = PreferenceManager.getDefaultSharedPreferences(context).getString("textorodape", "");
            String hash = Ultilitario.getValueSharedPreferences(context, "hashvenda", "");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.setMargins(5, 5, 0, 0);
            document.open();
            document.addCreationDate();
            BarcodeQRCode my_qr_code = new BarcodeQRCode(referenciaFactura, 250, 250, null);
            Image qr_code_image = my_qr_code.getImage();
            qr_code_image.setAlignment(Element.ALIGN_CENTER);
//          BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);
            Font font = new Font(Font.FontFamily.HELVETICA, 25.0f, Font.NORMAL, BaseColor.BLACK);
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 30.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, cliente.getNomeEmpresa(), Element.ALIGN_CENTER, titleFont);
            Font bairroRuaFont = new Font(Font.FontFamily.HELVETICA, 25.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, PreferenceManager.getDefaultSharedPreferences(context).getString("nomecomercial", "") + "\n" + cliente.getRua() + "\n" + cliente.getMunicipio() + " - " + cliente.getBairro() + "\n" + cliente.getProvincia(), Element.ALIGN_CENTER, bairroRuaFont);
            addLineSpace(document);
            addNewItem(document, "NIF: " + cliente.getNifbi(), Element.ALIGN_LEFT, font);
            addNewItem(document, "TEL: " + cliente.getTelefone() + " / " + cliente.getTelefonealternativo(), Element.ALIGN_LEFT, font);
            addNewItem(document, "DATA: " + dataEmissao, Element.ALIGN_LEFT, font);
            addLineSpace(document);
            Font facturaReciboFont = new Font(Font.FontFamily.HELVETICA, 25.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, isSegundaVia ? "Segunda Via Conforme Original" : (isAnulado ? (isAnuladoSegundaVia ? "Segunda Via Conforme Original - ANULADO" : "Original - ANULADO") : "Original"), Element.ALIGN_CENTER, font);
            addNewItem(document, isAnulado ? "Referente a: " + referencia : "", Element.ALIGN_CENTER, font);
            addNewItem(document, (isAnulado ? "Nota de Crédito" : "FACTURA/RECIBO") + "\n" + referenciaFactura + "\n", Element.ALIGN_CENTER, facturaReciboFont);
            addNewItem(document, "CLIENTE: " + (txtNomeCliente.getText().toString().isEmpty() ? context.getString(R.string.csm_fnl) : TextUtils.split(txtNomeCliente.getText().toString(), "-")[0]), Element.ALIGN_LEFT, font);
            addNewItem(document, "NIF: " + (txtNomeCliente.getText().toString().isEmpty() ? context.getString(R.string.csm_fnl) : (TextUtils.split(txtNomeCliente.getText().toString(), "-")[2].equals("999999999") ? context.getString(R.string.csm_fnl) : TextUtils.split(txtNomeCliente.getText().toString(), "-")[2])), Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            addNewLineHorizontal(document, "Desc", "Taxa", "P.Unit", "Qt", "Total", facturaReciboFont);
            for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
                addLineSpace(document);
                String preco = String.valueOf(produto.getValue().getPreco());
                String valor = String.valueOf(Objects.requireNonNull(precoTotalUnit.get(produto.getKey())).intValue());
                addNewItem(document, produto.getValue().getNome(), Element.ALIGN_LEFT, font);
                if (!produto.getValue().isIva()) {
                    addNewItem(document, "* " + getRasaoISE(context, produto.getValue().getCodigoMotivoIsencao()), Element.ALIGN_LEFT, new Font(Font.FontFamily.HELVETICA, 20.0f, Font.NORMAL, BaseColor.BLACK));
                }
                addNewLineHorizontal(document, "MSP" + produto.getValue().getId(), produto.getValue().getPercentagemIva() + "%", Ultilitario.formatPreco(preco).replaceAll("Kz", ""), "" + precoTotalUnit.get(produto.getKey()) / produto.getValue().getPreco(), Ultilitario.formatPreco(valor).replaceAll("Kz", ""), font);
            }
            addLineSeparator(document);
            addNewLineWithLeftAndRight(document, "Subtotal", Ultilitario.formatPreco(String.valueOf(totalVenda)), font, font);
            addNewLineWithLeftAndRight(document, "Valor Base", Ultilitario.formatPreco(String.valueOf(percDesc == 0 ? valorBase : valorBase - ((valorBase * percDesc) / 100))), font, font);
            addNewLineWithLeftAndRight(document, "IVA", Ultilitario.formatPreco(String.valueOf(percDesc == 0 ? valorIva : valorIva - ((valorIva * percDesc) / 100))), font, font);
            addNewLineWithLeftAndRight(document, "Desconto" + "(" + percDesc + "%)", Ultilitario.formatPreco(desconto.getText().toString()), font, font);
            addNewLineWithLeftAndRight(document, "Total", Ultilitario.formatPreco(String.valueOf(totalDesconto)), font, font);
            addNewLineWithLeftAndRight(document, "Total Pago", Ultilitario.formatPreco(String.valueOf(valorPago)), font, font);
            addNewLineWithLeftAndRight(document, "Troco", Ultilitario.formatPreco(String.valueOf(troco)), font, font);
            addLineSeparator(document);
            addNewItem(document, formaPagamento, Element.ALIGN_LEFT, font);
            addNewItem(document, "Operador: " + (idOperador > 0 ? " MSU" + idOperador : " MSA0"), Element.ALIGN_LEFT, font);
            if (!nib.isEmpty())
                addNewItem(document, "NIB: " + nib, Element.ALIGN_LEFT, font);
            if (!iban.isEmpty())
                addNewItem(document, "IBAN: " + iban, Element.ALIGN_LEFT, font);
            addLineSeparator(document);
            addNewItem(document, "Os bens/serviços foram colocados a disposição do adquirente na data de " + getDataFormatMonth(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent())) + " e endereço de emissão do documento.", Element.ALIGN_LEFT, font);
            addLineSpace(document);
            addNewItem(document, "Obs: Para devolução consulte as normas internas.", Element.ALIGN_CENTER, font);
            if (!textorodape.isEmpty()) {
                addNewItem(document, textorodape, Element.ALIGN_CENTER, font);
                addLineSeparator(document);
            }
            addNewItem(document, hash.charAt(0) + "" + hash.charAt(10) + "" + hash.charAt(20) + "" + hash.charAt(30) + "-" + "Processado por programa não validado n.º 0000/AGT/0000 MBORASYSTEM", Element.ALIGN_CENTER, font);
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("switch_qr_code", false)) {
                addLineSeparator(document);
                document.add(qr_code_image);
            }
            document.close();
            addFileContentProvider(activity.getApplicationContext(), "/Facturas/" + facturaPath);
            if (!isGuardar)
                printPDF(activity, activity.getBaseContext(), facturaPath, "Facturas");
        } catch (FileNotFoundException | DocumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            if (isAnulado)
                setIntPreference(context, getIntPreference(context, "numeroserienc") - 1, "numeroserienc");
        } finally {
            MainActivity.dismissProgressBar();
        }
    }
}

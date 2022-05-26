package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.FormatarDocumento.addLineSeparator;
import static com.yoga.mborasystem.util.FormatarDocumento.addLineSpace;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewItem;
import static com.yoga.mborasystem.util.FormatarDocumento.addNewLineWithLeftAndRight;
import static com.yoga.mborasystem.util.FormatarDocumento.printPDF;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class RelatorioDiariaVenda {

    public static void getPemissionAcessStoregeExternal(boolean isGuardar, Activity activity, Context context, String facturaPath, Cliente cliente, List<Venda> venda, List<ProdutoVenda> produtoVendas, Handler handler) {
        Dexter.withContext(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        createPdfFile(isGuardar, Common.getAppPath(context, activity.getString(R.string.rel_dia_ven)) + facturaPath, facturaPath, activity, context, cliente, venda, produtoVendas, handler);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        createPdfFile(isGuardar, Common.getAppPath(context, activity.getString(R.string.rel_dia_ven)) + facturaPath, facturaPath, activity, context, cliente, venda, produtoVendas, handler);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private static void createPdfFile(boolean isGuardar, String path, String facturaPath, Activity activity, Context context, Cliente cliente, List<Venda> vendas, List<ProdutoVenda> produtoVendas, Handler handler) {
        MainActivity.getProgressBar();
        if (new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.setMargins(10, 5, 0, 0);
            document.open();
            document.addCreationDate();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 40.0f, Font.BOLD, BaseColor.BLACK);
            addNewItem(document, activity.getString(R.string.rel_vend) + "(" + cliente.getNomeEmpresa() + ")", Element.ALIGN_CENTER, titleFont);
            Font bairroRuaFont = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, cliente.getBairro() + "\n" + cliente.getRua(), Element.ALIGN_CENTER, bairroRuaFont);
            addLineSpace(document);
            Font font = new Font(Font.FontFamily.HELVETICA, 36.0f, Font.NORMAL, BaseColor.BLACK);
            addNewItem(document, context.getString(R.string.nif) + " " + cliente.getNifbi(), Element.ALIGN_LEFT, font);
            addNewItem(document, context.getString(R.string.tel) + " " + cliente.getTelefone() + " / " + cliente.getTelefonealternativo(), Element.ALIGN_LEFT, font);
            addNewItem(document, activity.getString(R.string.data) + Ultilitario.getDateCurrent(), Element.ALIGN_LEFT, font);
            addLineSpace(document);
            addNewItem(document, activity.getString(R.string.vendas), Element.ALIGN_CENTER, titleFont);
            for (Venda venda : vendas) {
                addLineSeparator(document);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.cliente), activity.getString(R.string.cod_qr), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, venda.getNome_cliente(), venda.getCodigo_qr(), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.quantidade), activity.getString(R.string.total), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, String.valueOf(venda.getQuantidade()), Ultilitario.formatPreco(String.valueOf(venda.getTotal_venda())), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.desconto), activity.getString(R.string.total_desc), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, Ultilitario.formatPreco(String.valueOf(venda.getDesconto())), Ultilitario.formatPreco(String.valueOf(venda.getTotal_desconto())), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.valor_pago), activity.getString(R.string.dvd), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, Ultilitario.formatPreco(String.valueOf(venda.getValor_pago())), Ultilitario.formatPreco(String.valueOf(venda.getDivida())), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.valor_base), activity.getString(R.string.montante_iva), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, Ultilitario.formatPreco(String.valueOf(venda.getValor_base())), Ultilitario.formatPreco(String.valueOf(venda.getValor_iva())), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.forma_pagamento), venda.getPagamento(), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.dat_ven), venda.getData_cria(), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.operador), (venda.getIdoperador() > 0 ? " MSU" + venda.getIdoperador() : " MSA" + venda.getIdoperador()), font, font);
            }
            addLineSeparator(document);
            addNewItem(document, activity.getString(R.string.produtos), Element.ALIGN_CENTER, titleFont);
            for (ProdutoVenda produto : produtoVendas) {
                addLineSeparator(document);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.prod), activity.getString(R.string.venda), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, produto.getNome_produto(), produto.getCodigo_Barra(), font, font);
                addNewLineWithLeftAndRight(document, activity.getString(R.string.quantidade), activity.getString(R.string.total), titleFont, titleFont);
                addNewLineWithLeftAndRight(document, String.valueOf(produto.getQuantidade()), Ultilitario.formatPreco(String.valueOf(produto.getPreco_total())), font, font);
            }
            addLineSeparator(document);
            addLineSpace(document);
            addNewItem(document, "MboraSystem", Element.ALIGN_CENTER, titleFont);
            document.close();
            handler.post(() -> Toast.makeText(context, activity.getString(R.string.rel_ven_dia_gua), Toast.LENGTH_LONG).show());
            if (!isGuardar)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    printPDF(activity, activity.getBaseContext(), facturaPath, activity.getString(R.string.rel_dia_ven));
                } else {
                    Ultilitario.showToast(activity.getBaseContext(), Color.parseColor("#795548"), activity.getString(R.string.precisa_kitkat_maior), R.drawable.ic_toast_erro);
                }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            MainActivity.dismissProgressBar();
        }
    }
}

package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.Ultilitario.formatarValor;
import static com.yoga.mborasystem.util.Ultilitario.getDataFormatMonth;
import static com.yoga.mborasystem.util.Ultilitario.getFilePathCache;
import static com.yoga.mborasystem.util.Ultilitario.getRasaoISE;
import static com.yoga.mborasystem.util.Ultilitario.getValueWithDesconto;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

@Keep
public class SaftXMLDocument {

    private void criarElemento(Document doc, String elementoFilho, Element elementPai, String valorElementoFilho) {
        Element element = doc.createElement(elementoFilho);
        elementPai.appendChild(element);
        element.setTextContent(valorElementoFilho);
    }

    private void setAtributo(Document doc, String atributo, String valor, Element element) {
        Attr attr = doc.createAttribute(atributo);
        attr.setValue(valor);
        element.setAttributeNode(attr);
    }

    private boolean validateXMLSchema(String xsdPath, String xmlPath) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(xsdPath));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new File(xmlPath)));
        return true;
    }

    public void criarDocumentoSaft(Context context, Cliente cliente, String dataInicio, String dataFim, List<ClienteCantina> clienteCantina, List<ProdutoVenda> produtoVendas, List<Venda> vendas) throws ParserConfigurationException, TransformerException, IOException, SAXException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String[] dataHora = TextUtils.split(sdf.format(new Date()), " ");

        List<Venda> notaCredito = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        doc.normalize();

        // root element
        Element rootElement = doc.createElement("AuditFile");
        doc.appendChild(rootElement);
        setAtributo(doc, "xmlns", "urn:OECD:StandardAuditFile-Tax:AO_1.01_01", rootElement);
        setAtributo(doc, "xsi:schemaLocation", "urn:OECD:StandardAuditFile-Tax:AO_1.01_01 SAFTAO1.01_01.xsd", rootElement);
        setAtributo(doc, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", rootElement);

        Element header = doc.createElement("Header");
        rootElement.appendChild(header);

        criarElemento(doc, "AuditFileVersion", header, "1.01_01");
        criarElemento(doc, "CompanyID", header, cliente.getNifbi());
        criarElemento(doc, "TaxRegistrationNumber", header, cliente.getNifbi());
        criarElemento(doc, "TaxAccountingBasis", header, "F"); // Tipo de Sistema = F, C, I
        criarElemento(doc, "CompanyName", header, cliente.getNomeEmpresa());

        Element companyAddress = doc.createElement("CompanyAddress");
        header.appendChild(companyAddress);
        criarElemento(doc, "AddressDetail", companyAddress, cliente.getRua() + ", " + cliente.getBairro() + ", " + cliente.getMunicipio());
        criarElemento(doc, "City", companyAddress, cliente.getProvincia());
        criarElemento(doc, "Province", companyAddress, cliente.getProvincia());
        criarElemento(doc, "Country", companyAddress, "AO");

        criarElemento(doc, "FiscalYear", header, TextUtils.split(dataInicio, "-")[0].trim());
        criarElemento(doc, "StartDate", header, dataInicio);
        criarElemento(doc, "EndDate", header, dataFim);
        criarElemento(doc, "CurrencyCode", header, "AOA");
        criarElemento(doc, "DateCreated", header, dataHora[0]);
        criarElemento(doc, "TaxEntity", header, "Global"); // Ficheiro de Logística e Tesouraria
        criarElemento(doc, "ProductCompanyTaxID", header, "5000999784"); // Identificação fiscal da entidade produtora do software
        criarElemento(doc, "SoftwareValidationNumber", header, "000/AGT/0000");
        criarElemento(doc, "ProductID", header, "MBORASYSTEM/YOGA - TECNOLOGIA, COMERCIO E SERVIÇOS, LDA");
        criarElemento(doc, "ProductVersion", header, "1.0");
        criarElemento(doc, "Telephone", header, cliente.getTelefone());
        criarElemento(doc, "Email", header, cliente.getEmail());

        Element masterFiles = doc.createElement("MasterFiles");
        rootElement.appendChild(masterFiles);
        if (clienteCantina.isEmpty())
            elementCustomer(doc, masterFiles, "1", "999999999", "Consumidor Final", "Desconhecido", "Desconhecido", "Desconhecido", "Desconhecido", "Desconhecido");
        else for (ClienteCantina cc : clienteCantina)
            elementCustomer(doc, masterFiles, String.valueOf(cc.getId()), cc.getNif().isEmpty() ? "999999999" : cc.getNif(), cc.getNome(), isEmpty(cc.getEndereco()), cliente.getProvincia(), cliente.getProvincia(), isEmpty(cc.getTelefone().trim()), isEmpty(cc.getEmail().trim()));

        Map<Long, ProdutoVenda> produtoVendaMap = new HashMap<>();
        for (ProdutoVenda pv : produtoVendas)
            produtoVendaMap.put(pv.getId(), pv);

        for (Map.Entry<Long, ProdutoVenda> pv : produtoVendaMap.entrySet()) {
            Element product = doc.createElement("Product");
            masterFiles.appendChild(product);
            criarElemento(doc, "ProductType", product, pv.getValue().getTipo());
            criarElemento(doc, "ProductCode", product, String.valueOf(pv.getValue().getId()));
            criarElemento(doc, "ProductDescription", product, pv.getValue().getNome_produto());
            criarElemento(doc, "ProductNumberCode", product, pv.getValue().getCodigo_Barra().isEmpty() ? String.valueOf(pv.getValue().getId()) : pv.getValue().getCodigo_Barra());
        }
        Map<String, TaxTable> tabelaImposto = new HashMap<>();
        for (ProdutoVenda pv : produtoVendas) {
            String taxType = pv.isIva() ? "IVA" : "NS";
            String taxCode = pv.isIva() ? (pv.getPercentagemIva() == 14 ? "NOR" : "OUT") : "NS";
            String description = pv.isIva() ? (pv.getPercentagemIva() == 14 ? "Normal" : "Outros") : "Não Sujeição";
            String taxPercentage = String.valueOf(pv.getPercentagemIva());
            String key = (taxType + taxCode + description + taxPercentage).trim();
            tabelaImposto.put(key, new TaxTable(taxType, taxCode, description, taxPercentage));
        }
        Element taxTable = doc.createElement("TaxTable"); // Tabela de imposto
        masterFiles.appendChild(taxTable);
        for (Map.Entry<String, TaxTable> tax : tabelaImposto.entrySet()) {
            Element taxTableEntry = doc.createElement("TaxTableEntry");
            taxTable.appendChild(taxTableEntry);
            criarElemento(doc, "TaxType", taxTableEntry, tax.getValue().taxType);
            criarElemento(doc, "TaxCountryRegion", taxTableEntry, "AO");
            criarElemento(doc, "TaxCode", taxTableEntry, tax.getValue().taxCode);
            criarElemento(doc, "Description", taxTableEntry, tax.getValue().description);
            criarElemento(doc, "TaxPercentage", taxTableEntry, tax.getValue().taxPercentage);
        }

        Element sourceDocuments = doc.createElement("SourceDocuments");
        rootElement.appendChild(sourceDocuments);
        int totalCredit = 0, totalDebit = 0, numNC = 0;

        for (Venda vd : vendas) {
            if (vd.getEstado() == 3) {
                numNC += 1;
                totalDebit += vd.getDesconto() == 0 ? vd.getValor_base() : getValueWithDesconto(vd.getValor_base(), vd.getPercentagemDesconto());
            }
            totalCredit += vd.getDesconto() == 0 ? vd.getValor_base() : getValueWithDesconto(vd.getValor_base(), vd.getPercentagemDesconto());
        }
        Element salesInvoices = doc.createElement("SalesInvoices");
        sourceDocuments.appendChild(salesInvoices);
        criarElemento(doc, "NumberOfEntries", salesInvoices, String.valueOf(vendas.size() + numNC));
        criarElemento(doc, "TotalDebit", salesInvoices, formatarValor(totalDebit));
        criarElemento(doc, "TotalCredit", salesInvoices, formatarValor(totalCredit));

        for (Venda vd : vendas) {// FACTURA RECIBO
            Element invoice = doc.createElement("Invoice");
            salesInvoices.appendChild(invoice);
            criarElemento(doc, "InvoiceNo", invoice, vd.getReferenciaFactura());

            Element documentStatus = doc.createElement("DocumentStatus");
            invoice.appendChild(documentStatus);
            criarElemento(doc, "InvoiceStatus", documentStatus, "N");
            criarElemento(doc, "InvoiceStatusDate", documentStatus, vd.getData_cria_hora());
            criarElemento(doc, "SourceID", documentStatus, String.valueOf(vd.getId()));
            criarElemento(doc, "SourceBilling", documentStatus, "P");

            criarElemento(doc, "Hash", invoice, vd.getHashFR());
            criarElemento(doc, "HashControl", invoice, "1");
            criarElemento(doc, "Period", invoice, TextUtils.split(getDataFormatMonth(vd.getData_cria()), "-")[1].trim());
            criarElemento(doc, "InvoiceDate", invoice, getDataFormatMonth(vd.getData_cria()));
            criarElemento(doc, "InvoiceType", invoice, "FR");

            Element specialRegimes = doc.createElement("SpecialRegimes");
            invoice.appendChild(specialRegimes);
            criarElemento(doc, "SelfBillingIndicator", specialRegimes, "0");
            criarElemento(doc, "CashVATSchemeIndicator", specialRegimes, "0");
            criarElemento(doc, "ThirdPartiesBillingIndicator", specialRegimes, "0");

            criarElemento(doc, "SourceID", invoice, String.valueOf(vd.getId()));
            criarElemento(doc, "SystemEntryDate", invoice, vd.getData_cria_hora());
            criarElemento(doc, "CustomerID", invoice, String.valueOf(vd.getIdclicant() == 0 ? 1 : vd.getIdclicant()));

            Element shipTo = doc.createElement("ShipTo");
            invoice.appendChild(shipTo);

            Element address = doc.createElement("Address");
            shipTo.appendChild(address);
            boolean v = false;
            if (clienteCantina.isEmpty()) {
                v = true;
                addressCustomer(doc, address, "Desconhecido", "Desconhecido", "Desconhecido");
            } else for (ClienteCantina cc : clienteCantina) {
                if (cc.getId() == vd.getIdclicant()) {
                    v = true;
                    addressCustomer(doc, address, isEmpty(cc.getEndereco()), cliente.getProvincia(), cliente.getProvincia());
                    break;
                }
            }
            if (!v) addressCustomer(doc, address, "Desconhecido", "Desconhecido", "Desconhecido");
            int i = 0;
            for (ProdutoVenda pv : produtoVendas) {
                if (pv.getIdvenda() == vd.getId()) {

                    int precoUnitario = (pv.getPreco_total() / pv.getQuantidade());
                    int precoUnitarioSemIVA = (int) (precoUnitario / Float.parseFloat(pv.getPercentagemIva() == 14 ? "1." + pv.getPercentagemIva() : "1.0" + pv.getPercentagemIva()));

                    Element line = doc.createElement("Line");
                    invoice.appendChild(line);
                    criarElemento(doc, "LineNumber", line, String.valueOf(++i));

                    criarElemento(doc, "ProductCode", line, String.valueOf(pv.getId()));
                    criarElemento(doc, "ProductDescription", line, pv.getNome_produto().trim());
                    criarElemento(doc, "Quantity", line, String.valueOf(pv.getQuantidade()));
                    criarElemento(doc, "UnitOfMeasure", line, pv.getUnidade());
                    criarElemento(doc, "UnitPrice", line, vd.getDesconto() > 0 ? formatarValor(getValueWithDesconto(precoUnitarioSemIVA, vd.getPercentagemDesconto())) : (pv.isIva() ? formatarValor(precoUnitarioSemIVA) : formatarValor(precoUnitario)));
                    criarElemento(doc, "TaxPointDate", line, getDataFormatMonth(vd.getData_cria()));

                    criarElemento(doc, "Description", line, pv.getNome_produto().trim());
                    int creditAmount = precoUnitarioSemIVA * pv.getQuantidade();
                    criarElemento(doc, "CreditAmount", line, formatarValor(vd.getDesconto() > 0 ? getValueWithDesconto(creditAmount, vd.getPercentagemDesconto()) : creditAmount));

                    Element tax = doc.createElement("Tax");
                    line.appendChild(tax);
                    criarElemento(doc, "TaxType", tax, pv.isIva() ? "IVA" : "NS");
                    criarElemento(doc, "TaxCountryRegion", tax, "AO");
                    criarElemento(doc, "TaxCode", tax, pv.isIva() ? (pv.getPercentagemIva() == 14 ? "NOR" : "OUT") : "NS");
                    criarElemento(doc, "TaxPercentage", tax, String.valueOf(pv.getPercentagemIva()));
                    if (pv.getPercentagemIva() == 0) {
                        criarElemento(doc, "TaxExemptionReason", line, getRasaoISE(context, pv.getCodigoMotivoIsencao()));
                        criarElemento(doc, "TaxExemptionCode", line, pv.getCodigoMotivoIsencao());
                    }
                    int settlementAmount = vd.getDesconto() == 0 ? 0 : (creditAmount * vd.getPercentagemDesconto()) / 100;
                    criarElemento(doc, "SettlementAmount", line, formatarValor(settlementAmount));
                }
            }
            Element documentTotals = doc.createElement("DocumentTotals");
            invoice.appendChild(documentTotals);

            int taxPayable = vd.getDesconto() == 0 ? vd.getValor_iva() : getValueWithDesconto(vd.getValor_iva(), vd.getPercentagemDesconto());
            int netTotal = vd.getDesconto() == 0 ? vd.getValor_base() : getValueWithDesconto(vd.getValor_base(), vd.getPercentagemDesconto());
            int grossTotal = vd.getDesconto() == 0 ? taxPayable + vd.getValor_base() : getValueWithDesconto(vd.getTotal_venda(), vd.getPercentagemDesconto());

            criarElemento(doc, "TaxPayable", documentTotals, formatarValor(taxPayable));
            criarElemento(doc, "NetTotal", documentTotals, formatarValor(netTotal));
            criarElemento(doc, "GrossTotal", documentTotals, formatarValor(grossTotal));

            Element currency = doc.createElement("Currency");
            documentTotals.appendChild(currency);
            criarElemento(doc, "CurrencyCode", currency, "AOA");
            criarElemento(doc, "CurrencyAmount", currency, formatarValor(grossTotal));
            criarElemento(doc, "ExchangeRate", currency, "0");

            Element payment = doc.createElement("Payment");
            documentTotals.appendChild(payment);
            criarElemento(doc, "PaymentAmount", payment, formatarValor(vd.getValor_pago())); // Valor do pagamento
            criarElemento(doc, "PaymentDate", payment, getDataFormatMonth(vd.getData_cria()));

            Element withholdingTax = doc.createElement("WithholdingTax");
            invoice.appendChild(withholdingTax);
            criarElemento(doc, "WithholdingTaxAmount", withholdingTax, formatarValor(taxPayable)); // Valor do imposto retido na fonte

            if (vd.getEstado() == 3 && !vd.getReferenciaNC().isEmpty()) {
                notaCredito.add(vd);
            }
        }
        //NOTA DE CRÉDITO
        Collections.sort(notaCredito, (v1, v2) -> { // ORDER NOTA DE CRÉDITO
            int n1 = Integer.parseInt(TextUtils.split(v1.getReferenciaNC(), "/")[1]);
            int n2 = Integer.parseInt(TextUtils.split(v2.getReferenciaNC(), "/")[1]);
            return n1 - n2;
        });
        for (Venda vd : notaCredito) { //NOTA DE CRÉDITO
            Element invoiceNC = doc.createElement("Invoice");
            salesInvoices.appendChild(invoiceNC);
            criarElemento(doc, "InvoiceNo", invoiceNC, vd.getReferenciaNC());

            Element documentStatusNC = doc.createElement("DocumentStatus");
            invoiceNC.appendChild(documentStatusNC);
            criarElemento(doc, "InvoiceStatus", documentStatusNC, "N");
            criarElemento(doc, "InvoiceStatusDate", documentStatusNC, vd.getData_cria_hora_NC());
            criarElemento(doc, "SourceID", documentStatusNC, String.valueOf(vd.getId()));
            criarElemento(doc, "SourceBilling", documentStatusNC, "P");

            criarElemento(doc, "Hash", invoiceNC, vd.getHashNC());
            criarElemento(doc, "HashControl", invoiceNC, "1");
            criarElemento(doc, "Period", invoiceNC, TextUtils.split(getDataFormatMonth(vd.getData_cria_NC()), "-")[1].trim());
            criarElemento(doc, "InvoiceDate", invoiceNC, getDataFormatMonth(vd.getData_cria_NC()));
            criarElemento(doc, "InvoiceType", invoiceNC, "NC");

            Element specialRegimesNC = doc.createElement("SpecialRegimes");
            invoiceNC.appendChild(specialRegimesNC);
            criarElemento(doc, "SelfBillingIndicator", specialRegimesNC, "0");
            criarElemento(doc, "CashVATSchemeIndicator", specialRegimesNC, "0");
            criarElemento(doc, "ThirdPartiesBillingIndicator", specialRegimesNC, "0");

            criarElemento(doc, "SourceID", invoiceNC, String.valueOf(vd.getId()));
            criarElemento(doc, "SystemEntryDate", invoiceNC, vd.getData_cria_hora_NC());
            criarElemento(doc, "CustomerID", invoiceNC, String.valueOf(vd.getIdclicant() == 0 ? 1 : vd.getIdclicant()));

            Element shipToNC = doc.createElement("ShipTo");
            invoiceNC.appendChild(shipToNC);

            Element addressNC = doc.createElement("Address");
            shipToNC.appendChild(addressNC);
            boolean vNC = false;
            if (clienteCantina.isEmpty()) {
                vNC = true;
                addressCustomer(doc, addressNC, "Desconhecido", "Desconhecido", "Desconhecido");
            } else for (ClienteCantina cc : clienteCantina) {
                if (cc.getId() == vd.getIdclicant()) {
                    vNC = true;
                    addressCustomer(doc, addressNC, isEmpty(cc.getEndereco()), cliente.getProvincia(), cliente.getProvincia());
                    break;
                }
            }
            if (!vNC)
                addressCustomer(doc, addressNC, "Desconhecido", "Desconhecido", "Desconhecido");
            int iNC = 0;
            for (ProdutoVenda pv : produtoVendas) {
                if (pv.getIdvenda() == vd.getId()) {

                    int precoUnitario = (pv.getPreco_total() / pv.getQuantidade());
                    int precoUnitarioSemIVA = (int) (precoUnitario / Float.parseFloat(pv.getPercentagemIva() == 14 ? "1." + pv.getPercentagemIva() : "1.0" + pv.getPercentagemIva()));

                    Element lineNC = doc.createElement("Line");
                    invoiceNC.appendChild(lineNC);
                    criarElemento(doc, "LineNumber", lineNC, String.valueOf(++iNC));

                    criarElemento(doc, "ProductCode", lineNC, String.valueOf(pv.getId()));
                    criarElemento(doc, "ProductDescription", lineNC, pv.getNome_produto().trim());
                    criarElemento(doc, "Quantity", lineNC, String.valueOf(pv.getQuantidade()));
                    criarElemento(doc, "UnitOfMeasure", lineNC, pv.getUnidade());
                    criarElemento(doc, "UnitPrice", lineNC, vd.getDesconto() > 0 ? formatarValor(getValueWithDesconto(precoUnitarioSemIVA, vd.getPercentagemDesconto())) : (pv.isIva() ? formatarValor(precoUnitarioSemIVA) : formatarValor(precoUnitario)));
                    criarElemento(doc, "TaxPointDate", lineNC, getDataFormatMonth(vd.getData_cria_NC()));

                    Element referencesNC = doc.createElement("References");
                    lineNC.appendChild(referencesNC);
                    criarElemento(doc, "Reference", referencesNC, vd.getReferenciaFactura());
                    criarElemento(doc, "Reason", referencesNC, vd.getMotivoEmissaoNC());

                    criarElemento(doc, "Description", lineNC, pv.getNome_produto().trim());

                    int debitAmount = precoUnitarioSemIVA * pv.getQuantidade();
                    criarElemento(doc, "DebitAmount", lineNC, formatarValor(vd.getDesconto() > 0 ? getValueWithDesconto(debitAmount, vd.getPercentagemDesconto()) : debitAmount));

                    Element taxNC = doc.createElement("Tax");
                    lineNC.appendChild(taxNC);
                    criarElemento(doc, "TaxType", taxNC, pv.isIva() ? "IVA" : "NS");
                    criarElemento(doc, "TaxCountryRegion", taxNC, "AO");
                    criarElemento(doc, "TaxCode", taxNC, pv.isIva() ? (pv.getPercentagemIva() == 14 ? "NOR" : "OUT") : "NS");
                    criarElemento(doc, "TaxPercentage", taxNC, String.valueOf(pv.getPercentagemIva()));
                    if (pv.getPercentagemIva() == 0) {
                        criarElemento(doc, "TaxExemptionReason", lineNC, getRasaoISE(context, pv.getCodigoMotivoIsencao()));
                        criarElemento(doc, "TaxExemptionCode", lineNC, pv.getCodigoMotivoIsencao());
                    }
                    int settlementAmount = vd.getDesconto() == 0 ? 0 : (debitAmount * vd.getPercentagemDesconto()) / 100;
                    criarElemento(doc, "SettlementAmount", lineNC, formatarValor(settlementAmount));
                    Log.i("DESCONTO", vd.getDesconto() > 0 ? formatarValor(debitAmount) + "" : "");
                    Log.i("DESCONTO", vd.getDesconto() > 0 ? vd.getPercentagemDesconto() + "" : "");
                    Log.i("DESCONTO", vd.getDesconto() > 0 ? formatarValor(settlementAmount) + "" : "");
                }
            }
            Element documentTotalsNC = doc.createElement("DocumentTotals");
            invoiceNC.appendChild(documentTotalsNC);

            int taxPayableNC = vd.getDesconto() == 0 ? vd.getValor_iva() : getValueWithDesconto(vd.getValor_iva(), vd.getPercentagemDesconto());
            int netTotalNC = vd.getDesconto() == 0 ? vd.getValor_base() : getValueWithDesconto(vd.getValor_base(), vd.getPercentagemDesconto());
            int grossTotalNC = vd.getDesconto() == 0 ? taxPayableNC + vd.getValor_base() : getValueWithDesconto(vd.getTotal_venda(), vd.getPercentagemDesconto());

            criarElemento(doc, "TaxPayable", documentTotalsNC, formatarValor(taxPayableNC));
            criarElemento(doc, "NetTotal", documentTotalsNC, formatarValor(netTotalNC));
            criarElemento(doc, "GrossTotal", documentTotalsNC, formatarValor(grossTotalNC));

            Element currencyNC = doc.createElement("Currency");
            documentTotalsNC.appendChild(currencyNC);
            criarElemento(doc, "CurrencyCode", currencyNC, "AOA");
            criarElemento(doc, "CurrencyAmount", currencyNC, formatarValor(grossTotalNC));
            criarElemento(doc, "ExchangeRate", currencyNC, "0");

            Element paymentNC = doc.createElement("Payment");
            documentTotalsNC.appendChild(paymentNC);
            criarElemento(doc, "PaymentAmount", paymentNC, formatarValor(vd.getValor_pago())); // Valor do pagamento
            criarElemento(doc, "PaymentDate", paymentNC, getDataFormatMonth(vd.getData_cria()));

            Element withholdingTaxNC = doc.createElement("WithholdingTax");
            invoiceNC.appendChild(withholdingTaxNC);
            criarElemento(doc, "WithholdingTaxAmount", withholdingTaxNC, formatarValor(taxPayableNC)); // Valor do imposto retido na fonte
        }

        String FILE_PATH_SAFT_AO = Common.getAppPath("SAFT-AO") + dataHora[0] + "T" + dataHora[1] + "SAFT.xml";

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        OutputStream outputStream = new FileOutputStream(FILE_PATH_SAFT_AO);
        StreamResult result = new StreamResult(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        transformer.transform(new DOMSource(doc), result);

        if (validateXMLSchema(getFilePathCache(context, "SAFTAO1.01_01.xsd").getAbsolutePath(), FILE_PATH_SAFT_AO)) {
            new AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_baseline_done_24)
                    .setTitle(context.getString(R.string.doc_saft_expo))
                    .setMessage(FILE_PATH_SAFT_AO)
                    .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss())
                    .setNegativeButton(context.getString(R.string.partilhar), (dialogInterface, i) -> Ultilitario.partilharDocumento(FILE_PATH_SAFT_AO, context, "application/xml", context.getString(R.string.part_doc)))
                    .show();
        }
    }

    private void elementCustomer(Document doc, Element masterFiles, String customerID, String customerTaxID, String companyName, String addressDetail, String city, String province, String telephone, String email) {
        Element customer = doc.createElement("Customer");
        masterFiles.appendChild(customer);
        criarElemento(doc, "CustomerID", customer, customerID);
        criarElemento(doc, "AccountID", customer, "Desconhecido");
        criarElemento(doc, "CustomerTaxID", customer, customerTaxID);
        criarElemento(doc, "CompanyName", customer, companyName);

        Element billingAddress = doc.createElement("BillingAddress");
        customer.appendChild(billingAddress);
        addressCustomer(doc, billingAddress, addressDetail, city, province);

        Element shipToAddress = doc.createElement("ShipToAddress");
        customer.appendChild(shipToAddress);
        addressCustomer(doc, shipToAddress, "Desconhecido", "Desconhecido", "Desconhecido");

        criarElemento(doc, "Telephone", customer, telephone);
        criarElemento(doc, "Email", customer, email);
        criarElemento(doc, "SelfBillingIndicator", customer, "0");
    }

    private void addressCustomer(Document doc, Element elementoPai, String addressDetail, String city, String province) {
        criarElemento(doc, "AddressDetail", elementoPai, addressDetail);
        criarElemento(doc, "City", elementoPai, city);
        criarElemento(doc, "Province", elementoPai, province);
        criarElemento(doc, "Country", elementoPai, "AO");
    }

    private String isEmpty(String valor) {
        return valor.isEmpty() ? "Desconhecido" : valor;
    }

    @Keep
    private static class TaxTable {

        private final String taxType, taxCode, description, taxPercentage;

        public TaxTable(String taxType, String taxCode, String description, String taxPercentage) {
            this.taxType = taxType;
            this.taxCode = taxCode;
            this.description = description;
            this.taxPercentage = taxPercentage;
        }
    }
}

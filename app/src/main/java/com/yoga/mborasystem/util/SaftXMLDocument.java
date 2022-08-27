package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.Ultilitario.addFileContentProvider;

import android.content.Context;
import android.widget.Toast;

import com.yoga.mborasystem.model.entidade.Cliente;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    private File getPathXSDCacheFile(Context context) throws IOException {
        File cacheFile = new File(context.getCacheDir(), "SAFTAO1.01_01.xsd");
        try (InputStream inputStream = context.getAssets().open("SAFTAO1.01_01.xsd")) {
            try (FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
        }
        return cacheFile;
    }

    private boolean validateXMLSchema(String xsdPath, String xmlPath) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(xsdPath));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new File(xmlPath)));
        return true;
    }

    public void criarDocumentoSaft(Context context, Cliente cliente, String dataInicio, String dataFim) throws ParserConfigurationException, TransformerException, IOException, SAXException {

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
        criarElemento(doc, "TaxAccountingBasis", header, "F");
        criarElemento(doc, "CompanyName", header, cliente.getNomeEmpresa());

        Element companyAddress = doc.createElement("CompanyAddress");
        header.appendChild(companyAddress);
        criarElemento(doc, "AddressDetail", companyAddress, cliente.getRua() + ", " + cliente.getBairro() + ", " + cliente.getMunicipio());
        criarElemento(doc, "City", companyAddress, cliente.getProvincia());
        criarElemento(doc, "Province", companyAddress, cliente.getProvincia());
        criarElemento(doc, "Country", companyAddress, "AO");

        criarElemento(doc, "FiscalYear", header, "2022");
        criarElemento(doc, "StartDate", header, dataInicio);
        criarElemento(doc, "EndDate", header, dataFim);
        criarElemento(doc, "CurrencyCode", header, "AOA");
        criarElemento(doc, "DateCreated", header, "2012-12-13");
        criarElemento(doc, "TaxEntity", header, "Global");
        criarElemento(doc, "ProductCompanyTaxID", header, "5000999784");
        criarElemento(doc, "SoftwareValidationNumber", header, "308/AGT/2021");
        criarElemento(doc, "ProductID", header, "MBORASYSTEM/YOGA ANGOLA,LDA");
        criarElemento(doc, "ProductVersion", header, "1");
        criarElemento(doc, "Telephone", header, "932359808");
        criarElemento(doc, "Fax", header, "antonio@gmail.com");
        criarElemento(doc, "Email", header, "antonio@gmail.com");
        criarElemento(doc, "Website", header, "www.yoga.com");

        Element masterFiles = doc.createElement("MasterFiles");
        rootElement.appendChild(masterFiles);

        Element customer = doc.createElement("Customer");
        masterFiles.appendChild(customer);
        criarElemento(doc, "CustomerID", customer, "1");
        criarElemento(doc, "AccountID", customer, "Desconhecido");
        criarElemento(doc, "CustomerTaxID", customer, "1234");
        criarElemento(doc, "CompanyName", customer, "Consumidor Final");

        Element billingAddress = doc.createElement("BillingAddress");
        customer.appendChild(billingAddress);
        criarElemento(doc, "AddressDetail", billingAddress, "Benfica");
        criarElemento(doc, "City", billingAddress, "Luanda");
        criarElemento(doc, "Province", billingAddress, "Luanda");
        criarElemento(doc, "Country", billingAddress, "AO");

        Element shipToAddress = doc.createElement("ShipToAddress");
        customer.appendChild(shipToAddress);
        criarElemento(doc, "AddressDetail", shipToAddress, "Benfica");
        criarElemento(doc, "City", shipToAddress, "Luanda");
        criarElemento(doc, "Province", shipToAddress, "Luanda");
        criarElemento(doc, "Country", shipToAddress, "AO");

        criarElemento(doc, "Telephone", customer, "936566987");
        criarElemento(doc, "Fax", customer, "matias@gmail.com");
        criarElemento(doc, "Email", customer, "matias@gmail.com");
        criarElemento(doc, "Website", customer, "www.yoga.com");
        criarElemento(doc, "SelfBillingIndicator", customer, "0");

        Element product = doc.createElement("Product");
        masterFiles.appendChild(product);
        criarElemento(doc, "ProductType", product, "P");
        criarElemento(doc, "ProductCode", product, "123456789");
        criarElemento(doc, "ProductDescription", product, "Arroz");
        criarElemento(doc, "ProductNumberCode", product, "123456789");

        Element taxTable = doc.createElement("TaxTable");
        masterFiles.appendChild(taxTable);

        Element taxTableEntry = doc.createElement("TaxTableEntry");
        taxTable.appendChild(taxTableEntry);
        criarElemento(doc, "TaxType", taxTableEntry, "IVA");
        criarElemento(doc, "TaxCountryRegion", taxTableEntry, "AO");
        criarElemento(doc, "TaxCode", taxTableEntry, "NOR");
        criarElemento(doc, "Description", taxTableEntry, "Desconhecido");
        criarElemento(doc, "TaxExpirationDate", taxTableEntry, "2012-12-13");
        criarElemento(doc, "TaxPercentage", taxTableEntry, "123.45");
        criarElemento(doc, "TaxAmount", taxTableEntry, "0.00");

        Element sourceDocuments = doc.createElement("SourceDocuments");
        rootElement.appendChild(sourceDocuments);

        Element salesInvoices = doc.createElement("SalesInvoices");
        sourceDocuments.appendChild(salesInvoices);
        criarElemento(doc, "NumberOfEntries", salesInvoices, "33");
        criarElemento(doc, "TotalDebit", salesInvoices, "123.45");
        criarElemento(doc, "TotalCredit", salesInvoices, "123.45");

        Element invoice = doc.createElement("Invoice");
        salesInvoices.appendChild(invoice);
        criarElemento(doc, "InvoiceNo", invoice, "FT S001/1");

        Element documentStatus = doc.createElement("DocumentStatus");
        invoice.appendChild(documentStatus);
        criarElemento(doc, "InvoiceStatus", documentStatus, "N");
        criarElemento(doc, "InvoiceStatusDate", documentStatus, "2012-12-13T12:12:12");
        criarElemento(doc, "SourceID", documentStatus, "Desconhecido");
        criarElemento(doc, "SourceBilling", documentStatus, "P");

        criarElemento(doc, "Hash", invoice, "145568567645gfgsgsfsfd");
        criarElemento(doc, "HashControl", invoice, "gfdgxdrfg454ddfd");
        criarElemento(doc, "Period", invoice, "12");
        criarElemento(doc, "InvoiceDate", invoice, "2012-12-13");
        criarElemento(doc, "InvoiceType", invoice, "FT");

        Element specialRegimes = doc.createElement("SpecialRegimes");
        invoice.appendChild(specialRegimes);
        criarElemento(doc, "SelfBillingIndicator", specialRegimes, "0");
        criarElemento(doc, "CashVATSchemeIndicator", specialRegimes, "0");
        criarElemento(doc, "ThirdPartiesBillingIndicator", specialRegimes, "0");

        criarElemento(doc, "SourceID", invoice, "1");
        criarElemento(doc, "SystemEntryDate", invoice, "2012-12-13T12:12:12");
        criarElemento(doc, "CustomerID", invoice, "1");

        Element shipTo = doc.createElement("ShipTo");
        invoice.appendChild(shipTo);
        criarElemento(doc, "DeliveryID", shipTo, "1");
        criarElemento(doc, "DeliveryDate", shipTo, "2012-12-13");

        Element address = doc.createElement("Address");
        shipTo.appendChild(address);

        criarElemento(doc, "BuildingNumber", address, "124");
        criarElemento(doc, "StreetName", address, "Benfica");
        criarElemento(doc, "AddressDetail", address, "Benfica");
        criarElemento(doc, "City", address, "Luanda");
        criarElemento(doc, "Province", address, "Luanda");
        criarElemento(doc, "Country", address, "AO");

        Element shipFrom = doc.createElement("ShipFrom");
        invoice.appendChild(shipFrom);
        criarElemento(doc, "DeliveryID", shipFrom, "1");
        criarElemento(doc, "DeliveryDate", shipFrom, "2012-12-13");

        Element address1 = doc.createElement("Address");
        shipFrom.appendChild(address1);

        criarElemento(doc, "BuildingNumber", address1, "124");
        criarElemento(doc, "StreetName", address1, "Benfica");
        criarElemento(doc, "AddressDetail", address1, "Benfica");
        criarElemento(doc, "City", address1, "Luanda");
        criarElemento(doc, "Province", address1, "Luanda");
        criarElemento(doc, "Country", address1, "AO");

        Element line = doc.createElement("Line");
        invoice.appendChild(line);
        criarElemento(doc, "LineNumber", line, "33");

        Element orderReferences = doc.createElement("OrderReferences");
        line.appendChild(orderReferences);
        criarElemento(doc, "OriginatingON", orderReferences, "33");
        criarElemento(doc, "OrderDate", orderReferences, "2012-12-13");

        criarElemento(doc, "ProductCode", line, "111");
        criarElemento(doc, "ProductDescription", line, "Arroz");
        criarElemento(doc, "Quantity", line, "2");
        criarElemento(doc, "UnitOfMeasure", line, "UN");
        criarElemento(doc, "UnitPrice", line, "123.45");
        criarElemento(doc, "TaxPointDate", line, "2012-12-13");
        criarElemento(doc, "Description", line, "Arroz");

        Element productSerialNumber = doc.createElement("ProductSerialNumber");
        line.appendChild(productSerialNumber);
        criarElemento(doc, "SerialNumber", productSerialNumber, "ISS");

        criarElemento(doc, "CreditAmount", line, "123.45");

        Element tax = doc.createElement("Tax");
        line.appendChild(tax);
        criarElemento(doc, "TaxType", tax, "IVA");
        criarElemento(doc, "TaxCountryRegion", tax, "AO");
        criarElemento(doc, "TaxCode", tax, "NOR");
        criarElemento(doc, "TaxPercentage", tax, "0");
        criarElemento(doc, "TaxAmount", tax, "123.45");

        criarElemento(doc, "TaxExemptionReason", line, "Regime simplificado");
        criarElemento(doc, "TaxExemptionCode", line, "M00");
        criarElemento(doc, "SettlementAmount", line, "123.45");

        Element documentTotals = doc.createElement("DocumentTotals");
        invoice.appendChild(documentTotals);
        criarElemento(doc, "TaxPayable", documentTotals, "123.45");
        criarElemento(doc, "NetTotal", documentTotals, "123.45");
        criarElemento(doc, "GrossTotal", documentTotals, "123.45");

        Element currency = doc.createElement("Currency");
        documentTotals.appendChild(currency);
        criarElemento(doc, "CurrencyCode", currency, "AOA");
        criarElemento(doc, "CurrencyAmount", currency, "123.45");
        criarElemento(doc, "ExchangeRate", currency, "123.45");

        Element paymentMethod = doc.createElement("PaymentMethod");
        documentTotals.appendChild(paymentMethod);
        criarElemento(doc, "PaymentMechanism", paymentMethod, "CC");
        criarElemento(doc, "PaymentAmount", paymentMethod, "123.45");
        criarElemento(doc, "PaymentDate", paymentMethod, "2012-12-13");

        Element withholdingTax = doc.createElement("WithholdingTax");
        invoice.appendChild(withholdingTax);
        criarElemento(doc, "WithholdingTaxAmount", withholdingTax, "0");

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(Common.getAppPath("SAFT-AO") + "SAFTAO1.01_01.xml");
        transformer.transform(new DOMSource(doc), result);

        addFileContentProvider(context, "/SAFT-AO/" + "SAFTAO1.01_01.xml");

        if (validateXMLSchema(getPathXSDCacheFile(context).getAbsolutePath(), Common.getAppPath("SAFT-AO") + "SAFTAO1.01_01.xml")) {
            Toast.makeText(context, "Válido", Toast.LENGTH_LONG).show();
        }
    }
}

package com.yoga.mborasystem.view;

import static android.content.Context.VIBRATOR_SERVICE;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static com.yoga.mborasystem.util.Ultilitario.addFileContentProvider;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentLoginBinding;
import com.yoga.mborasystem.util.Common;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.LoginViewModel;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

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

public class LoginFragment extends Fragment {

    private Bundle bundle;
    private Handler handler;
    private List<String> digitos;
    private ObjectAnimator animation;
    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;
    private ClienteViewModel clienteViewModel;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private void instanceOfBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(requireActivity(),
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(requireContext(),
                        getString(R.string.err_aut) + ": " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                clienteViewModel.logarComBiometria();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), getString(R.string.fal_aut),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.titlte_biometricprompt))
                .setSubtitle(getString(R.string.subtitlte_biometricprompt))
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }

    public static boolean validateXMLSchema(String xsdPath, String xmlPath, Context context) {

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (IOException | SAXException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = new Bundle();
        digitos = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        clienteViewModel = new ViewModelProvider(this).get(ClienteViewModel.class);

        final Observer<String> infoPinObserver = s -> {
            if (s.equals("4")) {
                desabilitarTecladoPersonalisado();
                contarTempoDeEspera();
            } else {
                binding.tvinfoCodigoPin.setText(s);
                limparCodigoPin();
                vibrarTelefone(getContext());
            }
        };
        //  observador está vinculado ao objeto Lifecycle associado ao proprietário
        loginViewModel.getinfoPin().observe(this, infoPinObserver);
    }

//    @SuppressLint("MissingPermission")
//    public Task<Location> getLastLocationIfApiAvailable(FragmentActivity context) {
//        FusedLocationProviderClient client =
//                getFusedLocationProviderClient(context);
//
//        return GoogleApiAvailability.getInstance()
//                .checkApiAvailability(client)
//                .onSuccessTask(unused -> client.getLastLocation().addOnSuccessListener(context, location -> {
//                    if (location != null) {
//                        Log.d("localização", ""+location.getLatitude()+" - "+location.getLongitude());
//                        try {
//                            Geocoder geo = new Geocoder(getActivity(), Locale.getDefault());
//                            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                            if (addresses.isEmpty()) {
//                                Log.d("localização", "Endereço vazio");
//                            } else {
//                                if (addresses.size() > 0) {
//                                    Log.d("localização",addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
//                                }
//                            }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace(); // getFromLocation() may sometimes fail
//                            Log.d("localização", "Erro: " + e.getMessage());
//                        }
//                    }
//                })).addOnFailureListener(e -> Log.d("localização", "Location unavailable."));
//    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)){
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }else{
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }
//        }
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        instanceOfBiometricPrompt();
        binding.btn1.setOnClickListener(v -> digitarCodigoPin(1));
        binding.btn2.setOnClickListener(v -> digitarCodigoPin(2));
        binding.btn3.setOnClickListener(v -> digitarCodigoPin(3));
        binding.btn4.setOnClickListener(v -> digitarCodigoPin(4));
        binding.btn5.setOnClickListener(v -> digitarCodigoPin(5));
        binding.btn6.setOnClickListener(v -> digitarCodigoPin(6));
        binding.btn7.setOnClickListener(v -> digitarCodigoPin(7));
        binding.btn8.setOnClickListener(v -> digitarCodigoPin(8));
        binding.btn9.setOnClickListener(v -> digitarCodigoPin(9));
        binding.btn0.setOnClickListener(v -> digitarCodigoPin(0));
        binding.btnApagar.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_dialogCodigoPin);
        });
        binding.btnMenu.setOnClickListener(v -> {

            try {

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

                criarElemento(doc, "AuditFileVersion", header, "2.02_02");
                criarElemento(doc, "CompanyID", header, "1536728967LA98");
                criarElemento(doc, "TaxRegistrationNumber", header, "1536728967LA98");
                criarElemento(doc, "TaxAccountingBasis", header, "F");
                criarElemento(doc, "CompanyName", header, "YOGA");

                Element companyAddress = doc.createElement("CompanyAddress");
                header.appendChild(companyAddress);
                criarElemento(doc, "AddressDetail", companyAddress, "Benfica");
                criarElemento(doc, "City", companyAddress, "Luanda");
                criarElemento(doc, "Province", companyAddress, "Luanda");
                criarElemento(doc, "Country", companyAddress, "AO");

                criarElemento(doc, "FiscalYear", header, "2022");
                criarElemento(doc, "StartDate", header, "2012-12-13");
                criarElemento(doc, "EndDate", header, "2012-12-13");
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

                addFileContentProvider(getContext(), "/SAFT-AO/" + "SAFTAO1.01_01.xml");

                if (validateXMLSchema(getXSDORXMLCacheFile(requireContext(), "SAFTAO1.01_01.xsd").getAbsolutePath(), Common.getAppPath("SAFT-AO") + "SAFTAO1.01_01.xml", requireContext())) {
                    Toast.makeText(requireContext(), "Válido", Toast.LENGTH_SHORT).show();
                }

            } catch (ParserConfigurationException | TransformerException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
//            MainActivity.getProgressBar();
//            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_dialogCodigoPin);
        });

        clienteViewModel.getClienteMutableLiveData().observe(getViewLifecycleOwner(), cliente -> {
            bundle.putString("nome", cliente.get(0).getNome() + " " + cliente.get(0).getSobrenome());
            bundle.putBoolean("master", cliente.get(0).isMaster());
            bundle.putParcelable("cliente", cliente.get(0));
            try {
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation, bundle);
            } catch (Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            bundle.putString("nome", usuario.getNome());
            bundle.putBoolean("master", false);
            bundle.putLong("idusuario", usuario.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation, bundle);
        });

        binding.btnAuthBiometric.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        if (!Ultilitario.getActivarAutenticacaoBiometrica(requireContext())) {
            binding.btnAuthBiometric.setVisibility(View.INVISIBLE);
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

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

    public static File getXSDORXMLCacheFile(Context context, String filePath) {
        File cacheFile = new File(context.getCacheDir(), filePath);
        try {
            InputStream inputStream = context.getAssets().open(filePath);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return cacheFile;
    }

    private void digitarCodigoPin(Integer digito) {

        digitos.add(String.valueOf(digito));

        switch (digitos.size()) {

            case 1:
                animarBolinhas(binding.d1);
                binding.d1.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 2:
                animarBolinhas(binding.d2);
                binding.d2.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 3:
                animarBolinhas(binding.d3);
                binding.d3.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 4:
                animarBolinhas(binding.d4);
                binding.d4.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 5:
                animarBolinhas(binding.d5);
                binding.d5.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 6:
                animarBolinhas(binding.d6);
                binding.d6.setTextColor(Color.rgb(111, 55, 0));
                break;
            default:
                break;
        }

        if (digitos.size() == 6) {
            try {
                logarComTecladoPersonalizado();
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void desabilitarTecladoPersonalisado() {
        binding.gridLayout.setVisibility(View.GONE);
    }

    private void habilitarTecladoPersonalisado() {
        handler.postDelayed(() -> {
            binding.tvinfoCodigoPin.setText(R.string.tvIntroduzirCodigoPin);
            binding.gridLayout.setVisibility(View.VISIBLE);
            limparCodigoPin();
        }, 60000);
    }

    private void contarTempoDeEspera() {
        binding.tvinfoCodigoPin.setText(R.string.tentar_novamente);
        habilitarTecladoPersonalisado();
    }

    private void logarComTecladoPersonalizado() throws NoSuchAlgorithmException {
        StringBuilder codigoPin = new StringBuilder();
        for (String pin : digitos) {
            codigoPin.append(pin);
        }
        if (codigoPin.length() != 6) {
            binding.tvinfoCodigoPin.setError(getString(R.string.infoPinIncorreto));
        } else {
            MainActivity.getProgressBar();
            loginViewModel.logar(codigoPin.toString());
        }
    }

    private void animarBolinhas(TextView bolinha) {
        animation = ObjectAnimator.ofFloat(bolinha, "translationY", -10f);
        animation.setDuration(200);
        animation.start();
    }

    private void limparCodigoPin() {

        desabilitarHabilitarButton(false);
        digitos.clear();

        retirarBolinhasDeDigitos(binding.d6, 250);
        retirarBolinhasDeDigitos(binding.d5, 350);
        retirarBolinhasDeDigitos(binding.d4, 450);
        retirarBolinhasDeDigitos(binding.d3, 550);
        retirarBolinhasDeDigitos(binding.d2, 650);
        retirarBolinhasDeDigitos(binding.d1, 750);

        handler.postDelayed(() -> desabilitarHabilitarButton(true), 850);
    }

    private void retirarBolinhasDeDigitos(TextView bolinha, int tempo) {
        handler.postDelayed(() -> {
            bolinha.setTextColor(Color.GRAY);
            animation = ObjectAnimator.ofFloat(bolinha, "translationY", 0f);
            animation.setDuration(200);
            animation.start();
        }, tempo);

    }

    private void desabilitarHabilitarButton(Boolean estado) {
        binding.btn1.setEnabled(estado);
        binding.btn2.setEnabled(estado);
        binding.btn3.setEnabled(estado);
        binding.btn4.setEnabled(estado);
        binding.btn5.setEnabled(estado);
        binding.btn6.setEnabled(estado);
        binding.btn7.setEnabled(estado);
        binding.btn8.setEnabled(estado);
        binding.btn9.setEnabled(estado);
        binding.btn0.setEnabled(estado);
        binding.btnApagar.setEnabled(estado);
    }

    @SuppressLint("MissingPermission")
    public void vibrarTelefone(Context context) {

        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(500);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//        switch (requestCode){
//            case 1: {
//                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    if (ContextCompat.checkSelfPermission(getActivity(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
//                        getLastLocationIfApiAvailable(requireActivity());
//
//                    }
//                }else{
//                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }

}

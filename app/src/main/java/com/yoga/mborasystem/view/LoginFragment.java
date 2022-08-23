package com.yoga.mborasystem.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentLoginBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.LoginViewModel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import static android.content.Context.VIBRATOR_SERVICE;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import org.apache.xerces.xs.XSModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;

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
    ByteArrayOutputStream outputStream;

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
                File inputFile = new File(getXSDORXMLCacheFile(requireContext(), "SAFTAO1.01_01.xml").getAbsolutePath());
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(inputFile);
                doc.normalize();

                Node masterFiles = doc.getElementsByTagName("MasterFiles").item(0);
                Node header = doc.getElementsByTagName("Header").item(0);
                Node companyAddress = doc.getElementsByTagName("CompanyAddress").item(0);

                NodeList list = header.getChildNodes();
                NodeList listAddress = companyAddress.getChildNodes();


                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) node;
                        if ("AuditFileVersion".equals(eElement.getNodeName()))
                            eElement.setTextContent("2.02_02");
                        else if ("CompanyID".equals(eElement.getNodeName()))
                            eElement.setTextContent("5000999784");
                        else if ("TaxRegistrationNumber".equals(eElement.getNodeName()))
                            eElement.setTextContent("5000999784");
                        else if ("TaxAccountingBasis".equals(eElement.getNodeName()))
                            eElement.setTextContent("F");
                        else if ("CompanyName".equals(eElement.getNodeName()))
                            eElement.setTextContent("YOGA - COMÉRCIO, SERVIÇOS E TECNOLOGIA");
                        else if ("CompanyAddress".equals(eElement.getNodeName())) {
                            for (int i1 = 0; i1 < listAddress.getLength(); i1++) {
                                Node n1 = listAddress.item(i1);
                                if (n1.getNodeType() == Node.ELEMENT_NODE) {
                                    Element e1 = (Element) n1;
                                    if ("AddressDetail".equals(e1.getNodeName()))
                                        e1.setTextContent("Morro Bento ||");
                                    else if ("City".equals(e1.getNodeName()))
                                        e1.setTextContent("Luanda");
                                    else if ("Province".equals(e1.getNodeName()))
                                        e1.setTextContent("Luanda");
                                    else if ("Country".equals(e1.getNodeName()))
                                        e1.setTextContent("AO");
                                }
                            }
                        } else if ("FiscalYear".equals(eElement.getNodeName()))
                            eElement.setTextContent("2023");
                        else if ("StartDate".equals(eElement.getNodeName()))
                            eElement.setTextContent("2022-12-13");
                        else if ("EndDate".equals(eElement.getNodeName()))
                            eElement.setTextContent("2023-12-13");
                        else if ("CurrencyCode".equals(eElement.getNodeName()))
                            eElement.setTextContent("AOA");
                        else if ("DateCreated".equals(eElement.getNodeName()))
                            eElement.setTextContent("2022-12-13");
                        else if ("TaxEntity".equals(eElement.getNodeName()))
                            eElement.setTextContent("Global");
                        else if ("ProductCompanyTaxID".equals(eElement.getNodeName()))
                            eElement.setTextContent("AO5417432466");
                        else if ("SoftwareValidationNumber".equals(eElement.getNodeName()))
                            eElement.setTextContent("308/AGT/2021");
                        else if ("ProductID".equals(eElement.getNodeName()))
                            eElement.setTextContent("MBORASYSTEM/YOGA ANGOLA,LDA");
                        else if ("ProductVersion".equals(eElement.getNodeName()))
                            eElement.setTextContent("1");
                        else if ("Telephone".equals(eElement.getNodeName()))
                            eElement.setTextContent("932359808");
                        else if ("Fax".equals(eElement.getNodeName()))
                            eElement.setTextContent("yoga@hotmail.com");
                        else if ("Email".equals(eElement.getNodeName()))
                            eElement.setTextContent("yoga@hotmail.com");
                        else if ("Website".equals(eElement.getNodeName()))
                            eElement.setTextContent("www.yoga.com.ao");
                    }
                }

                for (int c = 0; c <= 1; c++) {
                    Element customer = doc.createElement("Customer");
                    customer.appendChild(doc.createElement("CustomerID")).setTextContent("1200 - " + c);
                    customer.appendChild(doc.createElement("AccountID")).setTextContent("Desconhecido - " + c);
                    customer.appendChild(doc.createElement("CustomerTaxID")).setTextContent("1236985LA44 - " + c);
                    customer.appendChild(doc.createElement("CompanyName")).setTextContent("Consumidor Final - " + c);
                    masterFiles.appendChild(customer);

                    Element billingAddress = doc.createElement("BillingAddress");
                    billingAddress.appendChild(doc.createElement("AddressDetail")).setTextContent("Luanda, benfica - " + c);
                    billingAddress.appendChild(doc.createElement("City")).setTextContent("Luanda - " + c);
                    billingAddress.appendChild(doc.createElement("Province")).setTextContent("Luanda - " + c);
                    billingAddress.appendChild(doc.createElement("Country")).setTextContent("AO");
                    customer.appendChild(billingAddress);

                    Element shipToAddress = doc.createElement("ShipToAddress");
                    shipToAddress.appendChild(doc.createElement("AddressDetail")).setTextContent("Luanda, benfica - " + c);
                    shipToAddress.appendChild(doc.createElement("City")).setTextContent("Luanda - " + c);
                    shipToAddress.appendChild(doc.createElement("Province")).setTextContent("Luanda - " + c);
                    shipToAddress.appendChild(doc.createElement("Country")).setTextContent("AO");
                    customer.appendChild(shipToAddress);

                    customer.appendChild(doc.createElement("Telephone")).setTextContent("96325841" + c);
                    customer.appendChild(doc.createElement("Fax")).setTextContent("mauropedro@gmail.com");
                    customer.appendChild(doc.createElement("Email")).setTextContent("mauropedro@gmail.com");
                    customer.appendChild(doc.createElement("Website")).setTextContent("www.pedro.com");
                }

                for (int c = 0; c <= 1; c++) {
                    Element product = doc.createElement("Product");
                    product.appendChild(doc.createElement("ProductType")).setTextContent("P");
                    product.appendChild(doc.createElement("ProductCode")).setTextContent("147858");
                    product.appendChild(doc.createElement("ProductDescription")).setTextContent("bombom");
                    product.appendChild(doc.createElement("ProductNumberCode")).setTextContent("1257885");
                    masterFiles.appendChild(product);
                }

                Element taxTable = doc.createElement("TaxTable");
                masterFiles.appendChild(taxTable);
                for (int c = 0; c <= 1; c++) {
                    Element taxTableEntry = doc.createElement("TaxTableEntry");
                    taxTableEntry.appendChild(doc.createElement("TaxType")).setTextContent("IVA");
                    taxTableEntry.appendChild(doc.createElement("TaxCountryRegion")).setTextContent("AO");
                    taxTableEntry.appendChild(doc.createElement("TaxCode")).setTextContent("NOR");
                    taxTableEntry.appendChild(doc.createElement("Description")).setTextContent("desconhecido");
                    taxTableEntry.appendChild(doc.createElement("TaxExpirationDate")).setTextContent("1989-12-26");
                    taxTableEntry.appendChild(doc.createElement("TaxPercentage")).setTextContent("70.23");
                    taxTableEntry.appendChild(doc.createElement("TaxAmount")).setTextContent("737.258");
                    taxTable.appendChild(taxTableEntry);
                }


                outputStream = new ByteArrayOutputStream();

                // write the content on console
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                System.out.println("-----------Modified File-----------");
                StreamResult consoleResult = new StreamResult(outputStream);
                StreamResult result = new StreamResult(System.out);
                transformer.transform(source, consoleResult);
                transformer.transform(source, result);

//                InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

                Log.i("SAF-T", outputStream.toString("UTF-8"));

            } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


//            if (validateXMLSchema(getXSDCacheFile(requireContext(), "SAFTAO1.01_01.xsd").getAbsolutePath(), getXSDCacheFile(requireContext(), "SAFTAO1.01_01.xml").getAbsolutePath(), requireContext()))
//                Toast.makeText(requireContext(), "Válido", Toast.LENGTH_SHORT).show();
//            MainActivity.getProgressBar();
//            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_dialogCodigoPin);

//            try {
//
//                // Parse the file into an XSModel object
//                XSModel xsModel = new XSParser().parse(getXSDCacheFile(requireContext(), "SAFTAO1.01_01.xsd").getAbsolutePath());
//
//                // Define defaults for the XML generation
//                XSInstance instance = new XSInstance();
//
//                // Build the sample xml doc
//                // Replace first param to XMLDoc with a file input stream to write to file
//                QName rootElement = new QName("urn:OECD:StandardAuditFile-Tax:AO_1.01_01", "AuditFile");
//                XMLDocument sampleXml = new XMLDocument(new StreamResult(System.out), false, 0, "utf-8");
//                instance.generate(xsModel, rootElement, sampleXml);
//
//            } catch (Exception e) {
//                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//            }


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

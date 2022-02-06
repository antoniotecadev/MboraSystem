package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDashboardBinding;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class DashboardFragment extends Fragment {

    private long totalVenda = 0;
    private long totalPrecoFornecedor = 0;
    private VendaViewModel vendaViewModel;
    private FragmentDashboardBinding binding;
    private ProdutoViewModel produtoViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        PieChart mPieChart = binding.start;
        BarChart mBarChart = binding.barchart;
        BarChart mBarChartD = binding.barchart2;
        BarChart mBarChartm = binding.barchartm;
        BarChart mBarChartme = binding.barchartme;

        String[] dataActual = TextUtils.split(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()), "-");

        binding.ano.setText(dataActual[2]);
        binding.vend.setText(getString(R.string.vendas) + " - " + dataActual[2]);
        binding.vendMes.setText(getString(R.string.vd_ms) + " - " + dataActual[2]);
        binding.vendDiaMes.setText(getString(R.string.vd_dr_ms) + " - " + dataActual[1]);
        binding.prodMaisVendh.setText("(3)" + getString(R.string.pd_ms_vdh));
        binding.prodMenosVendh.setText("(3)" + getString(R.string.pd_me_vdh));

        vendaViewModel.getProdutoMaisVendido(dataActual[0] + "-" + dataActual[1] + "-" + dataActual[2]).observe(getViewLifecycleOwner(), produtoVendas -> {
            if (!produtoVendas.isEmpty())
                for (ProdutoVenda pdVd : produtoVendas) {
                    produtosMaisVendidos(mBarChartm, pdVd.getNome_produto(), pdVd.getPreco_total(), pdVd.getQuantidade());
                }
        });

        vendaViewModel.getProdutoMenosVendido(dataActual[0] + "-" + dataActual[1] + "-" + dataActual[2]).observe(getViewLifecycleOwner(), produtoVendas -> {
            if (!produtoVendas.isEmpty())
                for (ProdutoVenda pdVd : produtoVendas) {
                    produtosMenosVendidos(mBarChartme, pdVd.getNome_produto(), pdVd.getPreco_total(), pdVd.getQuantidade());
                }
        });

        vendaViewModel.consultarVendas(null, 0, false, 0, false);
        vendaViewModel.getListaVendasLiveData().observe(getViewLifecycleOwner(), vendas -> {
            if (vendas.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.venda_nao_encontrada), Toast.LENGTH_LONG).show();
            } else {
                int qtv = 0;
                int janc = 0, fevc = 0, marc = 0, abrc = 0, maic = 0, junc = 0, julc = 0, agoc = 0, setc = 0, outc = 0, novc = 0, dezc = 0;
                long jan = 0, fev = 0, mar = 0, abr = 0, mai = 0, jun = 0, jul = 0, ago = 0, set = 0, out = 0, nov = 0, dez = 0;
                int v1 = 0, v2 = 0, v3 = 0, v4 = 0, v5 = 0, v6 = 0, v7 = 0, v8 = 0, v9 = 0, v10 = 0, v11 = 0, v12 = 0, v13 = 0, v14 = 0, v15 = 0, v16 = 0, v17 = 0, v18 = 0, v19 = 0, v20 = 0, v21 = 0, v22 = 0, v23 = 0, v24 = 0, v25 = 0, v26 = 0, v27 = 0, v28 = 0, v29 = 0, v30 = 0, v31 = 0;
                int cv1 = 0, cv2 = 0, cv3 = 0, cv4 = 0, cv5 = 0, cv6 = 0, cv7 = 0, cv8 = 0, cv9 = 0, cv10 = 0, cv11 = 0, cv12 = 0, cv13 = 0, cv14 = 0, cv15 = 0, cv16 = 0, cv17 = 0, cv18 = 0, cv19 = 0, cv20 = 0, cv21 = 0, cv22 = 0, cv23 = 0, cv24 = 0, cv25 = 0, cv26 = 0, cv27 = 0, cv28 = 0, cv29 = 0, cv30 = 0, cv31 = 0;

                for (Venda venda : vendas) {
                    String[] data = TextUtils.split(venda.getData_cria(), "-");
                    if (data[2].trim().equalsIgnoreCase(dataActual[2].trim())) {

                        binding.qtdVenda.setText(getString(R.string.qtd_vd) + ": " + ++qtv);

                        if (data[1].trim().equalsIgnoreCase("janeiro")) {
                            jan += venda.getTotal_venda();
                            ++janc;
                        } else if (data[1].trim().equalsIgnoreCase("fevereiro")) {
                            fev += venda.getTotal_venda();
                            ++fevc;
                        } else if (data[1].trim().equalsIgnoreCase("março")) {
                            mar += venda.getTotal_venda();
                            ++marc;
                        } else if (data[1].trim().equalsIgnoreCase("abril")) {
                            abr += venda.getTotal_venda();
                            ++abrc;
                        } else if (data[1].trim().equalsIgnoreCase("maio")) {
                            mai += venda.getTotal_venda();
                            ++maic;
                        } else if (data[1].trim().equalsIgnoreCase("junho")) {
                            jun += venda.getTotal_venda();
                            ++junc;
                        } else if (data[1].trim().equalsIgnoreCase("julho")) {
                            jul += venda.getTotal_venda();
                            ++julc;
                        } else if (data[1].trim().equalsIgnoreCase("agosto")) {
                            ago += venda.getTotal_venda();
                            ++agoc;
                        } else if (data[1].trim().equalsIgnoreCase("setembro")) {
                            set += venda.getTotal_venda();
                            ++setc;
                        } else if (data[1].trim().equalsIgnoreCase("outubro")) {
                            out += venda.getTotal_venda();
                            ++outc;
                        } else if (data[1].trim().equalsIgnoreCase("novembro")) {
                            nov += venda.getTotal_venda();
                            ++novc;
                        } else if (data[1].trim().equalsIgnoreCase("dezembro")) {
                            dez += venda.getTotal_venda();
                            ++dezc;
                        }
                    }

                    if (data[1].trim().equalsIgnoreCase(dataActual[1].trim())) {

                        if (data[0].trim().equalsIgnoreCase("01")) {
                            v1 += venda.getTotal_venda();
                            ++cv1;
                        } else if (data[0].trim().equalsIgnoreCase("02")) {
                            v2 += venda.getTotal_venda();
                            ++cv2;
                        } else if (data[0].trim().equalsIgnoreCase("03")) {
                            v3 += venda.getTotal_venda();
                            ++cv3;
                        } else if (data[0].trim().equalsIgnoreCase("04")) {
                            v4 += venda.getTotal_venda();
                            ++cv4;
                        } else if (data[0].trim().equalsIgnoreCase("05")) {
                            v5 += venda.getTotal_venda();
                            ++cv5;
                        } else if (data[0].trim().equalsIgnoreCase("06")) {
                            v6 += venda.getTotal_venda();
                            ++cv6;
                        } else if (data[0].trim().equalsIgnoreCase("07")) {
                            v7 += venda.getTotal_venda();
                            ++cv7;
                        } else if (data[0].trim().equalsIgnoreCase("08")) {
                            v8 += venda.getTotal_venda();
                            ++cv8;
                        } else if (data[0].trim().equalsIgnoreCase("09")) {
                            v9 += venda.getTotal_venda();
                            ++cv9;
                        } else if (data[0].trim().equalsIgnoreCase("10")) {
                            v10 += venda.getTotal_venda();
                            ++cv10;
                        } else if (data[0].trim().equalsIgnoreCase("11")) {
                            v11 += venda.getTotal_venda();
                            ++cv11;
                        } else if (data[0].trim().equalsIgnoreCase("12")) {
                            v12 += venda.getTotal_venda();
                            ++cv12;
                        } else if (data[0].trim().equalsIgnoreCase("13")) {
                            v13 += venda.getTotal_venda();
                            ++cv13;
                        } else if (data[0].trim().equalsIgnoreCase("14")) {
                            v14 += venda.getTotal_venda();
                            ++cv14;
                        } else if (data[0].trim().equalsIgnoreCase("15")) {
                            v15 += venda.getTotal_venda();
                            ++cv15;
                        } else if (data[0].trim().equalsIgnoreCase("16")) {
                            v16 += venda.getTotal_venda();
                            ++cv16;
                        } else if (data[0].trim().equalsIgnoreCase("17")) {
                            v17 += venda.getTotal_venda();
                            ++cv17;
                        } else if (data[0].trim().equalsIgnoreCase("18")) {
                            v18 += venda.getTotal_venda();
                            ++cv18;
                        } else if (data[0].trim().equalsIgnoreCase("19")) {
                            v19 += venda.getTotal_venda();
                            ++cv19;
                        } else if (data[0].trim().equalsIgnoreCase("20")) {
                            v20 += venda.getTotal_venda();
                            ++cv20;
                        } else if (data[0].trim().equalsIgnoreCase("21")) {
                            v21 += venda.getTotal_venda();
                            ++cv21;
                        } else if (data[0].trim().equalsIgnoreCase("22")) {
                            v22 += venda.getTotal_venda();
                            ++cv22;
                        } else if (data[0].trim().equalsIgnoreCase("23")) {
                            v23 += venda.getTotal_venda();
                            ++cv23;
                        } else if (data[0].trim().equalsIgnoreCase("24")) {
                            v24 += venda.getTotal_venda();
                            ++cv24;
                        } else if (data[0].trim().equalsIgnoreCase("25")) {
                            v25 += venda.getTotal_venda();
                            ++cv25;
                        } else if (data[0].trim().equalsIgnoreCase("26")) {
                            v26 += venda.getTotal_venda();
                            ++cv26;
                        } else if (data[0].trim().equalsIgnoreCase("27")) {
                            v27 += venda.getTotal_venda();
                            ++cv27;
                        } else if (data[0].trim().equalsIgnoreCase("28")) {
                            v28 += venda.getTotal_venda();
                            ++cv28;
                        } else if (data[0].trim().equalsIgnoreCase("29")) {
                            v29 += venda.getTotal_venda();
                            ++cv29;
                        } else if (data[0].trim().equalsIgnoreCase("30")) {
                            v30 += venda.getTotal_venda();
                            ++cv30;
                        } else if (data[0].trim().equalsIgnoreCase("31")) {
                            v31 += venda.getTotal_venda();
                            ++cv31;
                        }
                    }

                }

                totalVenda = jan + fev + mar + abr + mai + jun + jul + ago + set + out + nov + dez;

                binding.valTotVd.setText(getString(R.string.vendas) + ": " + Ultilitario.formatPreco(String.valueOf(totalVenda)));

                vendasMensais(mBarChart, jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez, janc, fevc, marc, abrc, maic, junc, julc, agoc, setc, outc, novc, dezc);
                vendasDiariasMensais(mBarChartD, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31
                        , cv1, cv2, cv3, cv4, cv5, cv6, cv7, cv8, cv9, cv10, cv11, cv12, cv13, cv14, cv15, cv16, cv17, cv18, cv19, cv20, cv21, cv22, cv23, cv24, cv25, cv26, cv27, cv28, cv29, cv30, cv31);
            }
        });

        produtoViewModel.consultarProdutos().observe(getViewLifecycleOwner(), quantProd -> binding.qtdProd.setText(getString(R.string.qtd_pd) + ": " + quantProd));

        produtoViewModel.getPrecoFornecedor().observe(getViewLifecycleOwner(), custos -> {
            totalPrecoFornecedor = custos == null ? 0 : custos;
            binding.valCusto.setText(getString(R.string.cst) + ": " + Ultilitario.formatPreco(String.valueOf(totalPrecoFornecedor)));
            mPieChart.addPieSlice(new PieModel(getString(R.string.cst), (totalPrecoFornecedor / 100), Color.parseColor("#EC7063")));

            if (totalVenda > totalPrecoFornecedor) {
                binding.valLucro.setText(getString(R.string.lc) + ": " + Ultilitario.formatPreco(String.valueOf(totalVenda - totalPrecoFornecedor)));
                mPieChart.addPieSlice(new PieModel(getString(R.string.lc), (totalVenda - totalPrecoFornecedor) / 100, Color.parseColor("#58D68D")));
            } else {
                binding.valLucro.setText(getString(R.string.lc) + ": " + (totalVenda - totalPrecoFornecedor) / 100 + " " + getString(R.string.lucro_negativo));
            }
        });

        mPieChart.startAnimation();
        mBarChart.startAnimation();
        mBarChartD.startAnimation();
        mBarChartm.startAnimation();
        mBarChartme.startAnimation();

        return binding.getRoot();
    }

    private void produtosMaisVendidos(BarChart mBarChartm, String produto, int preco, int quant) {
        mBarChartm.addBar(new BarModel(produto + " " + (preco / 100), quant, 0xFF4554E6));
    }

    private void produtosMenosVendidos(BarChart mBarChartm, String produto, int preco, int quant) {
        mBarChartm.addBar(new BarModel(produto + " " + (preco / 100), quant, 0xFF4554E6));
    }

    private void vendasMensais(BarChart mBarChart, long jan, long fev, long mar, long abr, long mai, long jun, long jul, long ago, long set, long out, long nov, long dez
            , int janc, int fevc, int marc, int abrc, int maic, int junc, int julc, int agoc, int setc, int outc, int novc, int dezc) {
        mBarChart.addBar(new BarModel("Jan" + " (" + janc + ")", (jan / 100), 0xFF123456));
        mBarChart.addBar(new BarModel("Fev" + " (" + fevc + ")", (fev / 100), 0xFF996324));
        mBarChart.addBar(new BarModel("Mar" + " (" + marc + ")", (mar / 100), 0xFF456328));
        mBarChart.addBar(new BarModel("Abr" + " (" + abrc + ")", (abr / 100), 0xFF873F56));
        mBarChart.addBar(new BarModel("Mai" + " (" + maic + ")", (mai / 100), 0xFF56B7F1));
        mBarChart.addBar(new BarModel("Jun" + " (" + junc + ")", (jun / 100), 0xFF343456));
        mBarChart.addBar(new BarModel("Jul" + " (" + julc + ")", (jul / 100), 0xFF1FF4AC));
        mBarChart.addBar(new BarModel("Ago" + " (" + agoc + ")", (ago / 100), 0xFF4554E6));
        mBarChart.addBar(new BarModel("Set" + " (" + setc + ")", (set / 100), 0xFF2BA4E8));
        mBarChart.addBar(new BarModel("Out" + " (" + outc + ")", (out / 100), 0xFF1BA402));
        mBarChart.addBar(new BarModel("Nov" + " (" + novc + ")", (nov / 100), 0xFF1B69E6));
        mBarChart.addBar(new BarModel("Dez" + " (" + dezc + ")", (dez / 100), 0xFF147856));
    }

    private void vendasDiariasMensais(BarChart mBarChart, int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9, int v10, int v11, int v12, int v13, int v14, int v15, int v16, int v17, int v18, int v19, int v20, int v21, int v22, int v23, int v24, int v25, int v26, int v27, int v28, int v29, int v30, int v31
            , int cv1, int cv2, int cv3, int cv4, int cv5, int cv6, int cv7, int cv8, int cv9, int cv10, int cv11, int cv12, int cv13, int cv14, int cv15, int cv16, int cv17, int cv18, int cv19, int cv20, int cv21, int cv22, int cv23, int cv24, int cv25, int cv26, int cv27, int cv28, int cv29, int cv30, int cv31) {
        mBarChart.addBar(new BarModel("1 (" + cv1 + ")", (v1 / 100), 0xFF123456));
        mBarChart.addBar(new BarModel("2 (" + cv2 + ")", (v2 / 100), 0xFF369874));
        mBarChart.addBar(new BarModel("3 (" + cv3 + ")", (v3 / 100), 0xFF147865));
        mBarChart.addBar(new BarModel("4 (" + cv4 + ")", (v4 / 100), 0xFF361235));
        mBarChart.addBar(new BarModel("5 (" + cv5 + ")", (v5 / 100), 0xFF325897));
        mBarChart.addBar(new BarModel("6 (" + cv6 + ")", (v6 / 100), 0xFF314789));
        mBarChart.addBar(new BarModel("7 (" + cv7 + ")", (v7 / 100), 0xFF965478));
        mBarChart.addBar(new BarModel("8 (" + cv8 + ")", (v8 / 100), 0xFF987968));
        mBarChart.addBar(new BarModel("9 (" + cv9 + ")", (v9 / 100), 0xFF657898));
        mBarChart.addBar(new BarModel("10 (" + cv10 + ")", (v10 / 100), 0xFF145698));
        mBarChart.addBar(new BarModel("11 (" + cv11 + ")", (v11 / 100), 0xFF548786));
        mBarChart.addBar(new BarModel("12 (" + cv12 + ")", (v12 / 100), 0xFF441111));
        mBarChart.addBar(new BarModel("13 (" + cv13 + ")", (v13 / 100), 0xFF478987));
        mBarChart.addBar(new BarModel("14 (" + cv14 + ")", (v14 / 100), 0xFF665257));
        mBarChart.addBar(new BarModel("15 (" + cv15 + ")", (v15 / 100), 0xFF369636));
        mBarChart.addBar(new BarModel("16 (" + cv16 + ")", (v16 / 100), 0xFF321232));
        mBarChart.addBar(new BarModel("17 (" + cv17 + ")", (v17 / 100), 0xFF222554));
        mBarChart.addBar(new BarModel("18 (" + cv18 + ")", (v18 / 100), 0xFF654123));
        mBarChart.addBar(new BarModel("19 (" + cv19 + ")", (v19 / 100), 0xFF111478));
        mBarChart.addBar(new BarModel("20 (" + cv20 + ")", (v20 / 100), 0xFF459878));
        mBarChart.addBar(new BarModel("21 (" + cv21 + ")", (v21 / 100), 0xFF777885));
        mBarChart.addBar(new BarModel("22 (" + cv22 + ")", (v22 / 100), 0xFF665441));
        mBarChart.addBar(new BarModel("23 (" + cv23 + ")", (v23 / 100), 0xFF365478));
        mBarChart.addBar(new BarModel("24 (" + cv24 + ")", (v24 / 100), 0xFF365417));
        mBarChart.addBar(new BarModel("25 (" + cv25 + ")", (v25 / 100), 0xFF321111));
        mBarChart.addBar(new BarModel("26 (" + cv26 + ")", (v26 / 100), 0xFF694258));
        mBarChart.addBar(new BarModel("27 (" + cv27 + ")", (v27 / 100), 0xFF784563));
        mBarChart.addBar(new BarModel("28 (" + cv28 + ")", (v28 / 100), 0xFF314789));
        mBarChart.addBar(new BarModel("29 (" + cv29 + ")", (v29 / 100), 0xFF665478));
        mBarChart.addBar(new BarModel("30 (" + cv30 + ")", (v30 / 100), 0xFF996547));
        mBarChart.addBar(new BarModel("31 (" + cv31 + ")", (v31 / 100), 0xFF258796));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

}
package com.brianna.meditrack.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brianna.meditrack.data.model.DoseLog;
import com.brianna.meditrack.databinding.FragmentHistoryBinding;
import com.brianna.meditrack.ui.detail.DoseLogAdapter;
import com.brianna.meditrack.util.DateUtils;
import com.brianna.meditrack.viewmodel.MedicationViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private MedicationViewModel viewModel;
    private DoseLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        setupRecyclerView();
        setupChart();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new DoseLogAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void setupChart() {
        BarChart chart = binding.barChart;
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#44474F"));

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setTextColor(Color.parseColor("#44474F"));
    }

    private void observeData() {
        String startKey = DateUtils.offsetDayKey(-6);
        String endKey   = DateUtils.todayKey();

        viewModel.getLogsForDateRange(startKey, endKey).observe(getViewLifecycleOwner(), logs -> {
            if (logs == null || logs.isEmpty()) {
                binding.tvNoHistory.setVisibility(View.VISIBLE);
                binding.rvHistory.setVisibility(View.GONE);
                return;
            }

            binding.tvNoHistory.setVisibility(View.GONE);
            binding.rvHistory.setVisibility(View.VISIBLE);

            // Show recent logs in list
            List<DoseLog> recent = logs.size() > 20 ? logs.subList(0, 20) : logs;
            adapter.submitList(recent);

            // Build chart data from last 7 days
            buildChart(logs, startKey);
        });
    }

    private void buildChart(List<DoseLog> logs, String startKey) {
        // Group logs by dateKey
        Map<String, int[]> dayMap = new TreeMap<>(); // dateKey -> [taken, total]
        for (int i = 0; i < 7; i++) {
            String key = DateUtils.offsetDayKey(-(6 - i));
            dayMap.put(key, new int[]{0, 0});
        }

        for (DoseLog log : logs) {
            if (dayMap.containsKey(log.getDateKey())) {
                int[] counts = dayMap.get(log.getDateKey());
                counts[1]++;
                if (DoseLog.STATUS_TAKEN.equals(log.getStatus())) {
                    counts[0]++;
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, int[]> entry : dayMap.entrySet()) {
            int[] counts = entry.getValue();
            float percent = counts[1] > 0 ? (counts[0] / (float) counts[1]) * 100f : 0f;
            entries.add(new BarEntry(index, percent));
            labels.add(DateUtils.formatDayLabel(entry.getKey()));
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Compliance %");
        dataSet.setColor(0xFF1565C0);
        dataSet.setValueTextColor(Color.parseColor("#44474F"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChart.setData(barData);
        binding.barChart.invalidate();
        binding.barChart.animateY(800);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

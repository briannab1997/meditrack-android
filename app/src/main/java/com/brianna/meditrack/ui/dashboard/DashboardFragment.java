package com.brianna.meditrack.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brianna.meditrack.databinding.FragmentDashboardBinding;
import com.brianna.meditrack.util.DateUtils;
import com.brianna.meditrack.viewmodel.MedicationViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private MedicationViewModel viewModel;
    private TodayMedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        setupHeader();
        setupRecyclerView();
        observeData();
    }

    private void setupHeader() {
        binding.tvGreeting.setText(DateUtils.getGreeting());
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        binding.tvDate.setText(sdf.format(new Date()));
    }

    private void setupRecyclerView() {
        adapter = new TodayMedAdapter();
        binding.rvTodayMeds.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayMeds.setAdapter(adapter);

        adapter.setOnTakeDoseListener(medication -> {
            viewModel.logDoseTaken(medication, System.currentTimeMillis());
            Snackbar.make(binding.getRoot(),
                    medication.getName() + " marked as taken",
                    Snackbar.LENGTH_SHORT).show();
        });
    }

    private void observeData() {
        // Today's medications
        viewModel.getAllMedications().observe(getViewLifecycleOwner(), medications -> {
            if (medications == null || medications.isEmpty()) {
                binding.tvEmptyToday.setVisibility(View.VISIBLE);
                binding.rvTodayMeds.setVisibility(View.GONE);
                adapter.submitList(null);

                // Reset progress
                updateProgress(0, 0);
            } else {
                binding.tvEmptyToday.setVisibility(View.GONE);
                binding.rvTodayMeds.setVisibility(View.VISIBLE);
                adapter.submitList(medications);

                // Set next dose
                updateNextDose(medications);
            }
        });

        // Today's taken count
        viewModel.getTodayTakenCount().observe(getViewLifecycleOwner(), taken -> {
            Integer total = viewModel.getTodayTotalCount().getValue();
            updateProgress(taken != null ? taken : 0, total != null ? total : 0);
        });

        viewModel.getTodayTotalCount().observe(getViewLifecycleOwner(), total -> {
            Integer taken = viewModel.getTodayTakenCount().getValue();
            updateProgress(taken != null ? taken : 0, total != null ? total : 0);
        });

        // Streak
        viewModel.getStreak().observe(getViewLifecycleOwner(), streak -> {
            if (streak == null || streak == 0) {
                binding.chipStreak.setText("Start your streak!");
            } else if (streak == 1) {
                binding.chipStreak.setText("1 day streak");
            } else {
                binding.chipStreak.setText(streak + " day streak");
            }
        });
    }

    private void updateProgress(int taken, int total) {
        if (total == 0) {
            binding.tvDosesTaken.setText("No doses scheduled");
            binding.progressIndicator.setProgress(0);
            binding.tvProgressPercent.setText("--");
        } else {
            binding.tvDosesTaken.setText(taken + " of " + total + " taken");
            int percent = (int) ((taken / (float) total) * 100);
            binding.progressIndicator.setProgressCompat(percent, true);
            binding.tvProgressPercent.setText(percent + "%");
        }
    }

    private void updateNextDose(List medications) {
        if (medications == null || medications.isEmpty()) {
            binding.tvNextDose.setText("No upcoming doses");
            binding.tvNextDoseTime.setText("");
            return;
        }
        // Show the first medication's first scheduled time as next dose
        com.brianna.meditrack.data.model.Medication first =
                (com.brianna.meditrack.data.model.Medication) medications.get(0);
        String[] times = first.getTimeArray();
        if (times.length > 0) {
            binding.tvNextDose.setText(first.getName() + " - " + first.getDosage());
            binding.tvNextDoseTime.setText(DateUtils.formatTimeFromString(times[0]));
        } else {
            binding.tvNextDose.setText(first.getName());
            binding.tvNextDoseTime.setText(first.getDisplayFrequency());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

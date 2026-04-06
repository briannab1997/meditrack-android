package com.brianna.meditrack.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brianna.meditrack.R;
import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.databinding.FragmentMedicationDetailBinding;
import com.brianna.meditrack.ui.addedit.AddEditMedicationFragmentArgs;
import com.brianna.meditrack.util.DateUtils;
import com.brianna.meditrack.viewmodel.MedicationViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MedicationDetailFragment extends Fragment {

    private FragmentMedicationDetailBinding binding;
    private MedicationViewModel viewModel;
    private long medicationId;
    private Medication currentMedication;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicationDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        if (getArguments() != null) {
            medicationId = MedicationDetailFragmentArgs.fromBundle(getArguments()).getMedicationId();
        }

        setupRecyclerView();
        setupButtons();
        observeData();
    }

    private void setupRecyclerView() {
        DoseLogAdapter adapter = new DoseLogAdapter();
        binding.rvDoseHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDoseHistory.setAdapter(adapter);

        viewModel.getRecentLogsForMedication(medicationId).observe(getViewLifecycleOwner(), logs -> {
            adapter.submitList(logs);
            boolean empty = logs == null || logs.isEmpty();
            binding.tvNoHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvDoseHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());

        binding.btnEdit.setOnClickListener(v -> {
            // Pass medicationId via the destination's args builder
            Bundle args = new AddEditMedicationFragmentArgs.Builder()
                    .setMedicationId((int) medicationId)
                    .build().toBundle();
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_detail_to_edit, args);
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (currentMedication == null) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Medication")
                    .setMessage("Are you sure you want to delete " + currentMedication.getName() + "?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteMedication(currentMedication);
                        Navigation.findNavController(binding.getRoot()).popBackStack();
                    })
                    .show();
        });
    }

    private void observeData() {
        viewModel.getMedicationById(medicationId).observe(getViewLifecycleOwner(), medication -> {
            if (medication == null) return;
            currentMedication = medication;
            bindMedication(medication);
        });

        viewModel.getMedicationStats(medicationId, stats -> {
            if (stats == null || !isAdded()) return;
            int taken = stats[0];
            int total = stats[1];
            int percent = total > 0 ? (int) ((taken / (float) total) * 100) : 0;

            requireActivity().runOnUiThread(() -> {
                binding.tvStatCompliance.setText(percent + "%");
                binding.tvStatTotal.setText(String.valueOf(taken));
            });
        });
    }

    private void bindMedication(Medication medication) {
        binding.tvDetailName.setText(medication.getName());
        binding.tvDetailDosageFreq.setText(
                medication.getDosage() + "  |  " + medication.getDisplayFrequency());

        // Header card color
        binding.cardHeader.setCardBackgroundColor(medication.getColorHex());

        // Pills stat
        binding.tvStatPills.setText(String.valueOf(medication.getPillsRemaining()));

        // Refill progress
        if (medication.getPillsTotal() > 0) {
            int progress = (int) ((medication.getPillsRemaining() / (float) medication.getPillsTotal()) * 100);
            binding.progressRefill.setProgress(progress);
            binding.progressRefill.setIndicatorColor(medication.getColorHex());
            binding.tvPillsStatus.setText(medication.getPillsRemaining() + " of " +
                    medication.getPillsTotal() + " pills remaining");
        }

        // Refill date
        if (medication.getRefillDate() > 0) {
            long daysUntil = DateUtils.daysUntil(medication.getRefillDate());
            if (daysUntil <= 0) {
                binding.tvRefillDate.setText("Refill overdue");
                binding.tvRefillDate.setTextColor(0xFFC62828);
            } else {
                binding.tvRefillDate.setText("Refill in " + daysUntil + " days");
            }
        } else {
            binding.tvRefillDate.setText("");
        }

        // Prescriber
        if (medication.getPrescriber() != null && !medication.getPrescriber().isEmpty()) {
            binding.tvPrescriber.setText(medication.getPrescriber());
            binding.tvPrescriberLabel.setVisibility(View.VISIBLE);
        } else {
            binding.tvPrescriberLabel.setVisibility(View.GONE);
            binding.tvPrescriber.setVisibility(View.GONE);
        }

        // Notes
        if (medication.getNotes() != null && !medication.getNotes().isEmpty()) {
            binding.tvNotes.setText(medication.getNotes());
            binding.tvNotesLabel.setVisibility(View.VISIBLE);
        } else {
            binding.tvNotesLabel.setVisibility(View.GONE);
            binding.tvNotes.setVisibility(View.GONE);
        }

        // Hide notes card if neither prescriber nor notes
        boolean hasDetails = (medication.getPrescriber() != null && !medication.getPrescriber().isEmpty())
                || (medication.getNotes() != null && !medication.getNotes().isEmpty());
        binding.cardNotes.setVisibility(hasDetails ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.brianna.meditrack.ui.addedit;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.brianna.meditrack.R;
import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.databinding.FragmentAddEditMedicationBinding;
import com.brianna.meditrack.util.DateUtils;
import com.brianna.meditrack.viewmodel.MedicationViewModel;

import java.util.Calendar;

public class AddEditMedicationFragment extends Fragment {

    private FragmentAddEditMedicationBinding binding;
    private MedicationViewModel viewModel;
    private long medicationId = -1;
    private Medication existingMedication;
    private long selectedRefillDate = 0;
    private int selectedColorHex = 0xFF42A5F5; // default blue

    private static final int[] COLORS = {
        0xFFEF5350, 0xFFFF7043, 0xFFFFA726, 0xFF66BB6A,
        0xFF26A69A, 0xFF42A5F5, 0xFF5C6BC0, 0xFFAB47BC,
        0xFFEC407A, 0xFF8D6E63
    };

    private static final String[] FREQUENCIES = {
        "Once daily", "Twice daily", "Three times daily",
        "Four times daily", "As needed", "Weekly"
    };

    private static final String[] FREQUENCY_KEYS = {
        "ONCE_DAILY", "TWICE_DAILY", "THREE_TIMES", "FOUR_TIMES", "AS_NEEDED", "WEEKLY"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditMedicationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        if (getArguments() != null) {
            medicationId = AddEditMedicationFragmentArgs.fromBundle(getArguments()).getMedicationId();
        }

        setupFrequencyDropdown();
        setupColorSwatches();
        setupDatePicker();
        setupBackButton();
        setupSaveButton();

        if (medicationId > 0) {
            binding.tvFormTitle.setText("Edit Medication");
            loadExistingMedication();
        }
    }

    private void setupFrequencyDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                FREQUENCIES);
        binding.dropdownFrequency.setAdapter(adapter);
        binding.dropdownFrequency.setText(FREQUENCIES[0], false);
    }

    private void setupColorSwatches() {
        int sizePx = (int) (44 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (8 * getResources().getDisplayMetrics().density);

        for (int color : COLORS) {
            ImageView swatch = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(0, 0, marginPx, 0);
            swatch.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            swatch.setBackground(drawable);

            if (color == selectedColorHex) {
                drawable.setStroke((int)(3 * getResources().getDisplayMetrics().density), 0xFF000000);
            }

            swatch.setOnClickListener(v -> {
                selectedColorHex = color;
                refreshSwatchBorders();
            });

            binding.llColorSwatches.addView(swatch);
        }
    }

    private void refreshSwatchBorders() {
        int borderPx = (int) (3 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < binding.llColorSwatches.getChildCount(); i++) {
            View child = binding.llColorSwatches.getChildAt(i);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(COLORS[i]);
            if (COLORS[i] == selectedColorHex) {
                d.setStroke(borderPx, 0xFF1565C0);
            }
            child.setBackground(d);
        }
    }

    private void setupDatePicker() {
        binding.etRefillDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                selectedRefillDate = selected.getTimeInMillis();
                binding.etRefillDate.setText(DateUtils.formatDisplayDate(selectedRefillDate));
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
    }

    private void setupSaveButton() {
        binding.fabSave.setOnClickListener(v -> saveMedication());
    }

    private void loadExistingMedication() {
        viewModel.getMedicationById(medicationId).observe(getViewLifecycleOwner(), medication -> {
            if (medication == null) return;
            existingMedication = medication;

            binding.etName.setText(medication.getName());
            binding.etDosage.setText(medication.getDosage());

            // Frequency
            String freq = medication.getFrequency();
            for (int i = 0; i < FREQUENCY_KEYS.length; i++) {
                if (FREQUENCY_KEYS[i].equals(freq)) {
                    binding.dropdownFrequency.setText(FREQUENCIES[i], false);
                    break;
                }
            }

            // Category chips
            switch (medication.getCategory() != null ? medication.getCategory() : "Morning") {
                case "Afternoon": binding.chipCatAfternoon.setChecked(true); break;
                case "Evening":   binding.chipCatEvening.setChecked(true);   break;
                case "As Needed": binding.chipCatAsNeeded.setChecked(true);  break;
                default:          binding.chipCatMorning.setChecked(true);   break;
            }

            selectedColorHex = medication.getColorHex();
            refreshSwatchBorders();

            if (medication.getPrescriber() != null)
                binding.etPrescriber.setText(medication.getPrescriber());

            if (medication.getPillsRemaining() > 0)
                binding.etPillsRemaining.setText(String.valueOf(medication.getPillsRemaining()));

            if (medication.getPillsTotal() > 0)
                binding.etPillsTotal.setText(String.valueOf(medication.getPillsTotal()));

            if (medication.getRefillDate() > 0) {
                selectedRefillDate = medication.getRefillDate();
                binding.etRefillDate.setText(DateUtils.formatDisplayDate(selectedRefillDate));
            }

            if (medication.getNotes() != null)
                binding.etNotes.setText(medication.getNotes());
        });
    }

    private void saveMedication() {
        String name = binding.etName.getText() != null ?
                binding.etName.getText().toString().trim() : "";
        String dosage = binding.etDosage.getText() != null ?
                binding.etDosage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.layoutName.setError("Medication name is required");
            return;
        }
        if (dosage.isEmpty()) {
            binding.layoutDosage.setError("Dosage is required");
            return;
        }

        binding.layoutName.setError(null);
        binding.layoutDosage.setError(null);

        Medication medication = existingMedication != null ? existingMedication : new Medication();
        medication.setName(name);
        medication.setDosage(dosage);
        medication.setColorHex(selectedColorHex);
        medication.setRefillDate(selectedRefillDate);

        // Frequency
        String freqText = binding.dropdownFrequency.getText().toString();
        String freqKey = "ONCE_DAILY";
        for (int i = 0; i < FREQUENCIES.length; i++) {
            if (FREQUENCIES[i].equals(freqText)) {
                freqKey = FREQUENCY_KEYS[i];
                break;
            }
        }
        medication.setFrequency(freqKey);

        // Default schedule time based on category
        String category = getSelectedCategory();
        medication.setCategory(category);
        if (medication.getScheduleTimes() == null || medication.getScheduleTimes().isEmpty()) {
            medication.setScheduleTimes(defaultTimeForCategory(category));
        }

        // Prescriber
        if (binding.etPrescriber.getText() != null)
            medication.setPrescriber(binding.etPrescriber.getText().toString().trim());

        // Pills
        try {
            String rem = binding.etPillsRemaining.getText() != null ?
                    binding.etPillsRemaining.getText().toString() : "";
            if (!rem.isEmpty()) medication.setPillsRemaining(Integer.parseInt(rem));
        } catch (NumberFormatException ignored) {}

        try {
            String tot = binding.etPillsTotal.getText() != null ?
                    binding.etPillsTotal.getText().toString() : "";
            if (!tot.isEmpty()) medication.setPillsTotal(Integer.parseInt(tot));
        } catch (NumberFormatException ignored) {}

        // Notes
        if (binding.etNotes.getText() != null)
            medication.setNotes(binding.etNotes.getText().toString().trim());

        if (existingMedication != null) {
            viewModel.updateMedication(medication);
        } else {
            viewModel.insertMedication(medication, null);
        }

        Navigation.findNavController(binding.getRoot()).popBackStack();
    }

    private String getSelectedCategory() {
        int id = binding.chipGroupCategory.getCheckedChipId();
        if (id == R.id.chip_cat_afternoon) return "Afternoon";
        if (id == R.id.chip_cat_evening)   return "Evening";
        if (id == R.id.chip_cat_as_needed) return "As Needed";
        return "Morning";
    }

    private String defaultTimeForCategory(String category) {
        switch (category) {
            case "Afternoon": return "12:00";
            case "Evening":   return "20:00";
            case "As Needed": return "";
            default:          return "08:00";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

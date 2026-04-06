package com.brianna.meditrack.ui.medications;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.brianna.meditrack.databinding.FragmentMedicationsBinding;
import com.brianna.meditrack.viewmodel.MedicationViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicationsFragment extends Fragment {

    private FragmentMedicationsBinding binding;
    private MedicationViewModel viewModel;
    private MedicationAdapter adapter;
    private List<Medication> allMedications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new MedicationAdapter();
        binding.rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedications.setAdapter(adapter);

        adapter.setOnMedicationClickListener(medication -> {
            MedicationsFragmentDirections.ActionMedicationsToDetail action =
                    MedicationsFragmentDirections.actionMedicationsToDetail(medication.getId());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String query = binding.etSearch.getText() != null ?
                    binding.etSearch.getText().toString() : "";
            filterList(query);
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            MedicationsFragmentDirections.ActionMedicationsToAddEdit action =
                    MedicationsFragmentDirections.actionMedicationsToAddEdit(-1);
            Navigation.findNavController(v).navigate(action);
        });
    }

    private void observeData() {
        viewModel.getAllMedications().observe(getViewLifecycleOwner(), medications -> {
            allMedications = medications != null ? medications : new ArrayList<>();
            String query = binding.etSearch.getText() != null ?
                    binding.etSearch.getText().toString() : "";
            filterList(query);
        });
    }

    private void filterList(String query) {
        String category = getSelectedCategory();
        List<Medication> filtered = new ArrayList<>();

        for (Medication med : allMedications) {
            boolean matchesQuery = query.isEmpty() ||
                    med.getName().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()));
            boolean matchesCategory = category == null ||
                    category.equals(med.getCategory());

            if (matchesQuery && matchesCategory) {
                filtered.add(med);
            }
        }

        adapter.submitList(filtered);
        boolean isEmpty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMedications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Nullable
    private String getSelectedCategory() {
        int checkedId = binding.chipGroupFilter.getCheckedChipId();
        if (checkedId == R.id.chip_morning)    return "Morning";
        if (checkedId == R.id.chip_afternoon)  return "Afternoon";
        if (checkedId == R.id.chip_evening)    return "Evening";
        if (checkedId == R.id.chip_as_needed)  return "As Needed";
        return null; // "All" selected
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

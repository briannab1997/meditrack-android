package com.brianna.meditrack.ui.medications;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brianna.meditrack.R;
import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.databinding.FragmentMedicationsBinding;
import com.brianna.meditrack.viewmodel.MedicationViewModel;
import com.google.android.material.snackbar.Snackbar;

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
        setupSwipeToDelete();
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

    private void setupSwipeToDelete() {
        Paint bgPaint = new Paint();
        bgPaint.setColor(0xFFEF5350);

        Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
        float cornerRadius = (int) (14 * getResources().getDisplayMetrics().density);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Medication deleted = adapter.getCurrentList().get(position);

                viewModel.deleteMedication(deleted);

                Snackbar.make(binding.getRoot(), deleted.getName() + " removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> viewModel.insertMedication(deleted, null))
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View item = viewHolder.itemView;
                int margin = (int) (12 * getResources().getDisplayMetrics().density);

                // Red background behind the card
                RectF bg = new RectF(
                        item.getLeft() + margin,
                        item.getTop() + margin,
                        item.getRight() - margin,
                        item.getBottom() - margin
                );
                c.drawRoundRect(bg, cornerRadius, cornerRadius, bgPaint);

                // Delete icon on the right
                if (deleteIcon != null) {
                    int iconMargin = (item.getHeight() - iconSize) / 2;
                    int iconTop    = item.getTop() + iconMargin;
                    int iconBottom = iconTop + iconSize;
                    int iconRight  = item.getRight() - iconMargin - margin;
                    int iconLeft   = iconRight - iconSize;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.setTint(0xFFFFFFFF);
                    deleteIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvMedications);
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
        binding.fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        MedicationsFragmentDirections.actionMedicationsToAddEdit()));
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
                    med.getName().toLowerCase(Locale.getDefault())
                            .contains(query.toLowerCase(Locale.getDefault()));
            boolean matchesCategory = category == null || category.equals(med.getCategory());

            if (matchesQuery && matchesCategory) filtered.add(med);
        }

        adapter.submitList(filtered);
        boolean isEmpty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMedications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Nullable
    private String getSelectedCategory() {
        int id = binding.chipGroupFilter.getCheckedChipId();
        if (id == R.id.chip_morning)   return "Morning";
        if (id == R.id.chip_afternoon) return "Afternoon";
        if (id == R.id.chip_evening)   return "Evening";
        if (id == R.id.chip_as_needed) return "As Needed";
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

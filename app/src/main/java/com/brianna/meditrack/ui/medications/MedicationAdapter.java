package com.brianna.meditrack.ui.medications;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.databinding.ItemMedicationBinding;
import com.brianna.meditrack.util.DateUtils;

import java.util.List;

public class MedicationAdapter extends ListAdapter<Medication, MedicationAdapter.MedViewHolder> {

    public interface OnMedicationClickListener {
        void onMedicationClicked(Medication medication);
    }

    private OnMedicationClickListener listener;

    public MedicationAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnMedicationClickListener(OnMedicationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMedicationBinding binding = ItemMedicationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MedViewHolder extends RecyclerView.ViewHolder {

        private final ItemMedicationBinding binding;

        MedViewHolder(ItemMedicationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Medication medication) {
            binding.tvName.setText(medication.getName());
            binding.tvDosage.setText(medication.getDosage() + "  |  " + medication.getDisplayFrequency());

            // Color circle
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(medication.getColorHex());
            binding.viewColorDot.setBackground(circle);

            // First letter of name in the circle would need a custom view, so we set alpha
            binding.viewColorDot.setAlpha(0.9f);

            // Pills progress
            if (medication.getPillsTotal() > 0) {
                int progress = (int) ((medication.getPillsRemaining() / (float) medication.getPillsTotal()) * 100);
                binding.progressPills.setProgress(progress);
                binding.tvPillsRemaining.setText(medication.getPillsRemaining() + " left");

                // Warn when low
                if (progress <= 25) {
                    binding.progressPills.setIndicatorColor(0xFFE53935);
                } else if (progress <= 50) {
                    binding.progressPills.setIndicatorColor(0xFFFFA000);
                } else {
                    binding.progressPills.setIndicatorColor(medication.getColorHex());
                }
            } else {
                binding.progressPills.setVisibility(View.GONE);
                binding.tvPillsRemaining.setVisibility(View.GONE);
            }

            // Next time chip
            String[] times = medication.getTimeArray();
            if (times.length > 0) {
                binding.chipNextTime.setText(DateUtils.formatTimeFromString(times[0]));
                binding.chipNextTime.setVisibility(View.VISIBLE);
            } else {
                binding.chipNextTime.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onMedicationClicked(medication);
            });
        }
    }

    private static final DiffUtil.ItemCallback<Medication> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Medication>() {
                @Override
                public boolean areItemsTheSame(@NonNull Medication a, @NonNull Medication b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Medication a, @NonNull Medication b) {
                    return a.getName().equals(b.getName())
                            && a.getDosage().equals(b.getDosage())
                            && a.getColorHex() == b.getColorHex()
                            && a.getPillsRemaining() == b.getPillsRemaining();
                }
            };
}

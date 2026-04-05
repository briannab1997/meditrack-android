package com.brianna.meditrack.ui.dashboard;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.databinding.ItemTodayMedicationBinding;
import com.brianna.meditrack.util.DateUtils;

public class TodayMedAdapter extends ListAdapter<Medication, TodayMedAdapter.TodayViewHolder> {

    public interface OnTakeDoseListener {
        void onTakeDose(Medication medication);
    }

    private OnTakeDoseListener listener;

    public TodayMedAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnTakeDoseListener(OnTakeDoseListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayMedicationBinding binding = ItemTodayMedicationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TodayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TodayViewHolder extends RecyclerView.ViewHolder {

        private final ItemTodayMedicationBinding binding;

        TodayViewHolder(ItemTodayMedicationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Medication medication) {
            binding.tvMedName.setText(medication.getName());

            String[] times = medication.getTimeArray();
            String timeText = medication.getDosage();
            if (times.length > 0) {
                timeText += "  |  " + DateUtils.formatTimeFromString(times[0]);
            }
            binding.tvDosageTime.setText(timeText);

            // Color tag
            GradientDrawable tag = new GradientDrawable();
            tag.setShape(GradientDrawable.RECTANGLE);
            tag.setCornerRadius(8f);
            tag.setColor(medication.getColorHex());
            binding.viewColorTag.setBackground(tag);

            binding.btnMarkTaken.setOnClickListener(v -> {
                if (listener != null) {
                    long scheduledTime = System.currentTimeMillis();
                    if (times.length > 0) {
                        try {
                            String[] parts = times[0].split(":");
                            scheduledTime = DateUtils.todayAtTime(
                                    Integer.parseInt(parts[0]),
                                    Integer.parseInt(parts[1]));
                        } catch (Exception ignored) {}
                    }
                    listener.onTakeDose(medication);
                }
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
                    return a.getName().equals(b.getName()) && a.getColorHex() == b.getColorHex();
                }
            };
}

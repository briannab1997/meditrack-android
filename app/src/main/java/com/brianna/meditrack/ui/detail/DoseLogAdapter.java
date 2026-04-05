package com.brianna.meditrack.ui.detail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brianna.meditrack.data.model.DoseLog;
import com.brianna.meditrack.databinding.ItemDoseLogBinding;
import com.brianna.meditrack.util.DateUtils;

public class DoseLogAdapter extends ListAdapter<DoseLog, DoseLogAdapter.LogViewHolder> {

    public DoseLogAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoseLogBinding binding = ItemDoseLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        private final ItemDoseLogBinding binding;

        LogViewHolder(ItemDoseLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DoseLog log) {
            binding.tvLogDate.setText(DateUtils.formatDisplayDate(log.getScheduledTime()));
            binding.tvLogTime.setText(
                    log.isTaken() ? "Taken at " + DateUtils.formatTime(log.getTakenTime()) :
                            "Scheduled: " + DateUtils.formatTime(log.getScheduledTime())
            );

            switch (log.getStatus()) {
                case DoseLog.STATUS_TAKEN:
                    binding.chipStatus.setText("Taken");
                    binding.chipStatus.setChipBackgroundColorResource(com.brianna.meditrack.R.color.taken_green_light);
                    binding.chipStatus.setTextColor(0xFF2E7D32);
                    binding.viewStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFF2E7D32));
                    break;
                case DoseLog.STATUS_MISSED:
                    binding.chipStatus.setText("Missed");
                    binding.chipStatus.setChipBackgroundColorResource(com.brianna.meditrack.R.color.missed_red_light);
                    binding.chipStatus.setTextColor(0xFFC62828);
                    binding.viewStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFC62828));
                    break;
                case DoseLog.STATUS_SKIPPED:
                    binding.chipStatus.setText("Skipped");
                    binding.chipStatus.setChipBackgroundColorResource(com.brianna.meditrack.R.color.upcoming_amber_light);
                    binding.chipStatus.setTextColor(0xFFE65100);
                    binding.viewStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFE65100));
                    break;
            }
        }
    }

    private static final DiffUtil.ItemCallback<DoseLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DoseLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull DoseLog a, @NonNull DoseLog b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull DoseLog a, @NonNull DoseLog b) {
                    return a.getStatus().equals(b.getStatus());
                }
            };
}

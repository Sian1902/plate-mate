package com.example.plate_mate.presentation.onboarding.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plate_mate.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = onboardingItems.get(position);
        holder.image.setImageResource(item.getImageRes());
        holder.title1.setText(item.getTitle1());
        holder.title2.setText(item.getTitle2());
        holder.description.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title1, title2, description;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.onboarding_image);
            title1 = itemView.findViewById(R.id.title_part1);
            title2 = itemView.findViewById(R.id.title_part2);
            description = itemView.findViewById(R.id.onboarding_description);
        }
    }
}

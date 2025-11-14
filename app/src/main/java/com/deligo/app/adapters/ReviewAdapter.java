package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList = new ArrayList<>();

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView userNameTextView;
        private final TextView ratingTextView;
        private final TextView commentTextView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }

        public void bind(Review review) {
            // Display user name if available, otherwise show "Anonymous"
            if (review.getUser() != null && review.getUser().getFullName() != null) {
                userNameTextView.setText(review.getUser().getFullName());
            } else {
                userNameTextView.setText("Anonymous");
            }

            ratingTextView.setText(review.getRating() + " ⭐");

            if (review.getComment() != null && !review.getComment().isEmpty()) {
                commentTextView.setText(review.getComment());
                commentTextView.setVisibility(View.VISIBLE);
            } else {
                commentTextView.setVisibility(View.GONE);
            }
        }
    }
}

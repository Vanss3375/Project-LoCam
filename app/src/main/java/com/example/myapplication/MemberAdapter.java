package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<Member> members;
    private Context context;
    public MemberAdapter(List<Member> members, Context context) {
        this.members = members;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.memberName.setText(member.getName());
        holder.memberId.setText(member.getId());
        holder.memberEmail.setText(member.getEmail()); // Set email address
        holder.memberImage.setImageResource(member.getImageResId());

        // Set click listener for the email TextView
        holder.memberEmail.setOnClickListener(v -> showEmailAppChooser(member.getEmail()));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    private void showEmailAppChooser(String email) {
        if (email != null && !email.isEmpty()) {
            Uri emailUri = Uri.parse("mailto:" + email);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, emailUri);
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject"); // Example subject

            // Create a chooser dialog to let the user select an email app
            Intent chooserIntent = Intent.createChooser(emailIntent, "Send Email");
            if (chooserIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooserIntent);
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Email address is not available", Toast.LENGTH_SHORT).show();
        }
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImage;
        TextView memberName;
        TextView memberId;
        TextView memberEmail;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage = itemView.findViewById(R.id.memberImage);
            memberName = itemView.findViewById(R.id.memberName);
            memberId = itemView.findViewById(R.id.memberId);
            memberEmail = itemView.findViewById(R.id.memberEmail);
        }
    }
}
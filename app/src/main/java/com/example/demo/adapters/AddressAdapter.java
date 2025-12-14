package com.example.demo.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.models.AddressResponse;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder>{
    private List<AddressResponse.AddressData> addressList;
    private OnAddressClickListener listener;
    public interface OnAddressClickListener{
        void onAddressClick(AddressResponse.AddressData address);
    }
    public AddressAdapter(List<AddressResponse.AddressData> addressList, OnAddressClickListener listener){
        this.addressList=addressList;
        this.listener=listener;
    }
    @NonNull
    @Override
    public  AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_card,parent,false);
        return new AddressViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressResponse.AddressData item = addressList.get(position);
        holder.tvContactInfo.setText(item.name + " | " + item.phone);
        holder.tvAddressDetail.setText(item.getFullAddress());

        if (item.isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_btn_selected);
        }
        else {
            holder.itemView.setBackgroundResource(0);
        }

        holder.itemView.setOnClickListener(v -> listener.onAddressClick(item));
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactInfo, tvAddressDetail;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactInfo = itemView.findViewById(R.id.tvContactInfo);
            tvAddressDetail = itemView.findViewById(R.id.tvAddressDetail);
            /*tvAddressTitle=itemView.findViewById(R.id.tvAddressTitle);
            ivIconType=itemView.findViewById(R.id.ivIconType);*/
        }
    }

}
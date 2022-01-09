package com.example.myguideapplication.ui.items;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.example.myguideapplication.activity.GuideActivity;
import com.example.myguideapplication.construction.Point;
import com.example.myguideapplication.databinding.FragmentItemBinding;
import com.example.myguideapplication.databinding.FragmentItemListBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myguideapplication.R;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     ItemListFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class ItemListFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private FragmentItemListBinding binding;

    // TODO: Customize parameters
    public static ItemListFragment newInstance(int itemCount) {
        final ItemListFragment fragment = new ItemListFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentItemListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ItemAdapter(getArguments().getInt(ARG_ITEM_COUNT)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;

        ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            text = binding.text;

        }

    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        ItemAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(Point.getPoints().get(position).getName());
            holder.text.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
//                    setCommodity(position);
//                    System.out.println(Point.getPoint().getName());
//                    System.out.println(getActivity());
//                    GuideActivity guideActivity=(GuideActivity)getActivity();
//                    guideActivity.setTab(1);
//                    dialog.setImageBitmap(createScaledBitmap(StringToBitmap(User.getUser().getProfile())));
                    Point.setPoint(Point.getPoints().get(position));
                    CommodityDialog dialog=new CommodityDialog(getContext());
                    dialog.setTitle(Point.getPoint().getName());
                    dialog.setMessage(Point.getPoint().getInfo()+"\n价格:"+Point.getPoint().getValue());
                    if(Point.getPoint().getImg()!=null)
                    {
                        dialog.setImage(Point.getPoint().getImg());
                    }
                    dialog.setOnClickBottomListener(
                            new CommodityDialog.OnClickBottomListener(){
                                @Override
                                public void onPositiveClick() {
                                    if(Point.getJudge()==2) {
                                        GuideActivity guideActivity = (GuideActivity) getActivity();
                                        guideActivity.onStartGuideClick();
                                    }
                                    dismiss();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegtiveClick() {
                                    dialog.dismiss();
                                }
                            });
                    dialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }


    }
    public void setCommodity(int i)
    {
        Point.setPoint(Point.getPoints().get(i));
    }
}
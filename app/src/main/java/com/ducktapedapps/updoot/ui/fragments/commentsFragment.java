package com.ducktapedapps.updoot.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.CommentsAdapter;
import com.ducktapedapps.updoot.utils.CustomItemAnimator;
import com.ducktapedapps.updoot.utils.SwipeUtils;
import com.ducktapedapps.updoot.viewModels.CommentsVM;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;

import static com.ducktapedapps.updoot.BR.linkdata;

public class commentsFragment extends Fragment {
    private static final String TAG = "commentsFragment";
    private static final String SUBMISSIONS_DATA_KEY = "submissions_data_key";
    private FragmentCommentsBinding binding;

    private SlidrConfig slidrConfig;
    private SlidrInterface slidrInterface;

    private CommentsAdapter adapter;

    static commentsFragment newInstance(LinkData data) {
        Log.i(TAG, "newInstance: " + data);
        Bundle args = new Bundle();
        args.putSerializable(SUBMISSIONS_DATA_KEY, data);
        commentsFragment fragment = new commentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        slidrConfig = new SlidrConfig.Builder()
                .edge(true)
                .edgeSize(5f)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (slidrInterface == null && getView() != null) {
            Log.i(TAG, "onResume: is Slidable");
            slidrInterface = Slidr.replace(getView().findViewById(R.id.commentsFragmentContent), slidrConfig);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comments, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        if (getArguments() != null) {
            LinkData data = (LinkData) getArguments().getSerializable(SUBMISSIONS_DATA_KEY);
            if (data != null) {
                binding.setVariable(linkdata, data);
                setUpRecyclerView();
                setUpViewModel(data);
            }
        }
        return binding.getRoot();
    }

    private void setUpViewModel(LinkData data) {
        CommentsVM viewModel = new ViewModelProvider(this).get(CommentsVM.class);
        binding.setCommentsViewModel(viewModel);

        viewModel.loadComments(data.getSubreddit_name_prefixed(), data.getId());

        viewModel.getAllComments().observe(this, commentDataList -> adapter.submitList(commentDataList));
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(commentsFragment.this.getContext());
        adapter = new CommentsAdapter();
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new CustomItemAnimator());

        new ItemTouchHelper(new SwipeUtils(commentsFragment.this.getContext(), new SwipeUtils.swipeActionCallback() {
            @Override
            public void performSlightLeftSwipeAction(int adapterPosition) {
            }

            @Override
            public void performSlightRightSwipeAction(int adapterPosition) {
            }

            @Override
            public void performLeftSwipeAction(int adapterPosition) {
            }

            @Override
            public void performRightSwipeAction(int adapterPosition) {
            }
        })).attachToRecyclerView(recyclerView);
    }


}

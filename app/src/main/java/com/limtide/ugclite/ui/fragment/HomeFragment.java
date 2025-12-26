package com.limtide.ugclite.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.limtide.ugclite.R;
import com.limtide.ugclite.data.model.Post;
import com.limtide.ugclite.databinding.FragmentHomeBinding;
import com.limtide.ugclite.ui.activity.PostDetailActivity;
import com.limtide.ugclite.ui.adapter.NoteCardAdapter;
import com.limtide.ugclite.ui.viewmodel.FeedViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String KEY_RECYCLER_STATE = "recycler_state";

    private FragmentHomeBinding binding;
    private NoteCardAdapter notecardAdapter;
    private FeedViewModel feedViewModel;
    private Parcelable savedRecyclerViewState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "HomeFragment onCreate");

        if (savedInstanceState != null) {
            savedRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
        }

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        Log.d(TAG, "HomeFragment onCreateView");
        initViews();
        setupObservers();
        setupRefreshListener();
        return binding.getRoot();
    }

    private void initViews() {
        notecardAdapter = new NoteCardAdapter(getContext());
        binding.recyclerView.setAdapter(notecardAdapter);

        if (savedRecyclerViewState != null) {
            binding.recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerViewState);
            Log.d(TAG, "恢复RecyclerView滚动状态");
        }

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    checkLoadMore();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkLoadMore();
                }
            }
        });

        notecardAdapter.setOnItemClickListener((post, position) -> {
            navigateToDetail(post, position);
        });

        Log.d(TAG, "初始化完成，开始加载数据");
        feedViewModel.loadFeed();
    }

    private void setupObservers() {
        feedViewModel.getFeedPosts().observe(getViewLifecycleOwner(), posts -> {
            Log.d(TAG, "Feed数据更新: " + (posts != null ? posts.size() : 0) + " 条");
            if (posts != null && !posts.isEmpty()) {
                notecardAdapter.setPosts(posts);
                hideEmptyState();
            }
        });

        feedViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding.swipeRefreshLayout != null) {
                binding.swipeRefreshLayout.setRefreshing(loading != null && loading);
            }
        });

        feedViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                showErrorState(errorMsg);
            }
        });

        feedViewModel.getIsEmptyState().observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
                showEmptyState();
            }
        });
    }

    private void setupRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "下拉刷新被触发");
            feedViewModel.refreshFeed();
        });
    }

    private void checkLoadMore() {
        Boolean hasMoreData = feedViewModel.getHasMoreData().getValue();
        Boolean isLoading = feedViewModel.getIsLoading().getValue();

        if (hasMoreData == null || !hasMoreData) {
            return;
        }

        if (isLoading != null && isLoading) {
            return;
        }

        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) binding.recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int[] positions = layoutManager.findLastVisibleItemPositions(null);
            int lastVisiblePosition = 0;
            for (int pos : positions) {
                if (pos > lastVisiblePosition) {
                    lastVisiblePosition = pos;
                }
            }

            int totalItemCount = layoutManager.getItemCount();
            if (totalItemCount > 0 && lastVisiblePosition >= totalItemCount - 3) {
                Log.d(TAG, "接近底部，开始加载更多数据。当前总数: " + totalItemCount + ", 最后可见位置: " + lastVisiblePosition);
                feedViewModel.loadMoreFeed();
            }
        }
    }

    private void navigateToDetail(Post post, int position) {
        try {
            Intent intent = new Intent(requireActivity(), PostDetailActivity.class);
            intent.putExtra("post", (Serializable) post);

            RecyclerView.ViewHolder viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(position);
            View coverImage = null;
            if (viewHolder != null) {
                NoteCardAdapter.ViewHolder noteViewHolder = (NoteCardAdapter.ViewHolder) viewHolder;
                coverImage = noteViewHolder.getBinding().coverImage;
            }

            if (coverImage != null) {
                android.app.ActivityOptions options = android.app.ActivityOptions
                        .makeSceneTransitionAnimation(requireActivity(),
                                android.util.Pair.create(coverImage, "cover_image_transition"));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }

            Log.d(TAG, "Successfully started PostDetailActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error starting PostDetailActivity: " + e.getMessage(), e);
        }
    }

    private void showEmptyState() {
        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showErrorState(String errorMessage) {
        Log.e(TAG, "显示错误状态: " + errorMessage);

        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);

            if (binding.emptyTitle != null) {
                binding.emptyTitle.setText("请检查网络连接");
            }
            if (binding.emptyDescription != null) {
                binding.emptyDescription.setText(errorMessage);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFragment onResume");

        if (notecardAdapter != null && notecardAdapter.getItemCount() > 0) {
            refreshVisibleLikeStatus();
        }
    }

    private void refreshVisibleLikeStatus() {
        if (notecardAdapter == null || binding.recyclerView == null) {
            return;
        }

        StaggeredGridLayoutManager layoutManager =
                (StaggeredGridLayoutManager) binding.recyclerView.getLayoutManager();

        if (layoutManager != null) {
            int[] firstVisiblePositions = layoutManager.findFirstVisibleItemPositions(null);
            int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);

            if (firstVisiblePositions != null && lastVisiblePositions != null) {
                for (int i = 0; i < firstVisiblePositions.length; i++) {
                    int firstPos = firstVisiblePositions[i];
                    int lastPos = lastVisiblePositions[i];

                    for (int pos = firstPos; pos <= lastPos; pos++) {
                        if (pos < notecardAdapter.getItemCount()) {
                            notecardAdapter.notifyItemChanged(pos);
                        }
                    }
                }
            }
        }

        Log.d(TAG, "已刷新所有可见item的点赞状态");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "保存Fragment状态");

        if (binding.recyclerView != null && binding.recyclerView.getLayoutManager() != null) {
            savedRecyclerViewState = binding.recyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(KEY_RECYCLER_STATE, savedRecyclerViewState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "HomeFragment onDestroyView");

        if (binding.recyclerView != null) {
            savedRecyclerViewState = binding.recyclerView.getLayoutManager().onSaveInstanceState();
        }

        binding = null;
    }
}
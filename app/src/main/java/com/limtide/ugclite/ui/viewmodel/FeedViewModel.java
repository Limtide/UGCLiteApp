package com.limtide.ugclite.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.limtide.ugclite.data.model.Post;
import com.limtide.ugclite.data.repository.FeedRepository;
import com.limtide.ugclite.data.repository.FeedRepository.FeedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FeedViewModel extends AndroidViewModel {

    private static final String TAG = "FeedViewModel";

    private final FeedRepository feedRepository;

    private final MutableLiveData<List<Post>> feedPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMoreData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isEmptyState = new MutableLiveData<>(false);

    private final AtomicBoolean isFirstLoad = new AtomicBoolean(true);

    public FeedViewModel(@NonNull Application application) {
        super(application);
        feedRepository = FeedRepository.getInstance();
        observeFeedResult();
    }

    private void observeFeedResult() {
        feedRepository.getFeedResult().observeForever(result -> {
            if (result == null) {
                return;
            }

            isLoading.postValue(false);

            if (result.isSuccess()) {
                errorMessage.postValue(null);

                if (result.getPosts() != null && !result.getPosts().isEmpty()) {
                    List<Post> currentPosts = feedPosts.getValue();
                    if (currentPosts == null) {
                        currentPosts = new ArrayList<>();
                    }

                    List<Post> updatedPosts;
                    if (result.isRefresh()) {
                        updatedPosts = new ArrayList<>(result.getPosts());
                        Log.d(TAG, "刷新数据，替换所有数据，数量: " + updatedPosts.size());
                    } else {
                        updatedPosts = new ArrayList<>(currentPosts);
                        updatedPosts.addAll(result.getPosts());
                        Log.d(TAG, "加载更多数据，新增: " + result.getPosts().size() + "，总数: " + updatedPosts.size());
                    }

                    feedPosts.postValue(updatedPosts);
                    hasMoreData.postValue(result.hasMore());
                    isEmptyState.postValue(false);
                } else {
                    if (result.isRefresh()) {
                        isEmptyState.postValue(true);
                        Log.d(TAG, "首次加载，没有数据");
                    } else {
                        Log.d(TAG, "加载更多，但没有新数据");
                    }
                }

                isFirstLoad.set(false);
            } else {
                errorMessage.postValue(result.getErrorMessage());
                if (result.isRefresh() && isFirstLoad.get()) {
                    isEmptyState.postValue(true);
                }
                Log.e(TAG, "数据加载失败: " + result.getErrorMessage());
            }
        });
    }

    public void loadFeed() {
        if (isFirstLoad.get()) {
            Log.d(TAG, "首次加载数据");
            refreshFeed();
        } else {
            Log.d(TAG, "恢复数据，跳过加载");
            List<Post> currentPosts = feedPosts.getValue();
            if (currentPosts == null || currentPosts.isEmpty()) {
                refreshFeed();
            }
        }
    }

    public void refreshFeed() {
        Log.d(TAG, "刷新Feed数据");
        isLoading.postValue(true);
        feedRepository.loadFeedData(true);
    }

    public void loadMoreFeed() {
        if (!feedRepository.hasMoreData()) {
            Log.d(TAG, "没有更多数据了");
            return;
        }

        if (feedRepository.isLoading()) {
            Log.d(TAG, "数据正在加载中");
            return;
        }

        Log.d(TAG, "加载更多Feed数据");
        feedRepository.loadFeedData(false);
    }

    public LiveData<List<Post>> getFeedPosts() {
        return feedPosts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getHasMoreData() {
        return hasMoreData;
    }

    public LiveData<Boolean> getIsEmptyState() {
        return isEmptyState;
    }

    public void clearErrorMessage() {
        errorMessage.postValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "FeedViewModel被清理");
        feedRepository.cleanup();
    }
}
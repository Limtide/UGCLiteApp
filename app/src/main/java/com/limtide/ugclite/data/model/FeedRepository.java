package com.limtide.ugclite.data.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.limtide.ugclite.data.model.Post;
import com.limtide.ugclite.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedRepository {

    private static final String TAG = "FeedRepository";
    private static final int PAGE_SIZE = 20;

    private final ApiService apiService;
    private final ExecutorService executorService;

    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private final AtomicInteger currentCursor = new AtomicInteger(0);
    private final AtomicBoolean hasMoreData = new AtomicBoolean(true);

    private final MutableLiveData<FeedResult> feedResult = new MutableLiveData<>();

    private static volatile FeedRepository instance;

    private FeedRepository() {
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static FeedRepository getInstance() {
        if (instance == null) {
            synchronized (FeedRepository.class) {
                if (instance == null) {
                    instance = new FeedRepository();
                }
            }
        }
        return instance;
    }

    public void loadFeedData(boolean refresh) {
        if (isLoading.get()) {
            Log.d(TAG, "数据正在加载中，跳过重复请求");
            return;
        }

        if (refresh) {
            currentCursor.set(0);
            hasMoreData.set(true);
            Log.d(TAG, "刷新数据，重置cursor");
        }

        isLoading.set(true);

        int cursor = currentCursor.get();
        Log.d(TAG, "开始加载Feed数据，cursor: " + cursor + ", 数量: " + PAGE_SIZE);

        apiService.getFeedData(PAGE_SIZE, false, cursor, new ApiService.FeedCallback() {
            @Override
            public void onSuccess(List<Post> posts, boolean hasMore) {
                executorService.execute(() -> {
                    try {
                        List<Post> filteredPosts = filterPosts(posts);

                        if (!refresh) {
                            currentCursor.addAndGet(filteredPosts.size());
                        }

                        hasMoreData.set(hasMore);
                        isLoading.set(false);

                        FeedResult result = new FeedResult(
                                true,
                                null,
                                filteredPosts,
                                hasMore,
                                refresh
                        );
                        feedResult.postValue(result);

                        Log.d(TAG, "数据加载成功 - 原始: " + posts.size() +
                                ", 过滤后: " + filteredPosts.size() +
                                ", hasMore: " + hasMore +
                                ", cursor: " + currentCursor.get());
                    } catch (Exception e) {
                        Log.e(TAG, "处理数据时发生异常", e);
                        handleError("数据处理异常: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleError(errorMessage);
            }
        });
    }

    private List<Post> filterPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return posts;
        }

        List<Post> filteredPosts = new ArrayList<>();
        for (Post post : posts) {
            if (shouldShowPost(post)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    private boolean shouldShowPost(Post post) {
        if (post.clips == null || post.clips.isEmpty()) {
            return false;
        }

        for (Post.Clip clip : post.clips) {
            if (clip.type == 0 || clip.type == 1) {
                return true;
            }
        }

        return false;
    }

    private void handleError(String errorMessage) {
        isLoading.set(false);
        FeedResult result = new FeedResult(
                false,
                errorMessage,
                null,
                false,
                false
        );
        feedResult.postValue(result);
        Log.e(TAG, "数据加载失败: " + errorMessage);
    }

    public MutableLiveData<FeedResult> getFeedResult() {
        return feedResult;
    }

    public boolean isLoading() {
        return isLoading.get();
    }

    public boolean hasMoreData() {
        return hasMoreData.get();
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public static class FeedResult {
        private final boolean success;
        private final String errorMessage;
        private final List<Post> posts;
        private final boolean hasMore;
        private final boolean isRefresh;

        public FeedResult(boolean success, String errorMessage, List<Post> posts, boolean hasMore, boolean isRefresh) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.posts = posts;
            this.hasMore = hasMore;
            this.isRefresh = isRefresh;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<Post> getPosts() {
            return posts;
        }

        public boolean hasMore() {
            return hasMore;
        }

        public boolean isRefresh() {
            return isRefresh;
        }
    }
}
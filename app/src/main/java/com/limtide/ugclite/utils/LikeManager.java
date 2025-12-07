package com.limtide.ugclite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 点赞状态管理器
 * 使用SharedPreferences进行本地持久化存储
 *
 * @Context说明:
 * - 使用Application Context，生命周期与应用绑定
 * - 单例模式，实例在应用运行期间持续存在
 * - 不会因Activity/Fragment销毁而丢失数据
 * - 线程安全，支持多线程访问
 *
 * @线程安全说明:
 * - 使用ConcurrentHashMap存储点赞状态，支持并发读写
 * - 使用ReentrantReadWriteLock保护复合操作
 * - 使用volatile保证变量可见性
 * - 同步化SharedPreferences操作避免竞态条件
 */
public class LikeManager {
    private static final String TAG = "LikeManager";
    private static final String PREFS_NAME = "like_prefs";
    private static final String KEY_LIKED_POSTS = "liked_posts";
    private static final String KEY_LIKE_COUNTS = "like_counts";

    private static volatile LikeManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // 内存缓存 - 使用ConcurrentHashMap保证线程安全
    private ConcurrentHashMap<String, Boolean> likedPostIds; // 使用ConcurrentHashMap替代HashSet

    // 读写锁保护复合操作
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile int baseLikeCount = 128; // 基础点赞数量（因为API没有提供）

    private LikeManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        // 强制使用Application Context确保生命周期安全
        Context appContext = context.getApplicationContext();
        if (appContext == null) {
            throw new IllegalStateException("Application Context is not available");
        }

        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        loadData();
    }

    /**
     * 获取LikeManager单例实例
     *
     * @param context 上下文对象，可以是Activity、Service或Application Context
     *               内部会自动转换为Application Context确保生命周期安全
     * @return LikeManager单例实例
     * @throws IllegalArgumentException 如果context为null
     * @throws IllegalStateException 如果Application Context不可用
     */
    public static LikeManager getInstance(Context context) {
        // 双重检查锁定模式，确保线程安全的单例创建
        if (instance == null) {
            synchronized (LikeManager.class) {
                if (instance == null) {
                    if (context == null) {
                        throw new IllegalArgumentException("Context cannot be null when initializing LikeManager");
                    }
                    try {
                        instance = new LikeManager(context);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to initialize LikeManager: " + e.getMessage(), e);
                        throw new RuntimeException("LikeManager initialization failed", e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 从本地存储加载数据 - 线程安全
     */
    private void loadData() {
        try {
            // 加载已点赞的Post ID集合
            Set<String> savedLikes = prefs.getStringSet(KEY_LIKED_POSTS, new HashSet<>());
            // 转换为ConcurrentHashMap保证线程安全
            likedPostIds = new ConcurrentHashMap<>();
            for (String postId : savedLikes) {
                likedPostIds.put(postId, true);
            }
            Log.d(TAG, "Loaded liked posts: " + likedPostIds.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading like data: " + e.getMessage(), e);
            likedPostIds = new ConcurrentHashMap<>();
        }
    }

    /**
     * 保存点赞状态到本地存储 - 线程安全
     */
    private void saveData() {
        lock.readLock().lock();
        try {
            // 创建快照避免在保存过程中数据变化
            Set<String> snapshot = new HashSet<>(likedPostIds.keySet());

            // 使用新的Editor实例避免竞态
            SharedPreferences.Editor newEditor = prefs.edit();
            newEditor.putStringSet(KEY_LIKED_POSTS, snapshot);
            newEditor.apply();

            Log.d(TAG, "Saved liked posts: " + snapshot.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error saving like data: " + e.getMessage(), e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 检查帖子是否已点赞 - 线程安全
     */
    public boolean isPostLiked(String postId) {
        if (postId == null) return false;

        lock.readLock().lock();
        try {
            return likedPostIds.containsKey(postId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 切换点赞状态 - 线程安全，原子操作
     * @param postId 帖子ID
     * @return 新的点赞状态（true=已点赞，false=未点赞）
     */
    public boolean toggleLike(String postId) {
        if (postId == null) return false;

        lock.writeLock().lock();
        try {
            // 原子性的检查-修改操作
            Boolean wasLiked = likedPostIds.get(postId);
            boolean isCurrentlyLiked = (wasLiked != null && wasLiked);

            boolean newStatus;
            if (isCurrentlyLiked) {
                // 取消点赞
                likedPostIds.remove(postId);
                newStatus = false;
                Log.d(TAG, "Unliked post: " + postId);
            } else {
                // 点赞
                likedPostIds.put(postId, true);
                newStatus = true;
                Log.d(TAG, "Liked post: " + postId);
            }

            // 在锁保护下保存数据
            saveDataUnsafe();

            return newStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 设置点赞状态 - 线程安全
     * @param postId 帖子ID
     * @param isLiked 是否已点赞
     */
    public void setLikeStatus(String postId, boolean isLiked) {
        if (postId == null) return;

        lock.writeLock().lock();
        try {
            if (isLiked) {
                likedPostIds.put(postId, true);
            } else {
                likedPostIds.remove(postId);
            }

            // 在锁保护下保存数据
            saveDataUnsafe();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取点赞数量 - 线程安全
     * 由于API没有提供点赞数量，使用基础数量加上本地计算
     * @param postId 帖子ID
     * @return 点赞数量
     */
    public int getLikeCount(String postId) {
        if (postId == null) return baseLikeCount;

        lock.readLock().lock();
        try {
            // 在锁保护下读取状态，保证一致性
            boolean isLiked = likedPostIds.containsKey(postId);
            return baseLikeCount + (isLiked ? 1 : 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 设置基础点赞数量 - 线程安全
     * @param baseCount 基础点赞数量
     */
    public void setBaseLikeCount(int baseCount) {
        lock.writeLock().lock();
        try {
            this.baseLikeCount = baseCount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取所有已点赞的帖子ID - 线程安全
     */
    public Set<String> getAllLikedPosts() {
        lock.readLock().lock();
        try {
            return new HashSet<>(likedPostIds.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 清空所有点赞数据 - 线程安全
     */
    public void clearAllData() {
        lock.writeLock().lock();
        try {
            likedPostIds.clear();

            // 使用新的Editor实例
            SharedPreferences.Editor newEditor = prefs.edit();
            newEditor.clear();
            newEditor.apply();

            Log.d(TAG, "Cleared all like data");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取点赞统计数据 - 线程安全
     */
    public void logStats() {
        lock.readLock().lock();
        try {
            Log.d(TAG, "Like stats - Total liked posts: " + likedPostIds.size());
            Log.d(TAG, "Base like count: " + baseLikeCount);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 保存数据到本地存储 - 内部方法，调用者需要持有锁
     */
    private void saveDataUnsafe() {
        try {
            // 创建快照避免在保存过程中数据变化
            Set<String> snapshot = new HashSet<>(likedPostIds.keySet());

            // 使用新的Editor实例避免竞态
            SharedPreferences.Editor newEditor = prefs.edit();
            newEditor.putStringSet(KEY_LIKED_POSTS, snapshot);
            newEditor.apply();

            Log.d(TAG, "Saved liked posts: " + snapshot.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error saving like data: " + e.getMessage(), e);
        }
    }

    /**
     * 清理资源和内存缓存
     * 通常在应用退出时调用，用于释放内存
     * 注意：此操作不会删除持久化存储的数据
     */
    public void cleanup() {
        try {
            // 清理内存缓存
            if (likedPostIds != null) {
                likedPostIds.clear();
            }
            likedPostIds = null;

            // 清理Editor引用
            editor = null;
            prefs = null;

            Log.d(TAG, "LikeManager cleaned up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage(), e);
        }
    }

    /**
     * 重置单例实例
     * 通常用于测试或特殊场景，谨慎使用
     * 重置后需要重新调用getInstance()初始化
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
            Log.d(TAG, "LikeManager instance reset");
        }
    }
}
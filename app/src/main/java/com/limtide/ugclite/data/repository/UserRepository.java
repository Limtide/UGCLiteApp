package com.limtide.ugclite.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.limtide.ugclite.database.AppDatabase;
import com.limtide.ugclite.database.dao.UserDao;
import com.limtide.ugclite.database.entity.User;
import com.limtide.ugclite.utils.PasswordHasher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户数据仓库
 * 处理用户相关的业务逻辑和数据操作
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String INVALID_CREDENTIALS = "用户名或密码错误";
    private static final String TOO_MANY_ATTEMPTS = "登录尝试过多，请稍后再试";
    private static final String DUMMY_PASSWORD_HASH = "pbkdf2_sha1$120000$"
            + "000102030405060708090a0b0c0d0e0f$cb00d3e02c8b7f0cc5d41439d06828fb65e511644aabf4943b2fedf91992e805";

    private final UserDao userDao;
    private final LiveData<List<User>> allActiveUsers;
    private final ExecutorService executorService;
    private final PasswordHasher passwordHasher;
    private final LoginAttemptLimiter loginAttemptLimiter;

    // 登录状态LiveData
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        allActiveUsers = userDao.getAllActiveUsers();
        executorService = Executors.newSingleThreadExecutor();
        passwordHasher = new PasswordHasher();
        loginAttemptLimiter = new LoginAttemptLimiter(application);
    }


    /**
     * 用户登录验证
     */
    public void loginUser(String username, String password) {
        executorService.execute(() -> {
            try {
                if (username == null || username.trim().isEmpty()) {
                    loginResult.postValue(new LoginResult(false, "用户名不能为空", null));
                    return;
                }
                if (password == null || password.trim().isEmpty()) {
                    loginResult.postValue(new LoginResult(false, "密码不能为空", null));
                    return;
                }

                String normalizedUsername = username.trim();
                if (!loginAttemptLimiter.isAllowed(normalizedUsername)) {
                    loginResult.postValue(new LoginResult(false, TOO_MANY_ATTEMPTS, null));
                    return;
                }

                User user = userDao.getUserByUsername(normalizedUsername);
                if (user == null || !user.isActive()) {
                    passwordHasher.verify(password, DUMMY_PASSWORD_HASH);
                    rejectInvalidCredentials(normalizedUsername);
                    return;
                }

                if (passwordHasher.isLegacyMd5(user.getPassword())) {
                    if (!passwordHasher.verifyLegacyMd5(password, user.getPassword())) {
                        passwordHasher.verify(password, DUMMY_PASSWORD_HASH);
                        rejectInvalidCredentials(normalizedUsername);
                        return;
                    }
                    user.setPassword(passwordHasher.hash(password));
                } else if (!passwordHasher.verify(password, user.getPassword())) {
                    rejectInvalidCredentials(normalizedUsername);
                    return;
                }

                loginAttemptLimiter.recordSuccess(normalizedUsername);
                user.updateLastLoginTime();
                userDao.updateUser(user);
                loginResult.postValue(new LoginResult(true, "登录成功", user));
            } catch (Exception e) {
                Log.e(TAG, "Login failed", e);
                loginResult.postValue(new LoginResult(false, "登录失败，请稍后重试", null));
            }
        });
    }

    private void rejectInvalidCredentials(String username) {
        loginAttemptLimiter.recordFailure(username);
        loginResult.postValue(new LoginResult(false, INVALID_CREDENTIALS, null));
    }

    /**
     * 获取登录状态LiveData
     */
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * 创建新用户
     */
    public void createUser(User user) {
        executorService.execute(() -> {
            try {
                // 检查用户名是否已存在
                if (userDao.checkUsernameExists(user.getUsername()) > 0) {
                    loginResult.postValue(new LoginResult(false, "用户名已存在", null));
                    return;
                }
                user.setPassword(passwordHasher.hash(user.getPassword()));


                long result = userDao.insertUser(user);
                if (result > 0) {
                    loginResult.postValue(new LoginResult(true, "注册成功", user));
                } else {
                    loginResult.postValue(new LoginResult(false, "注册失败", null));
                }
            } catch (Exception e) {
                loginResult.postValue(new LoginResult(false, "注册失败：" + e.getMessage(), null));
            }
        });
    }

    /**
     * 根据用户名获取用户
     */
    public LiveData<User> getUserByUsername(String username) {
        return userDao.getUserByUsernameLiveData(username);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 登录结果封装类
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final User user;

        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }
}
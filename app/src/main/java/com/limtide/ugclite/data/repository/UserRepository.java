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

    private final UserDao userDao;
    private final LiveData<List<User>> allActiveUsers;
    private final ExecutorService executorService;
    private final PasswordHasher passwordHasher;

    // 登录状态LiveData
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        allActiveUsers = userDao.getAllActiveUsers();
        executorService = Executors.newSingleThreadExecutor();
        passwordHasher = new PasswordHasher();
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

                User user = userDao.getUserByUsername(username.trim());
                if (user == null) {
                    loginResult.postValue(new LoginResult(false, "用户不存在", null));
                    return;
                }
                if (!user.isActive()) {
                    loginResult.postValue(new LoginResult(false, "账号已停用", null));
                    return;
                }
                if (passwordHasher.isLegacyMd5(user.getPassword())) {
                    loginResult.postValue(new LoginResult(false, "凭据格式已过期，请重置密码", null));
                    return;
                }
                if (!passwordHasher.verify(password, user.getPassword())) {
                    loginResult.postValue(new LoginResult(false, "密码错误", null));
                    return;
                }

                user.updateLastLoginTime();
                userDao.updateUser(user);
                loginResult.postValue(new LoginResult(true, "登录成功", user));
            } catch (Exception e) {
                loginResult.postValue(new LoginResult(false, "登录失败：" + e.getMessage(), null));
            }
        });
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
                if (!passwordHasher.isEncoded(user.getPassword())) {
                    user.setPassword(passwordHasher.hash(user.getPassword()));
                }


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
     * 预埋测试用户数据
     */
    public void createTestUsers() {
        executorService.execute(() -> {
            try {
                // 检查是否已有测试用户
                User existingUser = userDao.getUserByUsername("demo");
                if (existingUser == null) {

                    User userdemo = new User("demo", passwordHasher.hash("demo123"), "演示账号", "用于功能演示", "");

                    userDao.insertUsers(List.of(userdemo));
                    Log.d("UserRepository", "测试用户数据创建成功");
                } else if (passwordHasher.isLegacyMd5(existingUser.getPassword())) {
                    existingUser.setPassword(passwordHasher.hash("demo123"));
                    existingUser.setSignature("用于功能演示");
                    userDao.updateUser(existingUser);
                    Log.d("UserRepository", "测试用户凭据已升级");
                } else {
                    Log.d("UserRepository", "测试用户数据已存在");
                }
            } catch (Exception e) {
                // 预埋数据失败不影响应用运行
                Log.e("UserRepository", "创建测试用户数据失败", e);
            }
        });
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
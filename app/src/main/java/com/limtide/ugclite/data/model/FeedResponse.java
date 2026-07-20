package com.limtide.ugclite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Feed API 响应数据模型
 * 对应 API 返回的 JSON 结构
 */
public class FeedResponse {

    @SerializedName("status_code")
    public Integer statusCode; // 旧接口状态码（0：成功，其它：失败）

    @SerializedName("code")
    public Integer code; // README 接口状态码（200：成功）

    @SerializedName("message")
    public String message;

    @SerializedName("has_more")
    public int hasMore; // 是否还有更多作品（取值：0或1）

    @SerializedName("post_list")
    public List<Post> postList; // 作品列表

    @SerializedName("data")
    public List<Post> data; // README 接口作品列表

    public boolean hasRecognizedStatus() {
        return code != null || statusCode != null;
    }

    /**
     * 判断API请求是否成功
     */
    public boolean isSuccess() {
        if (code != null) {
            return code == 200;
        }
        return statusCode != null && statusCode == 0;
    }

    public List<Post> getPosts() {
        return data != null ? data : postList;
    }

    /**
     * 判断是否还有更多数据
     */
    public boolean hasMoreData() {
        return hasMore == 1;
    }

    @Override
    public String toString() {
        return "FeedResponse{" +
                "statusCode=" + statusCode +
                ", code=" + code +
                ", hasMore=" + hasMore +
                ", posts.size()=" + (getPosts() != null ? getPosts().size() : 0) +
                '}';
    }
}
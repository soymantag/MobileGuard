package com.chris.mobileguard.domain;

/**
 * Created by chris on 16-4-20.
 */
public class UrlBean {
    private String url;
    private int versionCode;
    private String desc;

    public String getUrl() {
        return url;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "UrlBean{" +
                "url='" + url + '\'' +
                ", versionCode=" + versionCode +
                ", desc='" + desc + '\'' +
                '}';
    }
}

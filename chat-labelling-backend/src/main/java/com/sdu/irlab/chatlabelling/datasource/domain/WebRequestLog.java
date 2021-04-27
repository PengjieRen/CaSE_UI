package com.sdu.irlab.chatlabelling.datasource.domain;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class WebRequestLog extends BaseEntity {
    private String ip;
    private String requestUrl;
    private String requestUser;
    private String classMethod;//响应函数
    @Lob
    private String parameters;
    @Lob
    private String response;
    private long spendTimeMills;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getClassMethod() {
        return classMethod;
    }

    public void setClassMethod(String classMethod) {
        this.classMethod = classMethod;
    }

    public long getSpendTimeMills() {
        return spendTimeMills;
    }

    public void setSpendTimeMills(long spendTimeMills) {
        this.spendTimeMills = spendTimeMills;
    }

    public String getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(String requestUser) {
        this.requestUser = requestUser;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}

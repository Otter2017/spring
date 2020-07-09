package cn.gigahome.web.message;

import java.util.List;

public class ObjectModel {
    private int version;

    private String productId;

    private List<Parameter> properties;

    private List<ObjectMethod> services;

    private List<ObjectMethod> events;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<Parameter> getProperties() {
        return properties;
    }

    public void setProperties(List<Parameter> properties) {
        this.properties = properties;
    }

    public List<ObjectMethod> getServices() {
        return services;
    }

    public void setServices(List<ObjectMethod> services) {
        this.services = services;
    }

    public List<ObjectMethod> getEvents() {
        return events;
    }

    public void setEvents(List<ObjectMethod> events) {
        this.events = events;
    }


    public ObjectMethod getMethod(short methodIdentifier) {
        if (services != null) {
            for (ObjectMethod method : services) {
                if (methodIdentifier == method.getMethodIdentifier()) {
                    return method;
                }
            }
        }
        if (events != null) {
            for (ObjectMethod method : events) {
                if (methodIdentifier == method.getMethodIdentifier()) {
                    return method;
                }
            }
        }
        return null;
    }
}

package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PackageServiceItemId implements Serializable {

    @Column(name = "package_code", length = 32)
    private String packageCode;

    @Column(name = "service_id", length = 32)
    private String serviceId;

    public PackageServiceItemId() {
    }

    public PackageServiceItemId(String packageCode, String serviceId) {
        this.packageCode = packageCode;
        this.serviceId = serviceId;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageServiceItemId that = (PackageServiceItemId) o;
        return Objects.equals(packageCode, that.packageCode) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageCode, serviceId);
    }
}

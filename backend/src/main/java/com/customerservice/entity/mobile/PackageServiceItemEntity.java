package com.customerservice.entity.mobile;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "package_service_items")
public class PackageServiceItemEntity {

    @EmbeddedId
    private PackageServiceItemId id;

    public PackageServiceItemId getId() {
        return id;
    }

    public void setId(PackageServiceItemId id) {
        this.id = id;
    }
}

package com.enty.payment.customer.storage;

import org.springframework.web.multipart.MultipartFile;

public interface CustomerLogoStorage {
    /** Stores a validated customer logo and returns its public application path. */
    String store(MultipartFile logo);

    /** Removes a previously stored local customer logo when it belongs to this storage provider. */
    void delete(String logoPath);
}

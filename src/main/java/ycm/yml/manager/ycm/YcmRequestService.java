package ycm.yml.manager.ycm;

import apple.utilities.request.AppleRequestService;

public class YcmRequestService extends AppleRequestService {
    @Override
    public int getRequestsPerTimeUnit() {
        return 10; // arbitrary non-zero value
    }

    @Override
    public int getTimeUnitMillis() {
        return 0;
    }

    @Override
    public int getSafeGuardBuffer() {
        return 0;
    }
}

package com.ciphermarket.api.upload.scan;

public record ScanResult(ScanStatus status, String details) {

    public enum ScanStatus {
        CLEAN,
        INFECTED,
        ERROR,
        SKIPPED
    }

    public static ScanResult clean() {
        return new ScanResult(ScanStatus.CLEAN, null);
    }

    public static ScanResult infected(String details) {
        return new ScanResult(ScanStatus.INFECTED, details);
    }

    public static ScanResult error(String details) {
        return new ScanResult(ScanStatus.ERROR, details);
    }

    public static ScanResult skipped(String details) {
        return new ScanResult(ScanStatus.SKIPPED, details);
    }

    public boolean isClean() {
        return status == ScanStatus.CLEAN || status == ScanStatus.SKIPPED;
    }
}

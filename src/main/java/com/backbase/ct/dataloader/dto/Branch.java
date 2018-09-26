package com.backbase.ct.dataloader.dto;

public enum Branch {

    ROOT, RETAIL, BUSINESS;

    public boolean isRoot() {
        return this.equals(ROOT);
    }

    public boolean isRetail() {
        return this.equals(RETAIL);
    }

    public boolean isBusiness() {
        return this.equals(BUSINESS);
    }
}

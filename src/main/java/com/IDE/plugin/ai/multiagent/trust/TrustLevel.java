package com.IDE.plugin.ai.multiagent.trust;

/**
 * TrustLevel: Enumeration representing different levels of trust
 */
public enum TrustLevel {
    NONE(0, "No trust"),
    LOW(1, "Low trust"),
    MEDIUM(2, "Medium trust"),
    HIGH(3, "High trust"),
    FULL(4, "Full trust");
    
    private final int value;
    private final String description;
    
    TrustLevel(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TrustLevel fromValue(int value) {
        for (TrustLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        return NONE;
    }
}
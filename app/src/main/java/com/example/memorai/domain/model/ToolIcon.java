package com.example.memorai.domain.model;

public class ToolIcon {
    private final int iconResId;
    private final String toolName;

    public ToolIcon(int iconResId, String toolName) {
        this.iconResId = iconResId;
        this.toolName = toolName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getToolName() {
        return toolName;
    }
}


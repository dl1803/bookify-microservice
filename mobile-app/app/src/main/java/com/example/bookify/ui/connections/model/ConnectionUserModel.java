package com.example.bookify.ui.connections.model;

public class ConnectionUserModel {
    public enum TabType {
        YOUR_FRIENDS,
        REQUESTS,
        SENT
    }

    private String name;
    private String subtitle;
    private int avatarResId;
    private TabType tabType;
    private String primaryActionText;
    private boolean showSecondaryAction;

    public ConnectionUserModel(String name, String subtitle, int avatarResId, TabType tabType, String primaryActionText, boolean showSecondaryAction) {
        this.name = name;
        this.subtitle = subtitle;
        this.avatarResId = avatarResId;
        this.tabType = tabType;
        this.primaryActionText = primaryActionText;
        this.showSecondaryAction = showSecondaryAction;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public TabType getTabType() {
        return tabType;
    }

    public String getPrimaryActionText() {
        return primaryActionText;
    }

    public boolean isShowSecondaryAction() {
        return showSecondaryAction;
    }
}

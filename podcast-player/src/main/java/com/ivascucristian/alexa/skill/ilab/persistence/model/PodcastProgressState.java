package com.ivascucristian.alexa.skill.ilab.persistence.model;

public class PodcastProgressState {
    private Episode episode;
    private String userId;
    private long offsetInMillis;
    private boolean loop = false;
    private boolean ascendingOrder = false;

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getOffsetInMillis() {
        return offsetInMillis;
    }

    public void setOffsetInMillis(long offsetInMillis) {
        this.offsetInMillis = offsetInMillis;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isAscendingOrder() {
        return ascendingOrder;
    }

    public void setAscendingOrder(boolean ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }
}

package com.aegisguard.playerdata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Packet tracking data for exploit/anomaly detection.
 */
public final class PacketData {

    private static final int MAX_HISTORY = 100;

    private final AtomicInteger packetsThisSecond = new AtomicInteger(0);
    private final AtomicInteger totalPackets = new AtomicInteger(0);
    private long lastPacketTime;
    private long lastResetTime = System.currentTimeMillis();
    private final Deque<Integer> packetsPerSecondHistory = new ArrayDeque<>();
    private final Deque<String> recentPacketTypes = new ArrayDeque<>();

    // Exploit tracking
    private int invalidPackets;
    private int duplicatePackets;
    private int outOfOrderPackets;
    private long lastBookEditTime;
    private int bookEditsThisMinute;
    private long lastSignEditTime;
    private int signEditsThisMinute;
    private long lastChatTime;
    private int chatMessagesThisMinute;
    private long lastCommandTime;
    private int commandsThisMinute;
    private long lastInteractionTime;
    private int interactionsThisSecond;

    /**
     * Record a packet received.
     */
    public void recordPacket(String packetType) {
        long now = System.currentTimeMillis();
        totalPackets.incrementAndGet();
        packetsThisSecond.incrementAndGet();
        lastPacketTime = now;

        recentPacketTypes.addLast(packetType);
        if (recentPacketTypes.size() > MAX_HISTORY) recentPacketTypes.pollFirst();

        // Reset per-second counter
        if (now - lastResetTime >= 1000) {
            packetsPerSecondHistory.addLast(packetsThisSecond.getAndSet(0));
            if (packetsPerSecondHistory.size() > 60) packetsPerSecondHistory.pollFirst();
            lastResetTime = now;
        }
    }

    /**
     * Record an invalid packet.
     */
    public void recordInvalidPacket() {
        invalidPackets++;
    }

    /**
     * Record a duplicate packet.
     */
    public void recordDuplicatePacket() {
        duplicatePackets++;
    }

    /**
     * Record out-of-order packets.
     */
    public void recordOutOfOrder() {
        outOfOrderPackets++;
    }

    /**
     * Record a book edit for rate limiting.
     */
    public void recordBookEdit() {
        long now = System.currentTimeMillis();
        if (now - lastBookEditTime > 60000) {
            bookEditsThisMinute = 0;
        }
        lastBookEditTime = now;
        bookEditsThisMinute++;
    }

    /**
     * Record a sign edit for rate limiting.
     */
    public void recordSignEdit() {
        long now = System.currentTimeMillis();
        if (now - lastSignEditTime > 60000) {
            signEditsThisMinute = 0;
        }
        lastSignEditTime = now;
        signEditsThisMinute++;
    }

    /**
     * Record a chat message for flood detection.
     */
    public void recordChat() {
        long now = System.currentTimeMillis();
        if (now - lastChatTime > 60000) {
            chatMessagesThisMinute = 0;
        }
        lastChatTime = now;
        chatMessagesThisMinute++;
    }

    /**
     * Record a command for flood detection.
     */
    public void recordCommand() {
        long now = System.currentTimeMillis();
        if (now - lastCommandTime > 60000) {
            commandsThisMinute = 0;
        }
        lastCommandTime = now;
        commandsThisMinute++;
    }

    /**
     * Record an interaction for spam detection.
     */
    public void recordInteraction() {
        long now = System.currentTimeMillis();
        if (now - lastInteractionTime > 1000) {
            interactionsThisSecond = 0;
        }
        lastInteractionTime = now;
        interactionsThisSecond++;
    }

    /**
     * Get packets per second average.
     */
    public double getAvgPacketsPerSecond() {
        if (packetsPerSecondHistory.isEmpty()) return 0;
        int sum = 0;
        for (int count : packetsPerSecondHistory) sum += count;
        return (double) sum / packetsPerSecondHistory.size();
    }

    /**
     * Reset packet data.
     */
    public void reset() {
        packetsThisSecond.set(0);
        totalPackets.set(0);
        invalidPackets = 0;
        duplicatePackets = 0;
        outOfOrderPackets = 0;
        bookEditsThisMinute = 0;
        signEditsThisMinute = 0;
        chatMessagesThisMinute = 0;
        commandsThisMinute = 0;
        interactionsThisSecond = 0;
        packetsPerSecondHistory.clear();
        recentPacketTypes.clear();
    }

    // --- Getters ---

    public int getPacketsThisSecond() { return packetsThisSecond.get(); }
    public int getTotalPackets() { return totalPackets.get(); }
    public long getLastPacketTime() { return lastPacketTime; }
    public int getInvalidPackets() { return invalidPackets; }
    public int getDuplicatePackets() { return duplicatePackets; }
    public int getOutOfOrderPackets() { return outOfOrderPackets; }
    public int getBookEditsThisMinute() { return bookEditsThisMinute; }
    public int getSignEditsThisMinute() { return signEditsThisMinute; }
    public int getChatMessagesThisMinute() { return chatMessagesThisMinute; }
    public int getCommandsThisMinute() { return commandsThisMinute; }
    public int getInteractionsThisSecond() { return interactionsThisSecond; }
    public Deque<String> getRecentPacketTypes() { return recentPacketTypes; }
}

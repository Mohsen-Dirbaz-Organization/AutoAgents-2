package com.IDE.plugin.ai.multiagent.trust.consensus;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.time.Instant;
import java.time.Duration;

/**
 * RAFT consensus algorithm implementation for leader election and log replication.
 * Provides strong consistency guarantees for distributed state management.
 */
public class RAFTConsensus {
    
    private final String nodeId;
    private final List<String> clusterNodes;
    private final AtomicReference<NodeRole> currentRole;
    private final AtomicReference<String> currentLeader;
    private final AtomicLong currentTerm;
    private final AtomicReference<String> votedFor;
    private final List<LogEntry> log;
    private final AtomicInteger commitIndex;
    private final AtomicInteger lastApplied;
    private final Map<String, Integer> nextIndex;
    private final Map<String, Integer> matchIndex;
    private final ReentrantReadWriteLock stateLock;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private final BlockingQueue<RaftMessage> messageQueue;
    
    // Timing parameters
    private static final int MIN_ELECTION_TIMEOUT_MS = 150;
    private static final int MAX_ELECTION_TIMEOUT_MS = 300;
    private static final int HEARTBEAT_INTERVAL_MS = 50;
    private static final int RPC_TIMEOUT_MS = 100;
    
    private volatile ScheduledFuture<?> electionTimer;
    private volatile ScheduledFuture<?> heartbeatTimer;
    private volatile boolean isRunning;
    
    public RAFTConsensus(String nodeId, List<String> clusterNodes) {
        this.nodeId = nodeId;
        this.clusterNodes = new ArrayList<>(clusterNodes);
        this.currentRole = new AtomicReference<>(NodeRole.FOLLOWER);
        this.currentLeader = new AtomicReference<>(null);
        this.currentTerm = new AtomicLong(0);
        this.votedFor = new AtomicReference<>(null);
        this.log = new CopyOnWriteArrayList<>();
        this.commitIndex = new AtomicInteger(0);
        this.lastApplied = new AtomicInteger(0);
        this.nextIndex = new ConcurrentHashMap<>();
        this.matchIndex = new ConcurrentHashMap<>();
        this.stateLock = new ReentrantReadWriteLock();
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.random = new Random();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.isRunning = true;
        
        initializeIndices();
        startMessageProcessor();
        resetElectionTimer();
    }
    
    /**
     * Proposes a new command to be replicated across the cluster
     */
    public CompletableFuture<Boolean> proposeCommand(String command, Map<String, Object> data) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (currentRole.get() != NodeRole.LEADER) {
            // Forward to leader if known
            String leader = currentLeader.get();
            if (leader != null) {
                forwardToLeader(leader, command, data);
                future.complete(false);
            } else {
                future.completeExceptionally(new IllegalStateException("No leader elected"));
            }
            return future;
        }
        
        // Leader appends to log
        stateLock.writeLock().lock();
        try {
            LogEntry entry = new LogEntry(
                log.size(),
                currentTerm.get(),
                command,
                data,
                Instant.now()
            );
            log.add(entry);
            
            // Start replication
            replicateLogEntry(entry, future);
        } finally {
            stateLock.writeLock().unlock();
        }
        
        return future;
    }
    
    /**
     * Handles incoming RAFT messages
     */
    public void handleMessage(RaftMessage message) {
        try {
            messageQueue.offer(message, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processMessage(RaftMessage message) {
        switch (message.getType()) {
            case REQUEST_VOTE:
                handleRequestVote(message);
                break;
            case VOTE_RESPONSE:
                handleVoteResponse(message);
                break;
            case APPEND_ENTRIES:
                handleAppendEntries(message);
                break;
            case APPEND_ENTRIES_RESPONSE:
                handleAppendEntriesResponse(message);
                break;
        }
    }
    
    /**
     * Handles RequestVote RPC
     */
    private void handleRequestVote(RaftMessage message) {
        RequestVoteRequest request = (RequestVoteRequest) message.getPayload();
        boolean voteGranted = false;
        
        stateLock.writeLock().lock();
        try {
            // Update term if necessary
            if (request.getTerm() > currentTerm.get()) {
                currentTerm.set(request.getTerm());
                votedFor.set(null);
                currentRole.set(NodeRole.FOLLOWER);
                resetElectionTimer();
            }
            
            // Grant vote if conditions are met
            if (request.getTerm() == currentTerm.get() &&
                (votedFor.get() == null || votedFor.get().equals(request.getCandidateId())) &&
                isLogUpToDate(request.getLastLogIndex(), request.getLastLogTerm())) {
                
                votedFor.set(request.getCandidateId());
                voteGranted = true;
                resetElectionTimer();
            }
        } finally {
            stateLock.writeLock().unlock();
        }
        
        // Send response
        sendMessage(request.getCandidateId(), new RaftMessage(
            MessageType.VOTE_RESPONSE,
            nodeId,
            new RequestVoteResponse(currentTerm.get(), voteGranted)
        ));
    }
    
    /**
     * Handles VoteResponse RPC
     */
    private void handleVoteResponse(RaftMessage message) {
        if (currentRole.get() != NodeRole.CANDIDATE) {
            return;
        }
        
        RequestVoteResponse response = (RequestVoteResponse) message.getPayload();
        
        stateLock.writeLock().lock();
        try {
            if (response.getTerm() > currentTerm.get()) {
                currentTerm.set(response.getTerm());
                currentRole.set(NodeRole.FOLLOWER);
                votedFor.set(null);
                resetElectionTimer();
                return;
            }
            
            if (response.isVoteGranted() && response.getTerm() == currentTerm.get()) {
                // Count vote
                int votesReceived = countVotes();
                if (votesReceived > clusterNodes.size() / 2) {
                    // Become leader
                    becomeLeader();
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }
    
    /**
     * Handles AppendEntries RPC (heartbeat/replication)
     */
    private void handleAppendEntries(RaftMessage message) {
        AppendEntriesRequest request = (AppendEntriesRequest) message.getPayload();
        boolean success = false;
        
        stateLock.writeLock().lock();
        try {
            // Update term and convert to follower if necessary
            if (request.getTerm() > currentTerm.get()) {
                currentTerm.set(request.getTerm());
                votedFor.set(null);
                currentRole.set(NodeRole.FOLLOWER);
            }
            
            if (request.getTerm() == currentTerm.get()) {
                currentRole.set(NodeRole.FOLLOWER);
                currentLeader.set(request.getLeaderId());
                resetElectionTimer();
                
                // Check log consistency
                if (request.getPrevLogIndex() == 0 ||
                    (request.getPrevLogIndex() <= log.size() &&
                     log.get(request.getPrevLogIndex() - 1).getTerm() == request.getPrevLogTerm())) {
                    
                    // Append entries
                    success = appendEntries(request);
                    
                    // Update commit index
                    if (request.getLeaderCommit() > commitIndex.get()) {
                        commitIndex.set(Math.min(request.getLeaderCommit(), log.size()));
                        applyCommittedEntries();
                    }
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
        
        // Send response
        sendMessage(request.getLeaderId(), new RaftMessage(
            MessageType.APPEND_ENTRIES_RESPONSE,
            nodeId,
            new AppendEntriesResponse(currentTerm.get(), success, log.size())
        ));
    }
    
    /**
     * Handles AppendEntriesResponse RPC
     */
    private void handleAppendEntriesResponse(RaftMessage message) {
        if (currentRole.get() != NodeRole.LEADER) {
            return;
        }
        
        AppendEntriesResponse response = (AppendEntriesResponse) message.getPayload();
        String followerId = message.getSenderId();
        
        stateLock.writeLock().lock();
        try {
            if (response.getTerm() > currentTerm.get()) {
                currentTerm.set(response.getTerm());
                currentRole.set(NodeRole.FOLLOWER);
                votedFor.set(null);
                resetElectionTimer();
                return;
            }
            
            if (response.isSuccess()) {
                // Update indices
                nextIndex.put(followerId, response.getMatchIndex() + 1);
                matchIndex.put(followerId, response.getMatchIndex());
                
                // Check if we can advance commit index
                updateCommitIndex();
            } else {
                // Decrement nextIndex and retry
                int currentNext = nextIndex.getOrDefault(followerId, 1);
                if (currentNext > 1) {
                    nextIndex.put(followerId, currentNext - 1);
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }
    
    /**
     * Starts election process
     */
    private void startElection() {
        stateLock.writeLock().lock();
        try {
            currentRole.set(NodeRole.CANDIDATE);
            currentTerm.incrementAndGet();
            votedFor.set(nodeId);
            resetElectionTimer();
            
            // Request votes from all other nodes
            int lastLogIndex = log.size();
            long lastLogTerm = lastLogIndex > 0 ? log.get(lastLogIndex - 1).getTerm() : 0;
            
            RequestVoteRequest request = new RequestVoteRequest(
                currentTerm.get(),
                nodeId,
                lastLogIndex,
                lastLogTerm
            );
            
            for (String node : clusterNodes) {
                if (!node.equals(nodeId)) {
                    sendMessage(node, new RaftMessage(
                        MessageType.REQUEST_VOTE,
                        nodeId,
                        request
                    ));
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }
    
    /**
     * Transitions to leader role
     */
    private void becomeLeader() {
        currentRole.set(NodeRole.LEADER);
        currentLeader.set(nodeId);
        
        // Initialize leader state
        for (String node : clusterNodes) {
            if (!node.equals(nodeId)) {
                nextIndex.put(node, log.size() + 1);
                matchIndex.put(node, 0);
            }
        }
        
        // Cancel election timer and start heartbeat
        if (electionTimer != null) {
            electionTimer.cancel(false);
        }
        
        sendHeartbeat();
        heartbeatTimer = scheduler.scheduleAtFixedRate(
            this::sendHeartbeat,
            0,
            HEARTBEAT_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Sends heartbeat/AppendEntries to all followers
     */
    private void sendHeartbeat() {
        if (currentRole.get() != NodeRole.LEADER) {
            return;
        }
        
        for (String node : clusterNodes) {
            if (!node.equals(nodeId)) {
                sendAppendEntries(node);
            }
        }
    }
    
    /**
     * Sends AppendEntries RPC to a specific node
     */
    private void sendAppendEntries(String node) {
        stateLock.readLock().lock();
        try {
            int nextIdx = nextIndex.getOrDefault(node, 1);
            int prevLogIndex = nextIdx - 1;
            long prevLogTerm = prevLogIndex > 0 ? log.get(prevLogIndex - 1).getTerm() : 0;
            
            List<LogEntry> entries = new ArrayList<>();
            if (nextIdx <= log.size()) {
                entries = log.subList(nextIdx - 1, log.size());
            }
            
            AppendEntriesRequest request = new AppendEntriesRequest(
                currentTerm.get(),
                nodeId,
                prevLogIndex,
                prevLogTerm,
                entries,
                commitIndex.get()
            );
            
            sendMessage(node, new RaftMessage(
                MessageType.APPEND_ENTRIES,
                nodeId,
                request
            ));
        } finally {
            stateLock.readLock().unlock();
        }
    }
    
    /**
     * Replicates a log entry to followers
     */
    private void replicateLogEntry(LogEntry entry, CompletableFuture<Boolean> future) {
        // Send to all followers
        for (String node : clusterNodes) {
            if (!node.equals(nodeId)) {
                sendAppendEntries(node);
            }
        }
        
        // Schedule timeout check
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                checkReplicationStatus(entry.getIndex(), future);
            }
        }, RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Checks if log entry has been replicated to majority
     */
    private void checkReplicationStatus(int entryIndex, CompletableFuture<Boolean> future) {
        int replicatedCount = 1; // Leader has the entry
        
        for (Map.Entry<String, Integer> entry : matchIndex.entrySet()) {
            if (entry.getValue() >= entryIndex) {
                replicatedCount++;
            }
        }
        
        if (replicatedCount > clusterNodes.size() / 2) {
            commitIndex.set(Math.max(commitIndex.get(), entryIndex));
            applyCommittedEntries();
            future.complete(true);
        } else {
            future.complete(false);
        }
    }
    
    /**
     * Updates commit index based on majority replication
     */
    private void updateCommitIndex() {
        if (currentRole.get() != NodeRole.LEADER) {
            return;
        }
        
        // Find highest index replicated on majority
        for (int n = log.size(); n > commitIndex.get(); n--) {
            if (log.get(n - 1).getTerm() == currentTerm.get()) {
                int replicatedCount = 1; // Leader
                
                for (Integer matchIdx : matchIndex.values()) {
                    if (matchIdx >= n) {
                        replicatedCount++;
                    }
                }
                
                if (replicatedCount > clusterNodes.size() / 2) {
                    commitIndex.set(n);
                    applyCommittedEntries();
                    break;
                }
            }
        }
    }
    
    /**
     * Applies committed log entries to state machine
     */
    private void applyCommittedEntries() {
        while (lastApplied.get() < commitIndex.get()) {
            int applyIndex = lastApplied.incrementAndGet();
            LogEntry entry = log.get(applyIndex - 1);
            applyLogEntry(entry);
        }
    }
    
    /**
     * Applies a single log entry to the state machine
     */
    private void applyLogEntry(LogEntry entry) {
        // Execute the command
        System.out.println("Applying log entry: " + entry.getCommand() + 
                         " at index " + entry.getIndex());
        // Actual state machine application would go here
    }
    
    /**
     * Checks if candidate's log is at least as up-to-date as ours
     */
    private boolean isLogUpToDate(int lastLogIndex, long lastLogTerm) {
        if (log.isEmpty()) {
            return true;
        }
        
        LogEntry lastEntry = log.get(log.size() - 1);
        if (lastLogTerm > lastEntry.getTerm()) {
            return true;
        }
        if (lastLogTerm == lastEntry.getTerm() && lastLogIndex >= log.size()) {
            return true;
        }
        return false;
    }
    
    /**
     * Appends entries from leader
     */
    private boolean appendEntries(AppendEntriesRequest request) {
        // Remove conflicting entries
        int i = request.getPrevLogIndex();
        for (LogEntry entry : request.getEntries()) {
            if (i < log.size() && log.get(i).getTerm() != entry.getTerm()) {
                // Remove this and all following entries
                while (log.size() > i) {
                    log.remove(log.size() - 1);
                }
            }
            
            if (i >= log.size()) {
                log.add(entry);
            }
            i++;
        }
        
        return true;
    }
    
    /**
     * Resets election timeout
     */
    private void resetElectionTimer() {
        if (electionTimer != null) {
            electionTimer.cancel(false);
        }
        
        int timeout = MIN_ELECTION_TIMEOUT_MS + 
                     random.nextInt(MAX_ELECTION_TIMEOUT_MS - MIN_ELECTION_TIMEOUT_MS);
        
        electionTimer = scheduler.schedule(
            this::handleElectionTimeout,
            timeout,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Handles election timeout
     */
    private void handleElectionTimeout() {
        if (currentRole.get() != NodeRole.LEADER) {
            startElection();
        }
    }
    
    /**
     * Counts votes in current election
     */
    private int countVotes() {
        // Implementation would track actual votes received
        return 1; // Self vote
    }
    
    /**
     * Initializes next and match indices
     */
    private void initializeIndices() {
        for (String node : clusterNodes) {
            if (!node.equals(nodeId)) {
                nextIndex.put(node, 1);
                matchIndex.put(node, 0);
            }
        }
    }
    
    /**
     * Starts message processing thread
     */
    private void startMessageProcessor() {
        scheduler.submit(() -> {
            while (isRunning) {
                try {
                    RaftMessage message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        processMessage(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * Sends message to another node
     */
    private void sendMessage(String targetNode, RaftMessage message) {
        // Network communication implementation
        System.out.println("Sending " + message.getType() + " to " + targetNode);
    }
    
    /**
     * Forwards command to leader
     */
    private void forwardToLeader(String leader, String command, Map<String, Object> data) {
        // Forward implementation
        System.out.println("Forwarding command to leader: " + leader);
    }
    
    /**
     * Shuts down the consensus module
     */
    public void shutdown() {
        isRunning = false;
        if (electionTimer != null) {
            electionTimer.cancel(false);
        }
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel(false);
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    // Getters for monitoring
    public NodeRole getCurrentRole() { return currentRole.get(); }
    public String getCurrentLeader() { return currentLeader.get(); }
    public long getCurrentTerm() { return currentTerm.get(); }
    public int getCommitIndex() { return commitIndex.get(); }
    public int getLogSize() { return log.size(); }
    
    // Inner classes
    private enum NodeRole {
        FOLLOWER, CANDIDATE, LEADER
    }
    
    private enum MessageType {
        REQUEST_VOTE, VOTE_RESPONSE, APPEND_ENTRIES, APPEND_ENTRIES_RESPONSE
    }
    
    private static class RaftMessage {
        private final MessageType type;
        private final String senderId;
        private final Object payload;
        
        public RaftMessage(MessageType type, String senderId, Object payload) {
            this.type = type;
            this.senderId = senderId;
            this.payload = payload;
        }
        
        public MessageType getType() { return type; }
        public String getSenderId() { return senderId; }
        public Object getPayload() { return payload; }
    }
    
    private static class LogEntry {
        private final int index;
        private final long term;
        private final String command;
        private final Map<String, Object> data;
        private final Instant timestamp;
        
        public LogEntry(int index, long term, String command, 
                       Map<String, Object> data, Instant timestamp) {
            this.index = index;
            this.term = term;
            this.command = command;
            this.data = data;
            this.timestamp = timestamp;
        }
        
        public int getIndex() { return index; }
        public long getTerm() { return term; }
        public String getCommand() { return command; }
        public Map<String, Object> getData() { return data; }
    }
    
    private static class RequestVoteRequest {
        private final long term;
        private final String candidateId;
        private final int lastLogIndex;
        private final long lastLogTerm;
        
        public RequestVoteRequest(long term, String candidateId, 
                                int lastLogIndex, long lastLogTerm) {
            this.term = term;
            this.candidateId = candidateId;
            this.lastLogIndex = lastLogIndex;
            this.lastLogTerm = lastLogTerm;
        }
        
        public long getTerm() { return term; }
        public String getCandidateId() { return candidateId; }
        public int getLastLogIndex() { return lastLogIndex; }
        public long getLastLogTerm() { return lastLogTerm; }
    }
    
    private static class RequestVoteResponse {
        private final long term;
        private final boolean voteGranted;
        
        public RequestVoteResponse(long term, boolean voteGranted) {
            this.term = term;
            this.voteGranted = voteGranted;
        }
        
        public long getTerm() { return term; }
        public boolean isVoteGranted() { return voteGranted; }
    }
    
    private static class AppendEntriesRequest {
        private final long term;
        private final String leaderId;
        private final int prevLogIndex;
        private final long prevLogTerm;
        private final List<LogEntry> entries;
        private final int leaderCommit;
        
        public AppendEntriesRequest(long term, String leaderId, int prevLogIndex,
                                  long prevLogTerm, List<LogEntry> entries, int leaderCommit) {
            this.term = term;
            this.leaderId = leaderId;
            this.prevLogIndex = prevLogIndex;
            this.prevLogTerm = prevLogTerm;
            this.entries = new ArrayList<>(entries);
            this.leaderCommit = leaderCommit;
        }
        
        public long getTerm() { return term; }
        public String getLeaderId() { return leaderId; }
        public int getPrevLogIndex() { return prevLogIndex; }
        public long getPrevLogTerm() { return prevLogTerm; }
        public List<LogEntry> getEntries() { return entries; }
        public int getLeaderCommit() { return leaderCommit; }
    }
    
    private static class AppendEntriesResponse {
        private final long term;
        private final boolean success;
        private final int matchIndex;
        
        public AppendEntriesResponse(long term, boolean success, int matchIndex) {
            this.term = term;
            this.success = success;
            this.matchIndex = matchIndex;
        }
        
        public long getTerm() { return term; }
        public boolean isSuccess() { return success; }
        public int getMatchIndex() { return matchIndex; }
    }
}
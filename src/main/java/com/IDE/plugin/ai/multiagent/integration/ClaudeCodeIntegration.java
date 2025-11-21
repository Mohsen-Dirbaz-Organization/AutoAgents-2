/**
 * Integration layer for Claude Code interactions.
 * Provides methods for agents to interact with Claude Code for code generation,
 * analysis, and review.
 */
package com.IDE.plugin.ai.multiagent.integration;

import com.IDE.plugin.ai.multiagent.agent.Agent;
import com.IDE.plugin.ai.multiagent.agent.AgentRole;
import com.IDE.plugin.services.AIService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Integration layer for Claude Code interactions.
 */
public class ClaudeCodeIntegration {
    private static final Logger LOG = Logger.getInstance(ClaudeCodeIntegration.class);
    
    private final AIService aiService;
    private final Map<String, RequestContext> pendingRequests;
    private final Map<AgentRole, String> rolePromptTemplates;
    
    public ClaudeCodeIntegration() {
        this.aiService = ApplicationManager.getApplication().getService(AIService.class);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.rolePromptTemplates = initializeRolePromptTemplates();
    }
    
    /**
     * Requests code generation from Claude.
     * 
     * @param agent The agent making the request
     * @param prompt The prompt for code generation
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the generated code
     */
    public CompletableFuture<String> generateCode(@NotNull Agent agent, @NotNull String prompt, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agent.getId() + " requesting code generation");
        
        // Create request context
        String requestId = generateRequestId(agent.getId());
        RequestContext context = new RequestContext(requestId, agent.getId(), RequestType.CODE_GENERATION);
        pendingRequests.put(requestId, context);
        
        // Enhance prompt with agent role context
        String enhancedPrompt = enhancePromptWithAgentContext(prompt, agent.getRole(), options);
        
        // Submit request to Claude
        return CompletableFuture.supplyAsync(() -> {
            try {
                String model = options.containsKey("model") ? options.get("model").toString() : "Claude";
                String result = aiService.generateCode(enhancedPrompt, model);
                
                // Remove request from pending
                pendingRequests.remove(requestId);
                
                return result;
            } catch (Exception e) {
                LOG.error("Error generating code", e);
                pendingRequests.remove(requestId);
                throw new RuntimeException("Code generation failed", e);
            }
        });
    }
    
    /**
     * Requests code analysis from Claude.
     * 
     * @param agent The agent making the request
     * @param code The code to analyze
     * @param analysisType The type of analysis to perform
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the analysis result
     */
    public CompletableFuture<String> analyzeCode(@NotNull Agent agent, @NotNull String code, @NotNull String analysisType, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agent.getId() + " requesting code analysis of type " + analysisType);
        
        // Create request context
        String requestId = generateRequestId(agent.getId());
        RequestContext context = new RequestContext(requestId, agent.getId(), RequestType.CODE_ANALYSIS);
        pendingRequests.put(requestId, context);
        
        // Create analysis prompt
        String analysisPrompt = createAnalysisPrompt(code, analysisType, agent.getRole(), options);
        
        // Submit request to Claude
        return CompletableFuture.supplyAsync(() -> {
            try {
                String model = options.containsKey("model") ? options.get("model").toString() : "Claude";
                String result = aiService.generateCode(analysisPrompt, model);
                
                // Remove request from pending
                pendingRequests.remove(requestId);
                
                return result;
            } catch (Exception e) {
                LOG.error("Error analyzing code", e);
                pendingRequests.remove(requestId);
                throw new RuntimeException("Code analysis failed", e);
            }
        });
    }
    
    /**
     * Requests code review from Claude.
     * 
     * @param agent The agent making the request
     * @param code The code to review
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the review result
     */
    public CompletableFuture<String> reviewCode(@NotNull Agent agent, @NotNull String code, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agent.getId() + " requesting code review");
        
        // Create request context
        String requestId = generateRequestId(agent.getId());
        RequestContext context = new RequestContext(requestId, agent.getId(), RequestType.CODE_REVIEW);
        pendingRequests.put(requestId, context);
        
        // Create review prompt
        String reviewPrompt = createReviewPrompt(code, agent.getRole(), options);
        
        // Submit request to Claude
        return CompletableFuture.supplyAsync(() -> {
            try {
                String model = options.containsKey("model") ? options.get("model").toString() : "Claude";
                String result = aiService.generateCode(reviewPrompt, model);
                
                // Remove request from pending
                pendingRequests.remove(requestId);
                
                return result;
            } catch (Exception e) {
                LOG.error("Error reviewing code", e);
                pendingRequests.remove(requestId);
                throw new RuntimeException("Code review failed", e);
            }
        });
    }
    
    /**
     * Gets the status of a pending request.
     * 
     * @param requestId The ID of the request
     * @return The request context, or null if the request is not found
     */
    @Nullable
    public RequestContext getRequestStatus(@NotNull String requestId) {
        return pendingRequests.get(requestId);
    }
    
    /**
     * Cancels a pending request.
     * 
     * @param requestId The ID of the request to cancel
     * @return True if the request was cancelled, false otherwise
     */
    public boolean cancelRequest(@NotNull String requestId) {
        RequestContext context = pendingRequests.remove(requestId);
        return context != null;
    }
    
    /**
     * Enhances a prompt with agent context.
     */
    private String enhancePromptWithAgentContext(@NotNull String prompt, @NotNull AgentRole role, @NotNull Map<String, Object> options) {
        String template = rolePromptTemplates.getOrDefault(role, DEFAULT_PROMPT_TEMPLATE);
        
        // Replace placeholders in template
        String enhancedPrompt = template
            .replace("{{ROLE}}", role.getDisplayName())
            .replace("{{ROLE_DESCRIPTION}}", role.getDescription())
            .replace("{{PROMPT}}", prompt);
        
        // Add additional context from options
        if (options.containsKey("context")) {
            enhancedPrompt += "\n\nAdditional Context:\n" + options.get("context");
        }
        
        return enhancedPrompt;
    }
    
    /**
     * Creates an analysis prompt.
     */
    private String createAnalysisPrompt(@NotNull String code, @NotNull String analysisType, @NotNull AgentRole role, @NotNull Map<String, Object> options) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("As a ").append(role.getDisplayName()).append(", please analyze the following code");
        
        if (analysisType.equals("security")) {
            promptBuilder.append(" for security vulnerabilities and potential issues");
        } else if (analysisType.equals("performance")) {
            promptBuilder.append(" for performance bottlenecks and optimization opportunities");
        } else if (analysisType.equals("quality")) {
            promptBuilder.append(" for code quality, readability, and maintainability");
        } else {
            promptBuilder.append(" and provide a comprehensive analysis");
        }
        
        // Add specific instructions from options
        if (options.containsKey("instructions")) {
            promptBuilder.append("\n\nSpecific instructions: ").append(options.get("instructions"));
        }
        
        // Add code
        promptBuilder.append("\n\nCode to analyze:\n```\n").append(code).append("\n```");
        
        // Add output format instructions
        promptBuilder.append("\n\nPlease provide your analysis in a structured format with sections for issues found, recommendations, and a summary.");
        
        return promptBuilder.toString();
    }
    
    /**
     * Creates a review prompt.
     */
    private String createReviewPrompt(@NotNull String code, @NotNull AgentRole role, @NotNull Map<String, Object> options) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("As a ").append(role.getDisplayName()).append(", please review the following code");
        
        // Add specific focus areas from options
        if (options.containsKey("focus")) {
            promptBuilder.append(" with a focus on ").append(options.get("focus"));
        }
        
        // Add specific instructions from options
        if (options.containsKey("instructions")) {
            promptBuilder.append("\n\nSpecific instructions: ").append(options.get("instructions"));
        }
        
        // Add code
        promptBuilder.append("\n\nCode to review:\n```\n").append(code).append("\n```");
        
        // Add output format instructions
        promptBuilder.append("\n\nPlease provide your review in a structured format with sections for issues found, suggestions for improvement, and positive aspects of the code.");
        
        return promptBuilder.toString();
    }
    
    /**
     * Generates a unique request ID.
     */
    private String generateRequestId(String agentId) {
        return agentId + "-" + System.currentTimeMillis() + "-" + Math.abs(new java.util.Random().nextInt(1000));
    }
    
    /**
     * Initializes role-specific prompt templates.
     */
    private Map<AgentRole, String> initializeRolePromptTemplates() {
        Map<AgentRole, String> templates = new HashMap<>();
        
        templates.put(AgentRole.ARCHITECT, "You are an AI assistant acting as a Software Architect. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.CODE_EDITOR, "You are an AI assistant acting as a Code Editor. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.OBSERVER, "You are an AI assistant acting as a System Observer. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.ANALYZER, "You are an AI assistant acting as a Code Analyzer. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.TESTER, "You are an AI assistant acting as a Software Tester. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.DOCUMENTER, "You are an AI assistant acting as a Technical Documenter. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.SECURITY, "You are an AI assistant acting as a Security Specialist. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        templates.put(AgentRole.OPTIMIZER, "You are an AI assistant acting as a Performance Optimizer. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}");
        
        return templates;
    }
    
    private static final String DEFAULT_PROMPT_TEMPLATE = "You are an AI assistant acting as a {{ROLE}}. Your role is to {{ROLE_DESCRIPTION}}.\n\n{{PROMPT}}";
    
    /**
     * Types of requests that can be made to Claude.
     */
    public enum RequestType {
        CODE_GENERATION,
        CODE_ANALYSIS,
        CODE_REVIEW
    }
    
    /**
     * Context for a request to Claude.
     */
    public static class RequestContext {
        private final String requestId;
        private final String agentId;
        private final RequestType type;
        private final long timestamp;
        
        public RequestContext(String requestId, String agentId, RequestType type) {
            this.requestId = requestId;
            this.agentId = agentId;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getRequestId() {
            return requestId;
        }
        
        public String getAgentId() {
            return agentId;
        }
        
        public RequestType getType() {
            return type;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - timestamp;
        }
    }
}
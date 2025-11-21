package com.IDE.plugin.ai.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles and processes Claude responses.
 * Extracts actionable items, code snippets, and integrates results back into the project.
 */
public class ClaudeResponseHandler {
    private static final Logger LOG = Logger.getInstance(ClaudeResponseHandler.class);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("(?:File:|file:)\\s*([^\\s]+\\.[a-zA-Z]+)");
    
    private final Project project;
    private final Map<String, ResponseProcessor> processors;
    private final Map<String, ProcessingResult> resultCache;
    private volatile boolean initialized = false;
    
    /**
     * Interface for response processors
     */
    private interface ResponseProcessor {
        boolean canProcess(String responseType);
        ProcessingResult process(ClaudeResponse response, Map<String, Object> context);
    }
    
    /**
     * Result of response processing
     */
    public static class ProcessingResult {
        private final String id;
        private final boolean success;
        private final List<Action> actions;
        private final Map<String, Object> metadata;
        
        public ProcessingResult(String id, boolean success) {
            this.id = id;
            this.success = success;
            this.actions = new ArrayList<>();
            this.metadata = new HashMap<>();
        }
        
        public void addAction(Action action) {
            actions.add(action);
        }
        
        public List<Action> getActions() {
            return Collections.unmodifiableList(actions);
        }
        
        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }
    
    /**
     * Represents an action to be taken based on response
     */
    public static class Action {
        private final ActionType type;
        private final Map<String, Object> parameters;
        
        public Action(ActionType type, Map<String, Object> parameters) {
            this.type = type;
            this.parameters = parameters;
        }
        
        public ActionType getType() {
            return type;
        }
        
        public Map<String, Object> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }
    }
    
    public enum ActionType {
        CREATE_FILE, MODIFY_FILE, DELETE_FILE, EXECUTE_COMMAND, 
        SHOW_NOTIFICATION, REQUEST_USER_INPUT, TRIGGER_AGENT_TASK
    }
    
    public ClaudeResponseHandler(@NotNull Project project) {
        this.project = project;
        this.processors = new HashMap<>();
        this.resultCache = new ConcurrentHashMap<>();
        registerDefaultProcessors();
    }
    
    /**
     * Initialize the response handler
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        LOG.info("Initializing Claude Response Handler");
        initialized = true;
    }
    
    /**
     * Process a Claude response
     */
    public ClaudeResponse processResponse(@NotNull ClaudeResponse response, 
                                        @NotNull Object context) throws Exception {
        // Extract processing context
        Map<String, Object> contextMap = extractContext(context);
        
        // Find appropriate processor
        ResponseProcessor processor = findProcessor(response.getType());
        if (processor == null) {
            // Use default processor
            processor = new DefaultResponseProcessor();
        }
        
        // Process response
        ProcessingResult result = processor.process(response, contextMap);
        
        // Cache result
        resultCache.put(response.getId(), result);
        
        // Execute actions
        executeActions(result.getActions());
        
        // Enhance response with processing results
        response.setProcessingResult(result);
        
        return response;
    }
    
    /**
     * Register default processors
     */
    private void registerDefaultProcessors() {
        processors.put("code_generation", new CodeGenerationProcessor());
        processors.put("code_review", new CodeReviewProcessor());
        processors.put("refactoring", new RefactoringProcessor());
        processors.put("documentation", new DocumentationProcessor());
        processors.put("tool_use", new ToolUseProcessor());
    }
    
    /**
     * Find processor for response type
     */
    private ResponseProcessor findProcessor(String responseType) {
        return processors.values().stream()
            .filter(p -> p.canProcess(responseType))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Extract context from object
     */
    private Map<String, Object> extractContext(Object context) {
        if (context instanceof Map) {
            return (Map<String, Object>) context;
        }
        
        // Extract from custom context object
        Map<String, Object> contextMap = new HashMap<>();
        if (context instanceof ClaudeCodeIntegrationService.TaskContext) {
            ClaudeCodeIntegrationService.TaskContext taskContext = 
                (ClaudeCodeIntegrationService.TaskContext) context;
            contextMap.put("task_id", taskContext.taskId);
            contextMap.put("agent_id", taskContext.agentId);
            contextMap.putAll(taskContext.metadata);
        }
        
        return contextMap;
    }
    
    /**
     * Execute actions from processing result
     */
    private void executeActions(List<Action> actions) {
        for (Action action : actions) {
            try {
                executeAction(action);
            } catch (Exception e) {
                LOG.error("Failed to execute action: " + action.getType(), e);
            }
        }
    }
    
    /**
     * Execute a single action
     */
    private void executeAction(Action action) {
        switch (action.getType()) {
            case CREATE_FILE:
                createFile(action.getParameters());
                break;
            case MODIFY_FILE:
                modifyFile(action.getParameters());
                break;
            case DELETE_FILE:
                deleteFile(action.getParameters());
                break;
            case EXECUTE_COMMAND:
                executeCommand(action.getParameters());
                break;
            case SHOW_NOTIFICATION:
                showNotification(action.getParameters());
                break;
            case REQUEST_USER_INPUT:
                requestUserInput(action.getParameters());
                break;
            case TRIGGER_AGENT_TASK:
                triggerAgentTask(action.getParameters());
                break;
        }
    }
    
    /**
     * Default response processor
     */
    private class DefaultResponseProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return true; // Can process any type
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            // Extract code blocks
            List<CodeBlock> codeBlocks = extractCodeBlocks(response.getContent());
            result.setMetadata("code_blocks", codeBlocks);
            
            // Extract file references
            List<String> fileReferences = extractFileReferences(response.getContent());
            result.setMetadata("file_references", fileReferences);
            
            // Process tool calls if present
            if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
                processToolCalls(response.getToolCalls(), result);
            }
            
            return result;
        }
    }
    
    /**
     * Code generation processor
     */
    private class CodeGenerationProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return "text".equals(responseType); // Claude returns text for code generation
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            // Extract generated code
            List<CodeBlock> codeBlocks = extractCodeBlocks(response.getContent());
            
            if (!codeBlocks.isEmpty()) {
                // Determine target file
                String targetFile = (String) context.get("target_file");
                if (targetFile != null) {
                    // Create action to write code
                    result.addAction(new Action(
                        ActionType.CREATE_FILE,
                        Map.of(
                            "path", targetFile,
                            "content", codeBlocks.get(0).getCode(),
                            "language", codeBlocks.get(0).getLanguage()
                        )
                    ));
                }
                
                result.setMetadata("generated_code", codeBlocks);
            }
            
            return result;
        }
    }
    
    /**
     * Code review processor
     */
    private class CodeReviewProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return "text".equals(responseType);
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            // Parse review comments
            List<ReviewComment> comments = parseReviewComments(response.getContent());
            result.setMetadata("review_comments", comments);
            
            // Create notifications for critical issues
            comments.stream()
                .filter(comment -> comment.getSeverity() == Severity.CRITICAL)
                .forEach(comment -> result.addAction(new Action(
                    ActionType.SHOW_NOTIFICATION,
                    Map.of(
                        "title", "Critical Issue Found",
                        "message", comment.getMessage(),
                        "type", "ERROR"
                    )
                )));
            
            return result;
        }
    }
    
    /**
     * Refactoring processor
     */
    private class RefactoringProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return "text".equals(responseType);
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            // Extract refactored code
            List<CodeBlock> codeBlocks = extractCodeBlocks(response.getContent());
            
            if (!codeBlocks.isEmpty()) {
                String targetFile = (String) context.get("target_file");
                if (targetFile != null) {
                    result.addAction(new Action(
                        ActionType.MODIFY_FILE,
                        Map.of(
                            "path", targetFile,
                            "content", codeBlocks.get(0).getCode(),
                            "backup", true
                        )
                    ));
                }
            }
            
            return result;
        }
    }
    
    /**
     * Documentation processor
     */
    private class DocumentationProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return "text".equals(responseType);
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            // Extract documentation
            String docs = response.getContent();
            result.setMetadata("documentation", docs);
            
            // Determine where to save documentation
            String targetFile = (String) context.get("doc_file");
            if (targetFile != null) {
                result.addAction(new Action(
                    ActionType.CREATE_FILE,
                    Map.of(
                        "path", targetFile,
                        "content", docs,
                        "type", "documentation"
                    )
                ));
            }
            
            return result;
        }
    }
    
    /**
     * Tool use processor
     */
    private class ToolUseProcessor implements ResponseProcessor {
        @Override
        public boolean canProcess(String responseType) {
            return "tool_use".equals(responseType);
        }
        
        @Override
        public ProcessingResult process(ClaudeResponse response, Map<String, Object> context) {
            ProcessingResult result = new ProcessingResult(response.getId(), true);
            
            if (response.getToolCalls() != null) {
                processToolCalls(response.getToolCalls(), result);
            }
            
            return result;
        }
    }
    
    /**
     * Process tool calls
     */
    private void processToolCalls(List<ToolCall> toolCalls, ProcessingResult result) {
        for (ToolCall call : toolCalls) {
            switch (call.getName()) {
                case "generate_code":
                    processCodeGenerationTool(call, result);
                    break;
                case "execute_command":
                    processCommandExecutionTool(call, result);
                    break;
                case "search_code":
                    processCodeSearchTool(call, result);
                    break;
                default:
                    LOG.warn("Unknown tool: " + call.getName());
            }
        }
    }
    
    /**
     * Process code generation tool call
     */
    private void processCodeGenerationTool(ToolCall call, ProcessingResult result) {
        Map<String, Object> args = call.getArguments();
        String code = (String) args.get("code");
        String language = (String) args.get("language");
        
        if (code != null) {
            result.setMetadata("generated_code", new CodeBlock(code, language));
        }
    }
    
    /**
     * Process command execution tool call
     */
    private void processCommandExecutionTool(ToolCall call, ProcessingResult result) {
        Map<String, Object> args = call.getArguments();
        String command = (String) args.get("command");
        
        if (command != null) {
            result.addAction(new Action(
                ActionType.EXECUTE_COMMAND,
                Map.of("command", command)
            ));
        }
    }
    
    /**
     * Process code search tool call
     */
    private void processCodeSearchTool(ToolCall call, ProcessingResult result) {
        Map<String, Object> args = call.getArguments();
        String query = (String) args.get("query");
        
        if (query != null) {
            result.addAction(new Action(
                ActionType.TRIGGER_AGENT_TASK,
                Map.of(
                    "task_type", "code_search",
                    "query", query
                )
            ));
        }
    }
    
    /**
     * Extract code blocks from content
     */
    private List<CodeBlock> extractCodeBlocks(String content) {
        List<CodeBlock> blocks = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String language = matcher.group(1);
            String code = matcher.group(2);
            blocks.add(new CodeBlock(code, language));
        }
        
        return blocks;
    }
    
    /**
     * Extract file references from content
     */
    private List<String> extractFileReferences(String content) {
        List<String> references = new ArrayList<>();
        Matcher matcher = FILE_PATH_PATTERN.matcher(content);
        
        while (matcher.find()) {
            references.add(matcher.group(1));
        }
        
        return references;
    }
    
    /**
     * Parse review comments from content
     */
    private List<ReviewComment> parseReviewComments(String content) {
        // Simple parsing logic - could be enhanced
        List<ReviewComment> comments = new ArrayList<>();
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("ERROR:") || line.contains("CRITICAL:")) {
                comments.add(new ReviewComment(line, Severity.CRITICAL));
            } else if (line.contains("WARNING:")) {
                comments.add(new ReviewComment(line, Severity.WARNING));
            } else if (line.contains("INFO:") || line.contains("SUGGESTION:")) {
                comments.add(new ReviewComment(line, Severity.INFO));
            }
        }
        
        return comments;
    }
    
    // Action implementation methods
    
    private void createFile(Map<String, Object> parameters) {
        // Implementation would create file in project
        LOG.info("Creating file: " + parameters.get("path"));
    }
    
    private void modifyFile(Map<String, Object> parameters) {
        // Implementation would modify existing file
        LOG.info("Modifying file: " + parameters.get("path"));
    }
    
    private void deleteFile(Map<String, Object> parameters) {
        // Implementation would delete file
        LOG.info("Deleting file: " + parameters.get("path"));
    }
    
    private void executeCommand(Map<String, Object> parameters) {
        // Implementation would execute command
        LOG.info("Executing command: " + parameters.get("command"));
    }
    
    private void showNotification(Map<String, Object> parameters) {
        // Implementation would show notification
        LOG.info("Showing notification: " + parameters.get("message"));
    }
    
    private void requestUserInput(Map<String, Object> parameters) {
        // Implementation would request user input
        LOG.info("Requesting user input: " + parameters.get("prompt"));
    }
    
    private void triggerAgentTask(Map<String, Object> parameters) {
        // Implementation would trigger new agent task
        LOG.info("Triggering agent task: " + parameters.get("task_type"));
    }
    
    /**
     * Helper classes
     */
    private static class CodeBlock {
        private final String code;
        private final String language;
        
        public CodeBlock(String code, String language) {
            this.code = code;
            this.language = language;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getLanguage() {
            return language;
        }
    }
    
    private static class ReviewComment {
        private final String message;
        private final Severity severity;
        
        public ReviewComment(String message, Severity severity) {
            this.message = message;
            this.severity = severity;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Severity getSeverity() {
            return severity;
        }
    }
    
    private enum Severity {
        INFO, WARNING, CRITICAL
    }
}
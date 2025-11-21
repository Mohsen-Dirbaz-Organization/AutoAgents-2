package com.IDE.plugin.ai.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.application.ReadAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapts agent tasks for Claude Code processing.
 * Converts multi-agent system tasks into Claude-compatible requests.
 */
public class ClaudeTaskAdapter {
    private static final Logger LOG = Logger.getInstance(ClaudeTaskAdapter.class);
    private static final int MAX_CONTEXT_FILES = 10;
    private static final int MAX_FILE_SIZE = 100_000; // 100KB
    
    private final Project project;
    private final Map<String, TaskAdapter> adapters;
    private volatile boolean initialized = false;
    
    /**
     * Interface for specific task adapters
     */
    private interface TaskAdapter {
        boolean canHandle(String taskType);
        ClaudeRequest adapt(String taskId, Map<String, Object> taskData);
    }
    
    public ClaudeTaskAdapter(@NotNull Project project) {
        this.project = project;
        this.adapters = new HashMap<>();
        registerDefaultAdapters();
    }
    
    /**
     * Initialize the task adapter
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        LOG.info("Initializing Claude Task Adapter");
        initialized = true;
    }
    
    /**
     * Adapt a task for Claude processing
     */
    public ClaudeRequest adaptTask(@NotNull String taskId, @NotNull Map<String, Object> taskData) {
        String taskType = (String) taskData.get("task_type");
        if (taskType == null) {
            throw new IllegalArgumentException("Task type not specified");
        }
        
        // Find appropriate adapter
        TaskAdapter adapter = findAdapter(taskType);
        if (adapter == null) {
            // Use generic adapter
            return createGenericRequest(taskId, taskData);
        }
        
        return adapter.adapt(taskId, taskData);
    }
    
    /**
     * Register default task adapters
     */
    private void registerDefaultAdapters() {
        // Code generation adapter
        adapters.put("code_generation", new CodeGenerationAdapter());
        
        // Code review adapter
        adapters.put("code_review", new CodeReviewAdapter());
        
        // Refactoring adapter
        adapters.put("refactoring", new RefactoringAdapter());
        
        // Documentation adapter
        adapters.put("documentation", new DocumentationAdapter());
        
        // Bug fix adapter
        adapters.put("bug_fix", new BugFixAdapter());
        
        // Test generation adapter
        adapters.put("test_generation", new TestGenerationAdapter());
    }
    
    /**
     * Find adapter for task type
     */
    @Nullable
    private TaskAdapter findAdapter(String taskType) {
        return adapters.values().stream()
            .filter(adapter -> adapter.canHandle(taskType))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Create generic Claude request
     */
    private ClaudeRequest createGenericRequest(String taskId, Map<String, Object> taskData) {
        ClaudeRequest request = new ClaudeRequest();
        request.setId(UUID.randomUUID().toString());
        request.setTaskId(taskId);
        request.setModel("claude-3-opus-20240229");
        request.setMaxTokens(4000);
        request.setTemperature(0.7);
        
        // Build prompt
        String prompt = buildGenericPrompt(taskData);
        request.setContent(prompt);
        
        // Add context if available
        addContextToRequest(request, taskData);
        
        return request;
    }
    
    /**
     * Build generic prompt from task data
     */
    private String buildGenericPrompt(Map<String, Object> taskData) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Task: ").append(taskData.get("task_type")).append("\n\n");
        
        if (taskData.containsKey("description")) {
            prompt.append("Description: ").append(taskData.get("description")).append("\n\n");
        }
        
        if (taskData.containsKey("requirements")) {
            prompt.append("Requirements:\n");
            List<String> requirements = (List<String>) taskData.get("requirements");
            requirements.forEach(req -> prompt.append("- ").append(req).append("\n"));
            prompt.append("\n");
        }
        
        if (taskData.containsKey("constraints")) {
            prompt.append("Constraints:\n");
            Map<String, Object> constraints = (Map<String, Object>) taskData.get("constraints");
            constraints.forEach((key, value) -> 
                prompt.append("- ").append(key).append(": ").append(value).append("\n"));
            prompt.append("\n");
        }
        
        return prompt.toString();
    }
    
    /**
     * Add context files to request
     */
    private void addContextToRequest(ClaudeRequest request, Map<String, Object> taskData) {
        List<String> contextFiles = (List<String>) taskData.get("context_files");
        if (contextFiles == null || contextFiles.isEmpty()) {
            return;
        }
        
        List<ConversationMessage> contextMessages = new ArrayList<>();
        
        ReadAction.run(() -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            
            contextFiles.stream()
                .limit(MAX_CONTEXT_FILES)
                .forEach(filePath -> {
                    VirtualFile file = project.getBaseDir().findFileByRelativePath(filePath);
                    if (file != null && file.isValid() && file.getLength() < MAX_FILE_SIZE) {
                        PsiFile psiFile = psiManager.findFile(file);
                        if (psiFile != null) {
                            String content = psiFile.getText();
                            ConversationMessage msg = new ConversationMessage();
                            msg.setRole("system");
                            msg.setContent("File: " + filePath + "\n```\n" + content + "\n```");
                            contextMessages.add(msg);
                        }
                    }
                });
        });
        
        request.setConversationHistory(contextMessages);
    }
    
    /**
     * Code generation adapter
     */
    private class CodeGenerationAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "code_generation".equals(taskType) || 
                   "generate_code".equals(taskType) ||
                   "implement_feature".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            // Customize for code generation
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate code for the following requirement:\n\n");
            prompt.append(taskData.get("description")).append("\n\n");
            
            // Add language preference
            String language = (String) taskData.get("language");
            if (language != null) {
                prompt.append("Language: ").append(language).append("\n");
            }
            
            // Add framework/library requirements
            List<String> frameworks = (List<String>) taskData.get("frameworks");
            if (frameworks != null && !frameworks.isEmpty()) {
                prompt.append("Frameworks/Libraries: ").append(String.join(", ", frameworks)).append("\n");
            }
            
            prompt.append("\nPlease provide clean, well-documented code following best practices.");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are an expert software developer. Generate high-quality, production-ready code.");
            
            // Add code generation tools
            request.addTool(createCodeGenerationTool());
            
            return request;
        }
    }
    
    /**
     * Code review adapter
     */
    private class CodeReviewAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "code_review".equals(taskType) || 
                   "review_code".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Please review the following code:\n\n");
            
            String code = (String) taskData.get("code");
            if (code != null) {
                prompt.append("```\n").append(code).append("\n```\n\n");
            }
            
            prompt.append("Focus on:\n");
            prompt.append("1. Code quality and best practices\n");
            prompt.append("2. Potential bugs or issues\n");
            prompt.append("3. Performance optimizations\n");
            prompt.append("4. Security vulnerabilities\n");
            prompt.append("5. Maintainability and readability\n");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are a senior code reviewer. Provide thorough, constructive feedback.");
            
            return request;
        }
    }
    
    /**
     * Refactoring adapter
     */
    private class RefactoringAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "refactoring".equals(taskType) || 
                   "refactor_code".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Refactor the following code:\n\n");
            
            String code = (String) taskData.get("code");
            if (code != null) {
                prompt.append("```\n").append(code).append("\n```\n\n");
            }
            
            String refactoringType = (String) taskData.get("refactoring_type");
            if (refactoringType != null) {
                prompt.append("Refactoring type: ").append(refactoringType).append("\n\n");
            }
            
            prompt.append("Ensure the refactored code:\n");
            prompt.append("- Maintains the same functionality\n");
            prompt.append("- Follows SOLID principles\n");
            prompt.append("- Is more maintainable and readable\n");
            prompt.append("- Has better performance if possible\n");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are an expert in code refactoring and clean code practices.");
            
            return request;
        }
    }
    
    /**
     * Documentation adapter
     */
    private class DocumentationAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "documentation".equals(taskType) || 
                   "generate_docs".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate documentation for:\n\n");
            
            String code = (String) taskData.get("code");
            if (code != null) {
                prompt.append("```\n").append(code).append("\n```\n\n");
            }
            
            String docType = (String) taskData.get("doc_type");
            if (docType != null) {
                prompt.append("Documentation type: ").append(docType).append("\n");
            }
            
            prompt.append("\nInclude:\n");
            prompt.append("- Clear descriptions\n");
            prompt.append("- Parameter explanations\n");
            prompt.append("- Return value descriptions\n");
            prompt.append("- Usage examples\n");
            prompt.append("- Any important notes or warnings\n");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are a technical documentation expert. Create clear, comprehensive documentation.");
            
            return request;
        }
    }
    
    /**
     * Bug fix adapter
     */
    private class BugFixAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "bug_fix".equals(taskType) || 
                   "fix_bug".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Fix the following bug:\n\n");
            
            String bugDescription = (String) taskData.get("bug_description");
            if (bugDescription != null) {
                prompt.append("Bug description: ").append(bugDescription).append("\n\n");
            }
            
            String code = (String) taskData.get("code");
            if (code != null) {
                prompt.append("Code with bug:\n```\n").append(code).append("\n```\n\n");
            }
            
            String errorMessage = (String) taskData.get("error_message");
            if (errorMessage != null) {
                prompt.append("Error message: ").append(errorMessage).append("\n\n");
            }
            
            prompt.append("Provide:\n");
            prompt.append("1. Root cause analysis\n");
            prompt.append("2. Fixed code\n");
            prompt.append("3. Explanation of the fix\n");
            prompt.append("4. How to prevent similar bugs\n");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are a debugging expert. Identify and fix bugs effectively.");
            
            return request;
        }
    }
    
    /**
     * Test generation adapter
     */
    private class TestGenerationAdapter implements TaskAdapter {
        @Override
        public boolean canHandle(String taskType) {
            return "test_generation".equals(taskType) || 
                   "generate_tests".equals(taskType);
        }
        
        @Override
        public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
            ClaudeRequest request = createGenericRequest(taskId, taskData);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate comprehensive tests for:\n\n");
            
            String code = (String) taskData.get("code");
            if (code != null) {
                prompt.append("```\n").append(code).append("\n```\n\n");
            }
            
            String testFramework = (String) taskData.get("test_framework");
            if (testFramework != null) {
                prompt.append("Test framework: ").append(testFramework).append("\n");
            }
            
            prompt.append("\nInclude:\n");
            prompt.append("- Unit tests for all methods\n");
            prompt.append("- Edge case testing\n");
            prompt.append("- Error handling tests\n");
            prompt.append("- Integration tests if applicable\n");
            prompt.append("- Clear test descriptions\n");
            
            request.setContent(prompt.toString());
            request.setSystemPrompt("You are a testing expert. Generate comprehensive, well-structured tests.");
            
            return request;
        }
    }
    
    /**
     * Create code generation tool
     */
    private Map<String, Object> createCodeGenerationTool() {
        return Map.of(
            "name", "generate_code",
            "description", "Generate code based on requirements",
            "parameters", Map.of(
                "type", "object",
                "properties", Map.of(
                    "code", Map.of("type", "string", "description", "Generated code"),
                    "language", Map.of("type", "string", "description", "Programming language"),
                    "explanation", Map.of("type", "string", "description", "Code explanation")
                )
            )
        );
    }
}
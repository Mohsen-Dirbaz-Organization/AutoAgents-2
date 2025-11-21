package com.IDE.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.IDE.plugin.settings.AutoAgentsSettings",
    storages = @Storage("AutoAgentsSettings.xml")
)
public class AutoAgentsSettings implements PersistentStateComponent<AutoAgentsSettings> {
    
    // API Configuration
    private String apiKey = "";
    private String apiEndpoint = "https://api.openai.com/v1/chat/completions";
    private String selectedModel = "GPT-4";
    private int maxTokens = 2048;
    private double temperature = 0.7;
    
    // UI Settings
    private boolean showNotifications = true;
    private boolean autoFormat = true;
    private boolean insertAtCursor = true;
    private String defaultPromptTemplate = "Generate {language} code that {description}";
    
    // Advanced Settings
    private int timeout = 30000; // 30 seconds
    private boolean useProxy = false;
    private String proxyHost = "";
    private int proxyPort = 8080;
    private boolean enableLogging = false;
    private String logLevel = "INFO";
    
    // Code Generation Settings
    private boolean includeComments = true;
    private boolean includeImports = true;
    private String codeStyle = "Standard";
    private boolean generateTests = false;
    private String testFramework = "JUnit";
    
    public static AutoAgentsSettings getInstance() {
        return ApplicationManager.getApplication().getService(AutoAgentsSettings.class);
    }
    
    @Nullable
    @Override
    public AutoAgentsSettings getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull AutoAgentsSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // Getters and Setters
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiEndpoint() {
        return apiEndpoint;
    }
    
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
    
    public String getSelectedModel() {
        return selectedModel;
    }
    
    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public boolean isShowNotifications() {
        return showNotifications;
    }
    
    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }
    
    public boolean isAutoFormat() {
        return autoFormat;
    }
    
    public void setAutoFormat(boolean autoFormat) {
        this.autoFormat = autoFormat;
    }
    
    public boolean isInsertAtCursor() {
        return insertAtCursor;
    }
    
    public void setInsertAtCursor(boolean insertAtCursor) {
        this.insertAtCursor = insertAtCursor;
    }
    
    public String getDefaultPromptTemplate() {
        return defaultPromptTemplate;
    }
    
    public void setDefaultPromptTemplate(String defaultPromptTemplate) {
        this.defaultPromptTemplate = defaultPromptTemplate;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public boolean isUseProxy() {
        return useProxy;
    }
    
    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }
    
    public String getProxyHost() {
        return proxyHost;
    }
    
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    
    public int getProxyPort() {
        return proxyPort;
    }
    
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
    
    public boolean isEnableLogging() {
        return enableLogging;
    }
    
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public boolean isIncludeComments() {
        return includeComments;
    }
    
    public void setIncludeComments(boolean includeComments) {
        this.includeComments = includeComments;
    }
    
    public boolean isIncludeImports() {
        return includeImports;
    }
    
    public void setIncludeImports(boolean includeImports) {
        this.includeImports = includeImports;
    }
    
    public String getCodeStyle() {
        return codeStyle;
    }
    
    public void setCodeStyle(String codeStyle) {
        this.codeStyle = codeStyle;
    }
    
    public boolean isGenerateTests() {
        return generateTests;
    }
    
    public void setGenerateTests(boolean generateTests) {
        this.generateTests = generateTests;
    }
    
    public String getTestFramework() {
        return testFramework;
    }
    
    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }
}
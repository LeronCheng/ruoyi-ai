package org.ruoyi.chat.service.chat.impl;

import io.github.imfangs.dify.client.DifyClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.DifyWorkflowClient;
import io.github.imfangs.dify.client.callback.ChatStreamCallback;
import io.github.imfangs.dify.client.callback.WorkflowStreamCallback;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.event.*;
import io.github.imfangs.dify.client.model.DifyConfig;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.workflow.WorkflowRunRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.chat.enums.ChatModeType;
import org.ruoyi.chat.service.chat.IChatService;
import org.ruoyi.common.chat.entity.chat.Message;
import org.ruoyi.common.chat.request.ChatRequest;
import org.ruoyi.domain.vo.ChatModelVo;
import org.ruoyi.service.IChatModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dify 聊天管理
 *
 * @author ageer
 */
@Service
@Slf4j
public class DifyWorkflowServiceImpl implements IChatService {

    @Autowired
    private IChatModelService chatModelService;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModelVo chatModelVo = chatModelService.selectModelByName(chatRequest.getModel());

        // 使用自定义配置创建客户端
        DifyConfig config = DifyConfig.builder()
                .baseUrl(chatModelVo.getApiHost())
                .apiKey(chatModelVo.getApiKey())
                .connectTimeout(5000)
                .readTimeout(60000)
                .writeTimeout(30000)
                .build();
        DifyWorkflowClient workflowClient = DifyClientFactory.createWorkflowClient(config);
        List<Message> messages = chatRequest.getMessages();
        Message message = messages.get(messages.size() - 1);
        // 创建工作流请求
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("model", "deepseek");
        inputs.put("input", message.getContent());
        inputs.put("style", "deepseek");

        WorkflowRunRequest request = WorkflowRunRequest.builder()
                .inputs(inputs)
                .responseMode(ResponseMode.STREAMING)
                .user("user-123")
                .build();

        // 发送流式消息
        try {
            workflowClient.runWorkflowStream(request, new WorkflowStreamCallback() {
                @Override
                public void onWorkflowStarted(WorkflowStartedEvent event) {
                    System.out.println("工作流开始: " + event);
                }

                @Override
                public void onNodeStarted(NodeStartedEvent event) {
                    System.out.println("节点开始: " + event);
                }

                @Override
                public void onNodeFinished(NodeFinishedEvent event) {
                    System.out.println("节点完成: " + event);
                }

                @Override
                public void onWorkflowFinished(WorkflowFinishedEvent event) {
                    emitter.complete();
                    System.out.println("工作流完成: " + event);
                }

                @Override
                public void onError(ErrorEvent event) {
                    System.err.println("错误: " + event.getMessage());
                }

                @Override
                public void onException(Throwable throwable) {
                    System.err.println("异常: " + throwable.getMessage());
                }

                @SneakyThrows
                @Override
                public void onWorkflowTextChunk(WorkflowTextChunkEvent event) {
                    emitter.send(event.getData().getText());
                    System.out.println("文本片段: " + event);
                }

            });
        } catch (Exception e) {
            log.error("dify请求失败：{}", e.getMessage());
        }

        return emitter;
    }

    @Override
    public String getCategory() {
        return ChatModeType.DIFY_WORKFLOW.getCode();
    }

}

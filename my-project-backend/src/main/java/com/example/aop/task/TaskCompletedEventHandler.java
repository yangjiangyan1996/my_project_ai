package com.example.aop.task;

import com.example.Facade.TaskFacade;
import com.example.entity.vo.TaskCompletedEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskCompletedEventHandler {
    @Resource
    private TaskFacade taskFacade;

    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        //TODO yang  搜【后续在看需要完成什么内容】
    }
}
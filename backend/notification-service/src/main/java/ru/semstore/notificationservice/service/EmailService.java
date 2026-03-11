package ru.semstore.notificationservice.service;

import java.util.Map;

public interface EmailService {
    void sendHtmlMessage(String to, String subject, String templateName, Map<String, Object> variables);

}

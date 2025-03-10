package com.mypaymentstestbot.my_payments_test_bot.service.webhook;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mypaymentstestbot.my_payments_test_bot.utils.AppProperties;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

import jakarta.annotation.PostConstruct;

@Component
public class WebhookManager {

	private static final Logger log = LoggerFactory.getLogger(WebhookManager.class);

	@Autowired
	private NgrokService ngrokService;
	@Autowired
	private TelegramBot bot;

	public WebhookManager() {
		log.info("WebhookManager create been");
	}

	@PostConstruct
	public void setWebhook() {
		// Получаем публичный url нашего локального сервера
		String publicUrl = ngrokService.startNgrok(8080);
		log.info("ngrok url: " + publicUrl);

		SetWebhook webhookRequest = new SetWebhook().url(publicUrl + "/" + AppProperties.endpointWebhook);
		// Установка вебхука
		bot.execute(webhookRequest, new Callback<SetWebhook, BaseResponse>() {
			@Override
			public void onResponse(SetWebhook request, BaseResponse response) {
				log.info("Webhook успешно установлен!");
			}

			@Override
			public void onFailure(SetWebhook request, IOException e) {
				log.info("Ошибка при установке вебхука: " + e.toString());
			}
		});
	}

	@EventListener(ContextClosedEvent.class)
	public void disroyWebhookAndNgrok() {
		ngrokService.stopNgrok();
	}
}

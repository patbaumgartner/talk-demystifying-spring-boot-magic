package com.fortytwotalents.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.context.event.EventListener;

/**
 * Listens for {@link AuditEvent}s on the Spring application event bus and writes a
 * structured, colour-highlighted log line.
 *
 * <p>
 * Colours are rendered via {@link AnsiOutput}, which honours the
 * {@code spring.output.ansi.enabled} property ({@code DETECT} by default). When ANSI is
 * disabled (e.g. in a CI log file) output falls back to plain text automatically.
 *
 * <p>
 * Replace this default with your own implementation – for example to route events to
 * Kafka, a database, or an external audit service – by declaring a bean of the same type.
 * Because it is registered with {@code @ConditionalOnMissingBean} your bean takes
 * precedence automatically.
 */
public class AuditEventListener {

	private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

	private final String topic;

	public AuditEventListener(String topic) {
		this.topic = topic;
	}

	@EventListener
	public void on(AuditEvent event) {
		String line = AnsiOutput.toString(AnsiStyle.BOLD, AnsiColor.CYAN, "▶ AUDIT ", AnsiStyle.NORMAL,
				AnsiColor.DEFAULT, "topic=", AnsiColor.YELLOW, topic, AnsiColor.DEFAULT, "  principal=",
				AnsiColor.GREEN, event.principal(), AnsiColor.DEFAULT, "  action=", AnsiColor.BLUE, event.action(),
				AnsiColor.DEFAULT, "  resource=", AnsiColor.MAGENTA, event.resourceId(), AnsiColor.DEFAULT);
		log.info(line);
	}

}

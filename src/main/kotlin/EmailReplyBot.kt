import ai.GptResponder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import email.EmailParser
import email.EmailTransport
import model.Configuration
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.mail.Message

/**
 * An email auto reply bot. Uses GPT to craft a relevant response.
 */
class EmailReplyBot {

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val config: Configuration
    private val emailTransport: EmailTransport
    private val gptResponder: GptResponder
    private val logger = LoggerFactory.getLogger(javaClass)

    // use a single threadpool; this won't scale to thousands of requests per minute, but that's not the intended
    // use case of this software in any case.
    private val executor = Executors.newFixedThreadPool(40)
    private val counter = AtomicInteger(0)

    init {
        val file = File(Thread.currentThread().contextClassLoader.getResource("application.yml")?.file
            ?: throw Exception("Could not find application.yml on classpath"))
        config = objectMapper.readValue(file, Configuration::class.java)
        emailTransport = EmailTransport(config)
        gptResponder = GptResponder(config)
    }

    fun listen() {
        while (true) {
            emailTransport.getEmail().forEach { message ->
                process(message)
            }
            logger.info("Total processed: ${counter.get()}")
            Thread.sleep(Duration.ofMinutes(1))
        }
    }

    fun process(message: EmailParser.EmailMessage) {
        executor.submit(Callable {
            if (message.replyTo == null || message.senderName == null) {
                logger.warn("Null properties on message: $message")
            } else {
                try {
                    emailTransport.sendEmail(message, gptResponder.reply(message))
                    counter.incrementAndGet()
                } catch (e: Exception) {
                    logger.error("Message sending failed:", e)
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EmailReplyBot().listen()
        }
    }
}
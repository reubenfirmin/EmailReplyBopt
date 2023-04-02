import ai.GptResponder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import email.EmailTransport
import model.Configuration
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

class EmailReplyBot {

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val config: Configuration
    private val emailTransport: EmailTransport
    private val gptResponder: GptResponder
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        val file = File(Thread.currentThread().contextClassLoader.getResource("application.yml")?.file
            ?: throw Exception("Could not find application.yml on classpath"))
        config = objectMapper.readValue(file, Configuration::class.java)
        emailTransport = EmailTransport(config)
        gptResponder = GptResponder(config)
    }

    fun process() {
        while (true) {
            val emails = emailTransport.getEmail()
            emails.forEach {message ->
                if (message.replyTo == null || message.senderName == null) {
                    logger.warn("Null properties on message: $message")
                } else {
                    emailTransport.sendEmail(message, gptResponder.reply(message))
                }
            }
            Thread.sleep(Duration.ofMinutes(1))
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EmailReplyBot().process()
        }
    }
}
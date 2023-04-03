import ai.GptResponder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import email.EmailParser
import email.EmailTransport
import model.Configuration
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

/**
 * An email auto reply bot. Uses GPT to craft a relevant response.
 */
class EmailReplyBot(conf: File) {

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val config: Configuration = objectMapper.readValue(conf, Configuration::class.java)
    private val emailTransport: EmailTransport = EmailTransport(config)
    private val gptResponder: GptResponder = GptResponder(config)
    private val logger = LoggerFactory.getLogger(javaClass)

    // use a single threadpool; this won't scale to thousands of requests per minute, but that's not the intended
    // use case of this software in any case.
    private val executor = Executors.newFixedThreadPool(40)
    private val counter = AtomicInteger(0)

    fun listen() {
        while (true) {
            emailTransport.getEmail().forEach { message ->
                process(message)
            }
            logger.info("Total processed: ${counter.get()}")
            Thread.sleep(60 * 1000) // 1 minute
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
            if (args.size != 1) {
                println("Supply the path to the config file; e.g. java -jar ./bot.jar /etc/bot.yml")
                exitProcess(1)
            }
            val conf = File(args[0])
            if (!conf.exists()) {
                println("The config file can't be located at $conf")
                exitProcess(1)
            }
            EmailReplyBot(conf).listen()
        }
    }
}
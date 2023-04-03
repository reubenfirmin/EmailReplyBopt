package email

import org.slf4j.LoggerFactory
import javax.mail.BodyPart
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.internet.InternetAddress

/**
 * Handle parsing multipart messages, which can be recursively structured.
 */
object EmailParser {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun Message.toEmailMessage(): EmailMessage {
        val internetAddress = this.replyTo?.get(0) as InternetAddress?
        val replyTo = internetAddress?.address
        val sender = this.from[0] as InternetAddress?
        val senderName = sender?.personal ?: sender?.address

        val body = this.content

        val msgs = if (body is Multipart) {
            (0 until body.count).map { idx ->
                processBodyPart(body.getBodyPart(idx))
            }
        } else if (body is String) {
            listOf(body)
        } else {
            logger.warn("don't know what to do with (main) $body")
            listOf("")
        }

        val msg = msgs.joinToString("\n")

        return EmailMessage(senderName, replyTo, this.subject!!, msg)
    }

    private fun processBodyPart(bodyPart: BodyPart): String {
        return if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
            // gpt can handle (and understand the formatting of) html elements, so don't strip them
            bodyPart.content as String
        } else if (bodyPart.content is Multipart) {
            // Handle multipart content
            val multipart = bodyPart.content as Multipart
            val count = multipart.count

            (0 until count).map { idx ->
                val nestedBodyPart = multipart.getBodyPart(idx)
                processBodyPart(nestedBodyPart) // recursively process nested BodyPart
            }.joinToString("\n")
        } else {
            // if we wanted attachments and other misc, handle here
            logger.warn("Don't know what to do with (sub) $bodyPart")
            ""
        }
    }

    data class EmailMessage(val senderName: String?, val replyTo: String?, val subject: String, val message: String)
}
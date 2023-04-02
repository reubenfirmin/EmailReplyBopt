package email

import email.EmailParser.toEmailMessage
import model.Configuration
import org.slf4j.LoggerFactory
import java.util.*
import javax.mail.*
import javax.mail.Flags.Flag.DELETED
import javax.mail.Flags.Flag.SEEN
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.search.FlagTerm

class EmailTransport(val config: Configuration) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val imapProps = Properties()
    private val smtpProps = Properties()

    init {
        imapProps["mail.imap.host"] = config.imap.host
        imapProps["mail.imap.port"] = config.imap.port
        imapProps["mail.imap.ssl.enable"] = "true"
        smtpProps["mail.smtp.host"] = config.smtp.host
        smtpProps["mail.smtp.port"] = config.smtp.port
        smtpProps["mail.smtp.auth"] = "true"
        smtpProps["mail.smtp.starttls.enable"] = "true"
    }

    fun getEmail(): List<EmailParser.EmailMessage> {
        val session = Session.getInstance(imapProps)

        val store = session.getStore("imap")
        store.connect(config.imap.user, config.imap.pass)

        val inbox = store.getFolder("INBOX")
        inbox.open(Folder.READ_WRITE)

        val messages = inbox.search(FlagTerm(Flags(SEEN), false))
        logger.info("Found ${messages.size} messages")

        return messages.map { message ->
            if (config.deleteMail) {
//                message.setFlag(DELETED, true)
            } else {
//                message.setFlag(SEEN, true)
            }
            message.toEmailMessage()
        }.apply {
            inbox.close(false)
            store.close()
        }
    }

    fun sendEmail(message: EmailParser.EmailMessage, reply: String) {
        val session = Session.getInstance(smtpProps, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.smtp.user, config.smtp.pass)
            }
        })

        Transport.send(MimeMessage(session).apply {
            setFrom(InternetAddress(config.replyTo.email, config.replyTo.name))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(message.replyTo))
            setSubject("Re: ${message.subject}")
            setText(reply)
        })
    }
}
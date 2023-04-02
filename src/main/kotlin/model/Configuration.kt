package model

data class Configuration(
    val imap: MailConfiguration,
    val smtp: MailConfiguration,
    val replyTo: ReplyTo,
    val openAIKey: String,
    val deleteMail: Boolean,
    val prompts: List<String>)

data class MailConfiguration(val host: String, val port: Int, val user: String, val pass: String)

data class ReplyTo(val name: String, val email: String, val signature: String)
# Email Reply Bot

Instead of setting a vacation responder the next time you're away from your email, why not hook up GPT and have it take care of your email for you? This project makes it easy to do just that.

# Demo

I have a bot listening to hobert4@zohomail.com (at least until I hit my OpenAI monthly limits.) Feel free to converse with it. Note that anything you send will be submitted to OpenAI's server; the bot will delete emails from the inbox after processing. Email addresses are _not_ stored, used, or harvested, except for the immediate reply. (Also note that some of the prompts I configured can be sarcastic.)

# Behavior
* Checks inbox every minute for new messages
* Adopts and maintains a "personality" per sender (prompts expire after 10 minutes, so you'll get a different personality after some time)
* Replies with a GPT created response

# Configure and run

Configure it:

1) `cp bot.yml.template bot.yml`

2) Edit bot.yml, and add your email credentials to the imap and smtp sections. Add your OpenAI key (https://platform.openai.com/account/api-keys).

3) Customize the replyTo. Your email provider may require the replyTo email matches your smpt user.

4) If you like, customize or add to the prompts. 

Build a self contained jar that can be deployed:
```
./gradlew bot 
```

Run it:
```
java -jar bot.jar bot.yml
```

You can deploy bot.jar and bot.yml together onto a server if you like.
# Email Reply Bot

Instead of setting a vacation responder the next time you're away from your email, why not hook up GPT and have it take care of your email for you? 

# Demo

I have a bot listening to hobert4@zohomail.com. Feel free to converse with it. (Note that anything you send will be submitted to OpenAI's server; the bot will delete emails from the inbox after processing.)

# Behavior
* Checks inbox every minute for new messages
* Adopts and maintains a "personality" per sender (prompts expire after 10 minutes, so you'll get a different personality after some time)
* Replies with a GPT created response

# Data Handling
* 

# Install And Configuration

To build it:

```
mvn install
```

To configure it:

1) 
```
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

2) Edit application.yml, and add your email credentials to the imap and smtp sections.

3) Customize the replyTo. Your email provider may require the replyTo email matches your smpt user.

4) If you like, customize or add to the prompts. 

To run it:

```
mvn exec:java
```

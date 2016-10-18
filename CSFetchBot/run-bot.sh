#!/bin/sh

# The address to the Symphony Agent API and Key Manager endpoints prefix (see -Dsessionauth.url, -Dkeyauth.url, -Dpod.url and -Dagent.url below)
FOUNDATION_API_URL=https://foundation-dev-api.symphony.com

# The address to the Symphony Pod endpoint prefix (see -Dpod.url below)
FOUNDATION_POD_URL=https://foundation-dev.symphony.com

RECEIVER_USER_EMAIL=yu.zheng@credit-suisse.com
BOT_USER_EMAIL=csfetchbot@gmail.com
BOT_CERT_PATH=./certs/userbot.p12
BOT_CERT_PASSWORD=changeit
TRUSTSTORE_PATH=./certs/server.truststore
TRUSTSTORE_PASSWORD=changeit

if [ -f $BOT_CERT_PATH ]; then
  echo "Found bot cert file $BOT_CERT_PATH"
else
  echo "Bot cert file is missing: $BOT_CERT_PATH"
fi

if [ -f $TRUSTSTORE_PATH ]; then
  echo "Found truststore file $TRUSTSTORE_PATH"
else
  echo "truststore file is missing: $TRUSTSTORE_PATH"
fi

java \
  -cp target/symphony-java-sample-bots-0.9.0-SNAPSHOT-jar-with-dependencies.jar \
  -Dsessionauth.url=$FOUNDATION_API_URL/sessionauth \
  -Dkeyauth.url=$FOUNDATION_API_URL/keyauth \
  -Dpod.url=$FOUNDATION_POD_URL/pod \
  -Dagent.url=$FOUNDATION_API_URL/agent \
  -Dtruststore.file=$TRUSTSTORE_PATH \
  -Dtruststore.password=$TRUSTSTORE_PASSWORD \
  -Dbot.user.cert.file=$BOT_CERT_PATH \
  -Dbot.user.cert.password=$BOT_CERT_PASSWORD \
  -Dbot.user.email=$BOT_USER_EMAIL \
  -Dreceiver.user.email=$RECEIVER_USER_EMAIL \
  org.symphonyoss.simplebot.StockInfoBot 

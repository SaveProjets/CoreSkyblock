./gradlew build
if [ $? -ne 0 ]; then
    echo "Gradle build failed. Exiting."
    exit 1
fi

./gradlew shadowJar
if [ $? -ne 0 ]; then
    echo "Gradle shadowJar failed. Exiting."
    exit 1
fi

PRIVATE_KEY_FILE="$HOME/.ssh/edmine_farmeur"

LOCAL_JAR_FILE="$HOME/IdeaProjects/CoreSkyblock/build/libs/CoreSkyblock-1.0.0-SNAPSHOT-all.jar"

SFTP_HOST="45.13.119.124"
SFTP_USERNAME="farmeur"
SFTP_PORT="22"

REMOTE_DIRECTORY1="/home/farmeur/skyblock-game-01/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY2="/home/farmeur/skyblock-game-02/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY3="/home/farmeur/skyblock-pvp-01/plugins/CoreSkyblock.jar"

sftp_command1="put $LOCAL_JAR_FILE $REMOTE_DIRECTORY1"
sftp_command2="cp $REMOTE_DIRECTORY1 $REMOTE_DIRECTORY2"
sftp_command3="cp $REMOTE_DIRECTORY1 $REMOTE_DIRECTORY3"

sftp -o "IdentityFile=$PRIVATE_KEY_FILE" -P $SFTP_PORT $SFTP_USERNAME@$SFTP_HOST << EOF
$sftp_command1
EOF

ssh -o "IdentityFile=$PRIVATE_KEY_FILE" -p $SFTP_PORT $SFTP_USERNAME@$SFTP_HOST << EOF
$sftp_command2
EOF

ssh -o "IdentityFile=$PRIVATE_KEY_FILE" -p $SFTP_PORT $SFTP_USERNAME@$SFTP_HOST << EOF
$sftp_command3
EOF

if [ $? -ne 0 ]; then
    echo "SFTP failed. Exiting."
    exit 1
fi

if [ $? -eq 0 ]; then
    echo "Deployment successful."
fi

echo "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
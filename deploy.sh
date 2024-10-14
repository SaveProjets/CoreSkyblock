#!/bin/bash

set -e

check_command() {
    if [ $? -ne 0 ]; then
        echo "$1 failed. Exiting."
        exit 1
    fi
}

./gradlew build
check_command "Gradle build"

./gradlew shadowJar
check_command "Gradle shadowJar"

PRIVATE_KEY_FILE="$HOME/.ssh/edmine_farmeur"
LOCAL_JAR_FILE="$HOME/IdeaProjects/CoreSkyblock/build/libs/CoreSkyblock-1.0.0-SNAPSHOT-all.jar"
SFTP_HOST="45.13.119.233"
SFTP_USERNAME="farmeur"
SFTP_PORT="22"

REMOTE_DIRECTORY_1="/home/farmeur/skyblock-spawn-01/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY_2="/home/farmeur/skyblock-game-01/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY_3="/home/farmeur/skyblock-game-02/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY_4="/home/farmeur/skyblock-pvp-01/plugins/CoreSkyblock.jar"
REMOTE_DIRECTORY_5="/home/farmeur/skyblock-spawn-02/plugins/CoreSkyblock.jar"

sftp -o "IdentityFile=$PRIVATE_KEY_FILE" -P $SFTP_PORT $SFTP_USERNAME@$SFTP_HOST << EOF
put $LOCAL_JAR_FILE $REMOTE_DIRECTORY_1
EOF
check_command "SFTP upload"

for REMOTE_DIRECTORY in $REMOTE_DIRECTORY_2 $REMOTE_DIRECTORY_3 $REMOTE_DIRECTORY_4 $REMOTE_DIRECTORY_5
do
    ssh -o "IdentityFile=$PRIVATE_KEY_FILE" -p $SFTP_PORT $SFTP_USERNAME@$SFTP_HOST << EOF
    cp $REMOTE_DIRECTORY_1 $REMOTE_DIRECTORY
EOF
    check_command "SSH copy to $REMOTE_DIRECTORY"
done

echo "Deployment successful."
echo "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
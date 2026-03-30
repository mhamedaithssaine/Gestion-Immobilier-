FROM jenkins/jenkins:lts-jdk17

USER root

# Docker CLI needed if pipeline builds/runs Docker commands.
RUN apt-get update \
    && apt-get install -y --no-install-recommends docker.io ca-certificates curl git \
    && rm -rf /var/lib/apt/lists/*

USER jenkins

# Pre-install required Jenkins plugins for CI/CD + Sonar + GitHub webhook.
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

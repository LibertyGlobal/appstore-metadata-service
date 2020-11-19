package com.lgi.common.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.IntStream;


@Mojo(name = "tcpmsg")
public class TcpMessagingMojo extends AbstractMojo {
    private static final String LOG_PREFIX = "maven-tcpmsg";

    @Parameter(property = "tcpmsg.host", defaultValue = "localhost")
    private String host;

    @Parameter(property = "tcpmsg.repeatAmount", defaultValue = "1")
    private Integer repeatAmount;

    @Parameter(property = "tcpmsg.intervalSec", defaultValue = "5")
    private Integer intervalSec;

    @Parameter(property = "tcpmsg.port")
    private Integer port;

    @Parameter(property = "tcpmsg.command")
    private String msg;

    @Override
    public void execute() throws MojoExecutionException {
        if (port == null) {
            getLog().error("Please specify 'port' param. of TCP socket.");
            throw new MojoExecutionException(getPluginContext().keySet().toString());
        } else if (msg == null) {
            getLog().warn("Please specify 'msg' param. with command to send.");
        } else {
            doCommunicate();
        }
    }

    private void doCommunicate() {
        Thread communicationChannel = new Thread(
                new ThreadGroup(LOG_PREFIX),
                this::sendMessages,
                String.format("%s-send", LOG_PREFIX)
        );
        communicationChannel.setDaemon(true);
        communicationChannel.start();
    }

    private void sendMessages() {
        IntStream.range(0, repeatAmount).forEach(attempt -> {
            sleepForConfiguredInterval();
            getLog().info(String.format("%s: Attempting [%d] to send message '%s' to TCP socket on %s:%d", LOG_PREFIX, attempt, msg, host, port));
            handleSendAttempt();
        });
    }

    private void sleepForConfiguredInterval() {
        try {
            Thread.sleep(intervalSec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleSendAttempt() {
        try (Socket clientSocket = new Socket(host, port)) {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String response = communicate(in, out);
                    getLog().info(String.format("%s: Received: %s", LOG_PREFIX, response));
                }
            }
        } catch (IOException e) {
            logSocketIssue(e);
        }
    }

    private String communicate(BufferedReader in, PrintWriter out) {
        try {
            out.println(msg);
            return in.readLine();
        } catch (IOException e) {
            String errorMsg = String.format("%s: Issue with sending message '%s' to TCP socket on %s:%d: %s - %s", LOG_PREFIX, msg, host, port, e.getClass().getSimpleName(), e.getMessage());
            getLog().error(errorMsg);
            return null;
        }
    }

    private void logSocketIssue(Exception e) {
        String warnMsg = String.format("%s: Issue with connection to TCP socket on %s:%d - %s: %s", LOG_PREFIX, host, port, e.getClass().getSimpleName(), e.getMessage());
        getLog().warn(warnMsg);
    }
}
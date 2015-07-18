# Cafebabe HTTP [![Build Status](https://travis-ci.org/yet-another-cafebabe/http.svg?branch=master)](https://travis-ci.org/yet-another-cafebabe/http)

Simple HTTP server/client powered by Netty and Akka.

Configuration example (HOCON):

    cafebabe.http.servers = [{
        
        port = 8080
        
        // Number of workers to process incoming messages.
        // 1 by default.
        workerGroupSize = 2
        
        HTTP = {
            
            // Time limit for resolveOne operation of route actor selection.
            // 10 seconds by default.
            resolveTimeout = 5s
            
            // Time limit to ask route actor.
            // 60 seconds by default.
            requestTimeout = 10s
            
            // Maximal length of HTTP request content.
            // 65536 bytes by default.
            maxContentLength = 2M
            
            // Compression level from 0 to 9.
            // Comment this line to disable compression.
            compression = 6
            
            routes = [{
                path = /api/1.0
                actor = /user/http-router
            }]
        }
        
        WebSocket = {
            
            // Time limit for resolveOne operation of route actor selection.
            // 10 seconds by default.
            resolveTimeout = 5s
            
            // Maximal WebSocket frame payload length.
            // 65536 bytes by default.
            maxFramePayloadLength = 2M
            
            // Compression of WebSocket text frames.
            // false by default.
            compression = true
            
            routes = [{
                path = /websocket
                actor = /user/ws-router
            }]
        }
    }]

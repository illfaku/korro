# Korro [![Build Status](https://travis-ci.org/yet-another-cafebabe/korro.svg?branch=master)](https://travis-ci.org/yet-another-cafebabe/korro)

Simple HTTP server/client powered by Netty and Akka.

Configuration example (HOCON):

    korro.server = {
        
        default = {
        
            // Port to bind on.
            // 8080 by default.
            port = 8282
            
            // Number of workers to process incoming messages.
            // 1 by default.
            workerGroupSize = 2
            
            HTTP = {
                
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
        }
    }
    
    korro.client = {
        
        default = { // Accessible at path /user/korro-client/default.
            
            // Optional URI
            uri = "http://127.0.0.1:8080"
            
            // Number of workers to process outgoing messages.
            // 1 by default.
            workerGroupSize = 2
            
            // Maximal length of HTTP response content.
            // 65536 bytes by default.
            maxContentLength = 2M
        }
    }

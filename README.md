Simple HTTP server/client powered by Netty and Akka.

Configuration example (HOCON):

    cafebabe.http.servers = [{
        port = 8080
        workerGroupSize = 2  // default: 1
        HTTP = {
            resolveTimeout = 5s  // default: 10 seconds
            requestTimeout = 10s  // default: 60 seconds
            maxContentLength = 2M  // default: 65536 bytes
            routes = [{
                path = /api/1.0
                actor = /user/http-router
            }]
        }
        WebSocket = {
            resolveTimeout = 5s  // default: 10 seconds
            maxFramePayloadLength = 2M  // default: 65536 bytes
            routes = [{
                path = /websocket
                actor = /user/ws-router
            }]
        }
    }]

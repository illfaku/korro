/*
 * Copyright 2018 Vladimir Konstantinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.illfaku.korro

import com.github.illfaku.korro.config._
import com.github.illfaku.korro.dto._
import com.github.illfaku.korro.dto.ws.WsHandshakeRequest
import com.github.illfaku.korro.internal.client.ClientActor
import com.github.illfaku.korro.internal.server.actor.ServerActor

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.pattern.ask
import akka.util.Timeout

import java.net.URL

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Collection of methods for starting HTTP servers and clients.
 */
object Korro {

  /**
   * Starts server actor by given name using your ActorRefFactory.
   * @param name Actor name.
   * @param handler Actor that will handle requests not matched in route config.
   * @param config Configuration of HTTP server.
   * @return Actor reference.
   */
  def server(name: String, handler: ActorRef, config: ServerConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(serverProps(config.copy(routes = config.routes :+ RouteConfig(RouteActorRef(handler)))), name)
  }

  /**
   * Starts server actor by given name using your ActorRefFactory.
   * @param name Actor name.
   * @param config Configuration of HTTP server.
   * @return Actor reference.
   */
  def server(name: String, config: ServerConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(serverProps(config), name)
  }

  /**
   * Creates props with which you can start server actor by your name using your ActorRefFactory.
   * @param config Configuration of HTTP server.
   * @return Props for actor creation.
   */
  def serverProps(config: ServerConfig): Props = Props(new ServerActor(config))


  /**
   * Starts client actor by given name using your ActorRefFactory with default configuration.
   * @param name Actor name.
   * @return Actor reference.
   */
  def client(name: String)(implicit factory: ActorRefFactory): Client = client(name, ClientConfig())

  /**
   * Starts client actor by given name using your ActorRefFactory.
   * @param name Actor name.
   * @param config Configuration of HTTP client.
   * @return Actor reference.
   */
  def client(name: String, config: ClientConfig)(implicit factory: ActorRefFactory): Client = {
    implicit val timeout: Timeout = config.instructions
      .find(_.isInstanceOf[HttpInstruction.RequestTimeout])
      .map(_.asInstanceOf[HttpInstruction.RequestTimeout].timeout.plus(1 second))
      .getOrElse(6 seconds)
    new Client(factory.actorOf(clientProps(config), name))
  }

  /**
   * Creates props with which you can start client actor by your name using your ActorRefFactory.
   * @param config Configuration of HTTP client.
   * @return Props for actor creation.
   */
  def clientProps(config: ClientConfig): Props = Props(new ClientActor(config))


  /**
   * Just a wrapper for client ActorRef with some handy methods to send HTTP requests
   * and initiate WebSocket connections.
   * @param ref Client actor reference.
   */
  class Client(val ref: ActorRef)(implicit timeout: Timeout) {

    /**
     * Sends GET request using HTTP/1.1.
     * @param path URI path.
     * @param params URI parameters.
     * @param headers HTTP headers.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def get(path: String = "", params: HttpParams = HttpParams.empty, headers: HttpParams = HttpParams.empty)
      (implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      send(HttpRequest.Method.Get(path, params, headers = headers))
    }

    /**
     * Sends POST request using HTTP/1.1.
     * @param path URI path.
     * @param content Request body.
     * @param headers HTTP headers.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def post(path: String = "", content: HttpContent = HttpContent.empty, headers: HttpParams = HttpParams.empty)
      (implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      send(HttpRequest.Method.Post(path, content = content, headers = headers))
    }

    /**
     * Sends PUT request using HTTP/1.1.
     * @param path URI path.
     * @param content Request body.
     * @param headers HTTP headers.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def put(path: String = "", content: HttpContent = HttpContent.empty, headers: HttpParams = HttpParams.empty)
      (implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      send(HttpRequest.Method.Put(path, content = content, headers = headers))
    }

    /**
     * Sends DELETE request using HTTP/1.1.
     * @param path URI path.
     * @param params URI parameters.
     * @param headers HTTP headers.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def delete(path: String = "", params: HttpParams = HttpParams.empty, headers: HttpParams = HttpParams.empty)
      (implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      send(HttpRequest.Method.Delete(path, params, headers = headers))
    }


    /**
     * Sends request using HTTP/1.1.
     * @param req HTTP request.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def send(req: HttpRequest)(implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      (ref ? req).mapTo[HttpResponse]
    }

    /**
     * Sends request to specified URL using HTTP/1.1.
     * @param req HTTP request.
     * @param url Destination URL.
     * @param instructions Additional instructions.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def send(req: HttpRequest, url: URL, instructions: List[HttpInstruction] = Nil)
      (implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      send(req to (url, instructions))
    }

    /**
     * Sends request using HTTP/1.1.
     * @param req HTTP request with URL and additional instructions.
     * @param sender Message originator.
     * @return Future of HTTP response.
     */
    def send(req: HttpRequest.Outgoing)(implicit sender: ActorRef = Actor.noSender): Future[HttpResponse] = {
      val timeoutOverride = req.instructions
        .find(_.isInstanceOf[HttpInstruction.RequestTimeout])
        .map(_.asInstanceOf[HttpInstruction.RequestTimeout].timeout.plus(1 second)).map(Timeout(_))
        .getOrElse(timeout)
      ref.?(req)(timeoutOverride, sender).mapTo[HttpResponse]
    }


    /**
     * Initiates WebSocket connection.
     * @param actor Actor that will process handshake response and WebSocket frames.
     * @param uri URI of handshake request.
     * @param headers HTTP headers of handshake request.
     */
    def ws(actor: ActorRef, uri: String = "", headers: HttpParams = HttpParams.empty): Unit = {
      ws(WsHandshakeRequest(actor, HttpRequest.Uri(uri), headers))
    }

    /**
     * Initiates WebSocket connection.
     * @param req Handshake request.
     */
    def ws(req: WsHandshakeRequest): Unit = ref ! req

    /**
     * Initiates WebSocket connection.
     * @param actor Actor that will process handshake response and WebSocket frames.
     * @param url Destination URL.
     * @param headers HTTP headers of handshake request.
     * @param instructions Additional instructions.
     */
    def ws(
      actor: ActorRef,
      url: URL,
      headers: HttpParams = HttpParams.empty,
      instructions: List[HttpInstruction] = Nil
    ): Unit = {
      ws(WsHandshakeRequest(actor, HttpRequest.Uri(""), headers).to(url, instructions))
    }

    /**
     * Initiates WebSocket connection.
     * @param req Handshake request with URL and additional instructions.
     */
    def ws(req: WsHandshakeRequest.Outgoing): Unit = ref ! req
  }
}

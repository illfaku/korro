package io.cafebabe.korro.client

import akka.actor.ActorRef

import scala.concurrent.Future

/**
 * Created by ygintsyak on 15.06.15.
 */
class HttpRequestBuilder {

  abstract class ActorFunction(ref:ActorRef) extends ((Any)=>Future[Any]) {



  }






  def POST[T](uri:String)(any:Any):(T)=>Future[Any] = {




    ???
  }

  def PUT[T](uri:String)(any:Any):(T)=>Future[Any] = {
    ???
  }

  def GET[T](uri:String)(any:Any):(T)=>Future[Any] = {
    ???
  }

  def HEAD[T](uri:String)(any:Any):(T)=>Future[Any] = {
    ???
  }
}

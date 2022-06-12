package com.example

import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val errorResponseFormat = jsonFormat2(ErrorResponse)
}


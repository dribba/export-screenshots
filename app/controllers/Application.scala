package controllers

import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64
import java.util.zip.{ZipEntry, ZipOutputStream}

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.core.parsers.Multipart

case class Screenshot(base64: String, ts: Long)

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def tolerantMultipartFormData(maxLength: Long) =
    parse.multipartFormData(Multipart.handleFilePartAsTemporaryFile, maxLength)


  private def mapReadsSeq[A <: JsValue, B](reads: Reads[Seq[A]], r: Reads[B]): Reads[Seq[B]] = {
    Reads(json => {
      reads.reads(json).flatMap(values => {
        values.foldLeft[JsResult[Seq[B]]](JsSuccess(Nil)) {
          case (result, next) =>
            result.flatMap(newSeq => r.reads(next).map(_ +: newSeq))
        }
      })
    })
  }


  def upload = Action(tolerantMultipartFormData(parse.UNLIMITED)) { request =>
    request.body.file("data").map { file =>

      def throwTS =
        throw new Exception("Couldn't find base TS")

      val json = Json.parse(new FileInputStream(file.ref.file))
      val filename = file.filename.replaceAll("\\.[^.]*$", "")

      val extractShots = (
        (__ \ "args" \ "snapshot").read[String] and
        (__ \ "ts").read[Long]
      )(Screenshot.apply _)

      val base =
        __.read[Seq[JsObject]].map(_.filter(obj => (obj \ "name").as[String] == "Screenshot"))


      val baseTs =
        __.read[Seq[JsObject]].map(_.headOption.map(o => (o \ "ts").as[Long])).reads(json)
          .getOrElse(throwTS)
          .getOrElse(throwTS)

      val screenShots = mapReadsSeq(base, extractShots)

      val md = MessageDigest.getInstance("SHA-1")

      def decode(s: String) =
        Base64.getDecoder.decode(s)


      def imageName(img: Array[Byte], ts: Long) = {
        val hash = new BigInteger(1, md.digest()).toString(16).take(5)
        val time = ((ts - baseTs.toDouble) / (1000 * 1000)).toString.take(5)

        md.reset()
        md.update(img)

        s"$filename-$time-$hash.png"
      }

      def removeDup(data: Seq[Screenshot]) =
        data.groupBy(_.base64).map(_._2.sortBy(_.ts).head) // Super ugly but does the trick

      screenShots.reads(json) match {
        case JsSuccess(data, _) =>
          import play.api.libs.concurrent.Execution.Implicits.defaultContext

          val zipped = Enumerator.outputStream(out => {
            val zip = new ZipOutputStream(out)

            try {
              removeDup(data).zipWithIndex.foreach {
                case (Screenshot(base64, ts), i) =>
                  val image = decode(base64)
                  val name = imageName(image, ts)

                  zip.putNextEntry(new ZipEntry(name))
                  zip.write(image)
                  zip.closeEntry()
              }
            } catch {
              case e: Throwable =>
                play.api.Logger.error("Broke", e)
                zip.close()

            } finally {
              zip.close()
            }
          })

          Ok.stream(zipped >>> Enumerator.eof).withHeaders(
            CONTENT_TYPE -> "application/zip",
            CONTENT_DISPOSITION -> s"attachment; filename=$filename.zip"
          )

        case JsError(errors) =>
          InternalServerError(Json.toJson(errors.flatMap(_._2).map(_.messages)))

      }
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Missing file")
    }
  }

}

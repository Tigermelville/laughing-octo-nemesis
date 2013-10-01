package testshorty

import org.scalatest._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import play.api.test._
import play.api.test.Helpers._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json
import play.api.libs.json._
import anorm.Pk

import models._
import service._

class ShortyTest extends FlatSpec with ShouldMatchers {
  // Reference http://en.wikipedia.org/wiki/Uniform_resource_locator
  // or http://tools.ietf.org/html/rfc3986 for the pedantic.  These
  // characters can legitimately occur in shortened urls
  val legitUrlChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-.~"

  // Shortened urls will never contain these characters
  val bumChars = "!*'();:@&=+$,/?%#[] "  // notice Space is one of the bad guys

  def postgresDatabase(name: String = "default", 
                   options: Map[String, String] = Map.empty): Map[String, String] =
  Map(
    "db.default.driver"   -> "org.postgresql.Driver",
    "db.default.user"     -> "shorty",
    "db.default.password" -> "shrty",
    "db.default.url"      -> "jdbc:postgresql://localhost/shortydev"
  )

  def fakeApp[T](block: => T): T =
    running(FakeApplication(additionalConfiguration = 
      postgresDatabase("default") ++ Map("evolutionplugin" -> "disabled"))) {
        val db = DB.getDataSource("default")
        block
      }

  "config test 1" should
    "find 64 digits" in fakeApp {
    assert(Shorty.digits.length == 64)
  }

  "config test 2" should
    "find legitimate url characters" in fakeApp {
    Shorty.digits.map(chr => assert(legitUrlChars.indexOf(chr) != -1))
  }

  "config test 3" should
    "find unique digits" in fakeApp {
    for ((chr, index) <- Shorty.digits.toList.zipWithIndex)
      if (index < 63)
        assert(Shorty.digits.substring(index + 1).indexOf(chr) == -1)
  }
  
  "config test 4" should
    "find an http or https host in the config {shorty.host}" in fakeApp {
    assert(Shorty.shortyHost.indexOf("http://") == 0 || Shorty.shortyHost.indexOf("https://") == 0)
  }

  "base 64 encodings" should
    "obey the reflexive property" in fakeApp {
    val rand = new scala.util.Random(new java.util.Date().getTime())
    (1 to 1000).foreach { ignore =>
      val r = rand.nextLong()
      val random = if (r < 0) r * -1 else r
      val encoded = Shorty.encode(random)
      val maybeDecoded = Shorty.decode(encoded)
      assert(maybeDecoded.isDefined && maybeDecoded.get == random)
    }  
  }
  
  "urls with bum characters" should
    "fail to decode" in fakeApp {
    bumChars.map(chr => assert(!Shorty.decode("Something" + chr.toString + "Rotten").isDefined))
  }
  
  "Random urls" should
    "save to the DB and be retrievable" in fakeApp {
    val rand = new scala.util.Random(new java.util.Date().getTime())
    (1 to 500) foreach { ignored =>
      val r = rand.nextLong()
      val random = if (r < 0) r * -1 else r
      val url = "http://stackoverflow.com/questions/" + random + "/please-help-me-jquery-gods?novice=true"
      val instance = Shorty.lookupOrCreateShortUrl(url)
      val id = instance.id.get
      val lookup = ShortUrl.lookupByUrl(url)
      assert(lookup.isDefined)
      val instance2 = ShortUrl.lookup(id)
      assert (instance2.isDefined && instance.id.get == lookup.get.id.get)
    }
  }
 
  "Zero size urls" should
    "bomb" in fakeApp {
      try {
        val short = Shorty.shortenUrl("")
        fail()
      } catch {
        case x: BadUrlException =>
      }
  }

  "Immensely long urls" should
      "die as well" in fakeApp {
      val someChars = "afdiayuifaeniavcdaijv"
      val card = someChars.length
      val builder = new StringBuilder(5500)
      for (i <- 1 until 5500)
        builder += someChars(i % card)
      try {
        val short = Shorty.shortenUrl(builder.toString)
        fail()
      } catch {
        case x: BadUrlException =>
      }
  }
}

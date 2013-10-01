package service

import models._

class BadUrlException(msg: String) extends Exception(msg)

/**
 * Helper object to shorten and lengthen urls.
 */
object Shorty {
  // This host is of form http:thishost.com or https://thathost.com
  val shortyHost = play.api.Play.current.configuration.getString("shorty.host").getOrElse("")

  /**
   * The digits in my version of a base 64 number
   */
  val digits = play.api.Play.current.configuration.getString("shorty.digits").getOrElse("")

  /**
   * Produce a base 64 encoded string from a long int
   * todo: the math here could use shifing and masking, since
   * 64 is a power of two.  I haven't done so to avoid
   * optimizing too early.  todo: optimize base64 arithmetic
   */
  def encode(num: Long): String = {
    if (num == 0)
      digits(0).toString
    else {
      val builder = new StringBuilder(256)
      var tmp: Long = num  // Oh my, a var!  It is locally scoped so I can live with it.

      while (tmp > 0) {
        builder += digits((tmp % 64).toInt)
        tmp = tmp / 64
      }

      builder.reverse.toString
    }
  }

  /**
   * Decode a base 64 encoded string produced by my encode() method.
   * BigDecimal is used to avoid the painful absense of unsigned long
   * in the JVM
   * @return None if an unknown character is found, or the value overflows
   */
  def decode(str: String): Option[Long] = {
    import java.math.BigDecimal
    class BadCharException extends Exception

    def build(): Long = {
      val base = new BigDecimal(64L)
      var acc = new BigDecimal(0L)
      var factor = new BigDecimal(1L)
      str.reverse.map { chr =>
        val digit = digits.indexOf(chr)
        if (digit == -1)
          throw new BadCharException
        // acc = acc + factory * digit
        acc = acc.add(factor.multiply(new BigDecimal(digit.toLong)))
        factor = factor.multiply(base)
      }
      // this will throw an ArithmeticException on overflow
      acc.longValueExact()
    }

    if (str.length == 1 && str(0) == digits(0))
      Some(0)
    else {
      try {
        Some(build())
      } catch {
        case x: ArithmeticException =>
          None
        case x: BadCharException =>
          None
      }
    }
  }

  /**
   * Lookup or create a ShortUrl instance, handling race
   * conditions
   */
  def lookupOrCreateShortUrl(givenUrl: String): ShortUrl = {
    val shortUrl: Option[ShortUrl] = ShortUrl.lookupByUrl(givenUrl)

    if (shortUrl.isDefined)
      shortUrl.get
    else 
     try {
       ShortUrl.create(givenUrl)
     } catch {
       case ex: AnormNoInsertException =>
         // How about that.  Somebody beat me to it
         ShortUrl.lookupByUrl(givenUrl).get
     }
  }

  /**
   * @param url the plain url
   * @return the shortened url
   * @throws service.BadUrlException if the url length is not in range [1, 4095]
   * or length zero
   */
  def shortenUrl(givenUrl: String): String = {
    if (givenUrl.length == 0)
      throw new BadUrlException("A length 0 url is in poor taste")
    if (givenUrl.length > 4095)
      throw new BadUrlException("Max url length is 4095")

    // get the persistent ShortUrl instance from the db
    val shortUrl = lookupOrCreateShortUrl(givenUrl)
    // the instance has the original url and an id
    val id = shortUrl.id.get
    // the code is a custom base 64 interpretation of the id
    val code = encode(id)
    // still not using the 2.10 string interpolation - old habits die hard!
    shortyHost + "/" + code
  }

  /**
   * @param encoding, which is the part of the shortened
   * url past the http://disc.org:8888/ part
   * @return the url string or None if the encoding is bogus
   */
  def encodingToUrl(encoding: String): Option[String] = {
    val optId = decode(encoding)
    if (!optId.isDefined)
      None
    else {
      val optShortUrl = ShortUrl.lookup(optId.get)
      
      if (!optShortUrl.isDefined)
        None
      else
        Some(optShortUrl.get.url)
    }
  }
}

package work

object work {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val x = "abceg".toList.grouped(4).toList        //> x  : List[List[Char]] = List(List(a, b, c, e), List(g))
  def bytesToHex(bytes : List[Byte]) =
     bytes.map{ b => String.format("%02X", java.lang.Byte.valueOf(b)) }.mkString(" ")
                                                  //> bytesToHex: (bytes: List[Byte])String
  val url = "abcdefgh"                            //> url  : String = abcdefgh
  val bytes = List('a', 'b', 'c', 'd').map(_.toByte)
                                                  //> bytes  : List[Byte] = List(97, 98, 99, 100)
  val len = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789".length
                                                  //> len  : Int = 63
  val numeric =
      for (tuple <- url.toList.grouped(4).toList)
        yield tuple.length match {
          case 4 => (tuple(0).toInt << 12) + (tuple(1).toInt << 8) + (tuple(2).toInt << 4) + tuple(3).toInt
          case 3 => (tuple(0).toInt << 8) + (tuple(1).toInt << 4) + tuple(2).toInt
          case 2 => (tuple(0).toInt << 4) + tuple(1).toInt
          case 1 => tuple(0).toInt
        }                                         //> numeric  : List[Int] = List(424084, 441560)
  val spliturl = "http://www.disc.org/blat".split("://")
                                                  //> spliturl  : Array[String] = Array(http, www.disc.org/blat)
  val split2 = "http://www.disc.org/blat".splitAt("http://www.disc.org/blat".indexOf("://"))
                                                  //> split2  : (String, String) = (http,://www.disc.org/blat)
  
  val numeric2 =
      for (tuple <- url.toList.grouped(8).toList)
        yield bytesToHex(tuple.map(_.toByte))     //> numeric2  : List[String] = List(61 62 63 64 65 66 67 68)

  val digits  = "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789"
                                                  //> digits  : String = abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ_01
                                                  //| 23456789
  val digits2 = "opqrstuvwxyzABCDEFmnPQRSTUV-WXYZ_01234GHIJKLMNOabcdefghijkl56789"
                                                  //> digits2  : String = opqrstuvwxyzABCDEFmnPQRSTUV-WXYZ_01234GHIJKLMNOabcdefgh
                                                  //| ijkl56789
  val digits3 = "opqrstuvwxyzABCDEFmnPQRSTUV-WXYZ_01234GHIJKLMNOabcdefghijkl56789"
                                                  //> digits3  : String = opqrstuvwxyzABCDEFmnPQRSTUV-WXYZ_01234GHIJKLMNOabcdefgh
                                                  //| ijkl56789
  val len2 = digits3.length                       //> len2  : Int = 64
  val len3 = digits.length                        //> len3  : Int = 64
  def splitUrl(url: String): Tuple2[String, String] = {
    val index = url.indexOf("://")
    if (index == -1)
      ("http", url)
    else {
      val pair = url.splitAt(index)
      (pair._1, pair._2.substring(3))
    }
  }                                               //> splitUrl: (url: String)(String, String)

  val url1 = splitUrl("http://www.disc.org")      //> url1  : (String, String) = (http,www.disc.org)
  val url2 = splitUrl("www.disc.org")             //> url2  : (String, String) = (http,www.disc.org)
  val url3 = "http://www.disc.org:8888/junk"      //> url3  : String = http://www.disc.org:8888/junk
  val index = url3.indexOf(':', url3.indexOf(':'))//> index  : Int = 4
  
  def encode(num: Long): String = {
    if (num == 0)
      "a"
    else {
      val builder = new StringBuilder(256)
      var tmp: Long = num

      while (tmp > 0) {
        builder += digits((tmp % 64).toInt)
        tmp = tmp / 64
      }

      builder.reverse.toString
    }
  }                                               //> encode: (num: Long)String

  def decode(str: String): Long = {
    def process(acc: Long, place: Int, str: String, index: Int): Long = {
      if (index >= 0)
        process(acc + digits.indexOf(str.charAt(index)) * place, place * 64, str, index - 1)
      else acc
    }
    process(0, 1, str, str.length - 1)
  }                                               //> decode: (str: String)Long
  
  val a = encode(1234567)                         //> a  : String = eS-h
  val b = decode("da4")                           //> b  : Long = 12346
  val fact = 1 << 6                               //> fact  : Int = 64
}
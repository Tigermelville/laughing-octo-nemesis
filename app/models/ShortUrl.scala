package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.libs.json
import play.api.libs.json._
import play.api.Play.current

case class AnormNoInsertException(msg: String) extends Exception(msg)

case class ShortUrl (val id: Pk[Long], val url: String)

object ShortUrl {
  /**
   * Persistent short url class factory
   * 
   * @constructor create instance and insert into the database
   * @param url the original version of the url, max length 4095
   * @return an instance of case class ShortUrl
   * @throws models.AnormNoInsertException SQL insert did not return 1
   * @throws java.sql.SQLException unhandled DB exception
   */
  def create(url: String): ShortUrl = {
    DB.withConnection { implicit connection =>
      val id: Long = SQL("select pseudo_encrypt(nextval('short_url_id_seq'))").as(scalar[Long].single)
      try {
        val kt = SQL(
          """
            insert into t_short_url (
              f_id, f_url
            ) values (
            {id}, {url}
          )
        """).on(
            'id -> id,
            'url -> url
          ).executeUpdate()
        if (kt != 1)
            throw AnormNoInsertException(s"t_short_url insert returned $kt")
      } catch {
        case ex: java.sql.SQLException =>
          if (ex.getSQLState() == "23505") // todo - postgres specific, need a driver call here
            throw AnormNoInsertException("t_short_url Key Collision")
          else
            throw ex
      }
      ShortUrl(Id(id), url);
    }
  }
  
  /**
   * a parser to read result rows from JDBC
   */
  val simpleRowParser = {
    get[Pk[Long]]("f_id") ~
    get[String]("f_url") map {
      case id~url => ShortUrl(
        id, url
      )
    }
  }

  /**
   * Lookup a ShortUrl by its primary key
   */
  def lookup(id: Long): Option[ShortUrl] = {
    DB.withConnection { implicit connection =>/* leaf lookup */
      SQL("select f_id, f_url from t_short_url where f_id = {id}").on(
        'id -> id
      ).as(ShortUrl.simpleRowParser.singleOpt)
    }
  }

  /**
   * Lookup a ShortUrl by its url
   */
  def lookupByUrl(url: String): Option[ShortUrl] = {
    DB.withConnection { implicit connection =>/* leaf lookup */
      SQL("select f_id, f_url from t_short_url where f_url = {url}").on(
        'url -> url
      ).as(ShortUrl.simpleRowParser.singleOpt)
    }
  }
  
  /**
   * @return a count of ShortUrl instances in the db
   */
  def count: Long = {
    DB.withConnection { implicit connection =>
      SQL("select count(f_id) from t_short_url").as(scalar[Long].single)
    }
  }
}

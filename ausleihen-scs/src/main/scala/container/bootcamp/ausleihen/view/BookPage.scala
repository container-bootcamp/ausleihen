package container.bootcamp.ausleihen.view

import container.bootcamp.ausleihen.books.Book.{BookLent, BookReturned}
import container.bootcamp.ausleihen.books.BookLibrarian.BookNotFound
import container.bootcamp.ausleihen.books.BookLibrarianLookup.BookDataLookup

/**
  * Provide the all html pages
  */
object BookPages {

  private def pageHeader = {
    """
       | <head>
       |    <title>Ausleihen</title>
       |    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M" crossorigin="anonymous">
       |    <link rel="stylesheet" href="/css/bibliothek.css">
       |    <link rel="stylesheet" href="/ausleihen/assets/ausleihen.css">
       |    <meta name="cache-control" content="no-cache" />
       |    <meta name="expires" content="0" />
       |    <meta name="pragma" content="no-cache" />
       |    <script src="/ausleihen/assets/reload.js"></script>
       |  </head>
     """.stripMargin
  }

  def indexPage: String = {
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <p>
       |      Um ein Buch auszuleihen, zuvor Buch <a href="/suchen" class="btn btn-primary">auswählen</a>
       |    <p>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
    """.stripMargin.replace("  ", " ")
  }

  def bookPage(bookData: BookDataLookup): String = {

    val lent = if(bookData.lent)
      """<span class="badge badge-danger">ja</span>"""
    else
      """<span class="badge badge-success">nein</span>"""


    val reserved = if(bookData.reserved)
      """<span class="badge badge-danger">ja</span>"""
    else
      """<span class="badge badge-success">nein</span>"""

      val loanForm = {
        if(!bookData.reserved) {
          if (bookData.lent) {
            """
              |  <input type="hidden" name="lent" value="false">
              |  <button class="btn btn-dark" type="submit">Zurück Geben</button>
              |""".stripMargin
          }
          else {
            """
              |  <input type="hidden" name="lent" value="true">
              |  <button class="btn btn-dark" type="submit">Ausleihen</button>
              |""".stripMargin
          }
        } else ""
    }

    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <main>
       |      <div class="o-box">
       |        <h1>Buch Ausleihen</h1>
       |        <div class="card bg-light mb-3">
       |          <div class="card-body">
       |              <p>
       |                <h4 class="card-title">Titel: ${bookData.title.getOrElse("")}<h4/><br>
       |                <h6 class="card-subtitle mb-2 text-muted">Autor: ${bookData.author.getOrElse("")}</h6><br>
       |                <p class="card-text">Kurzbeschreibung: ${bookData.shortDescription.getOrElse("")}</p><br>
       |                Isbn: ${bookData.isbn.getOrElse("")}<br>
       |                Ausgeliehen: $lent<br>
       |                Reserviert: $reserved<br>
       |                <br>
       |                <form action="/ausleihen/book/${bookData.isbn.getOrElse("")}/lent" method="post">
       |                  $loanForm
       |                </form>
       |              </p>
       |            </div>
       |          </div>
       |        </div>
       |      </main>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
       |
       """.stripMargin
  }

  def lentPage(bookLend: BookLent): String = {

    val error = if(! bookLend.lend) "nicht" else ""
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |        <p class="card-text">
       |        Buch ${error} erfolgreich ausgeliehen.<br>
       |        <br>
       |        Weiter zu <a href="/suchen" class="btn btn-dark">suchen</a>
       |      </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
    """.stripMargin.replace("  ", " ")
  }

  def lentStatePage(lentState: Boolean): String = {

    val lent = if(lentState)
      """<span class="badge badge-danger">ja</span>"""
    else """<span class="badge badge-success">nein</span>"""

      s"""
         |<html>
         |  $pageHeader
         |  <body>
         |    <esi:include src="http://assets/header.html"/>
         |    <div class="o-box">
         |      <h1>Buch Ausleihen</h1>
         |      <p class="card-text">
         |        <main>Buch ausgeliehen: $lent</main>
         |        <br>
         |        Weiter zu <a href="/suchen" class="btn btn-dark">suchen</a>
         |      </p>
         |    </div>
         |    <esi:include src="http://assets/footer.html"/>
         |  </body>
         |</html
    """.stripMargin
  }

  def alreadyLendPage: String = {
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |      <p class="card-text">
       |        Buch bereits ausgeliehen.<br>
       |        <br>
       |        Weiter zu <a href="/suchen" class="btn btn-dark">suchen</a>
       |      </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
    """.stripMargin
  }

  def returnPage(bookReturned: BookReturned): String = {

    val error = if(!bookReturned.returned) "" else "nicht"
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |      <p class="card-text">
       |        Buch ${error} erfolgreich zurückgeben.<br>
       |        <br>
       |        Weiter zu  <a href="/suchen" class="btn btn-dark">suchen</a>
       |      </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
    """.stripMargin.replace("  ", " ")
  }

  def alreadyReturnedPage: String = {
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |      <p class="card-text">
       |        Buch bereits zurückgegeben.<br>
       |        <br>
       |        Weiter zu <a href="/suchen" class="btn btn-dark">suchen</a>
       |      </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
    """.stripMargin
  }

  def bookNotFound(bookNotFound: BookNotFound): String = {
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |      <p class="card-text">
       |        Buch nicht gefunden mit isbn: ${bookNotFound.isbn}
       |        <br>
       |        <a href="/suchen" class="btn btn-dark">suchen</a>
       |      </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
     """.stripMargin
  }

  def unexpectedErrorPage: String = {
    s"""
       |<html>
       |  $pageHeader
       |  <body>
       |    <esi:include src="http://assets/header.html"/>
       |    <div class="o-box">
       |      <h1>Buch Ausleihen</h1>
       |      <p class="card-text">
       |        Unerwarteter Fehler!
       |       </p>
       |    </div>
       |    <esi:include src="http://assets/footer.html"/>
       |  </body>
       |</html
     """.stripMargin
  }

}

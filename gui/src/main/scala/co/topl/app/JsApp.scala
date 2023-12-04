package co.topl.app

// import com.raquo.app.JsRouter.*
import com.raquo.laminar.api.L._
import org.scalajs.dom

object JsApp {
  
  def main(args: Array[String]): Unit  = {
    // Scala.js outputs to the browser dev console, not the sbt session
    // Always have the browser dev console open when developing web UIs.
    println("-- Scala.js app start --")

    // Find the div to render the app into. It's defined in index.html
    lazy val container = dom.document.getElementById("root")

    lazy val networkVar = Var("934B1900")

    lazy val appElement = new TxComponent(networkVar).component
    render(container, appElement)
  }

}

package controllers

import java.text._
import java.sql._

import play._
import play.mvc._

import models._

object Application extends Controller {

  import views.Application._

  def index = {

    val formatter = new DecimalFormat("$#,###");
    def curr(value: Int): String = formatter.format(value)

    def f(expenses: List[MonthlyExpense], i: Int): String = {
      expenses.find(_.month == i) match {
        case Some(e) if e.amount > 0 => curr(e.amount)
        case _ => "&nbsp;"
      }
    }

    val expenses = 
      List(
        MonthlyExpense("a", 1, 25)
        , MonthlyExpense("b", 1, 50)
        , MonthlyExpense("b", 2, 50)
        , MonthlyExpense("a", 2, 150)
        , MonthlyExpense("c", 2, 5000)
        , MonthlyExpense("b", 3, 5000)
      )
        
    val monthlyExpenses = 
      expenses
      .groupBy { _.name}
      .toList
      .sortWith {(a, b) => a._2.map(_.amount).sum > b._2.map(_.amount).sum}

    val monthlyTotals =
      expenses
      .groupBy { _.month }
      .toList
      .sortWith {(a, b) => a._1 < b._1}
      .map {case (a, b) => b.map {_.amount}.sum}

    html.index(monthlyExpenses, monthlyTotals, f _, curr _)

      /*
    val conn = play.db.DB.datasource.getConnection
    var ps: PreparedStatement = null;
    var rs: ResultSet = null;
    try {
      val sql = "select name, mm, sum(value_num) from public.transactions_tree " +
      "where account_type = 'EXPENSE' and yyyy = '2011' group by name, mm " +
      "order by mm desc";
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery;

      val buffer = new scala.collection.mutable.ArrayBuffer[MonthlyExpense]();
      while (rs.next) {
        val name = rs.getString("name");
        val month = rs.getInt("mm");
        val amount = rs.getInt("sum");
        buffer += MonthlyExpense(name, month, amount)
      }

      val monthlyExpenses = buffer.toList.groupBy { _.name}

      html.index(monthlyExpenses, f _)
    } catch {
      case e: Exception => {
        e.printStackTrace();
        if (conn != null) try { conn.close } catch { case _ =>  }
        if (ps != null) try { ps.close } catch { case _ =>  }
        if (rs != null) try { rs.close } catch { case _ =>  }
      }
    }
    */
  }
}

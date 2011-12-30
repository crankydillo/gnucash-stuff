package controllers

import java.text._
import java.sql._

import play._
import play.mvc._

import models._

object Application extends Controller {

  import views.Application._

    val formatter = new DecimalFormat("$#,###");
    def curr(value: Int): String = formatter.format(value)

  def index = {

    def f(expenses: List[MonthlyExpense], i: Int): String = {
      expenses.find(_.month == i) match {
        case Some(e) if e.amount > 0 => curr(e.amount)
        case _ => "&nbsp;"
      }
    }

    def transLink(exp: String, month: Int) = 
      "transaction-list?exp=" + java.net.URLEncoder.encode(exp) + "&mo=" + month

    /*
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
    */

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

      val expenses = buffer.toList

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

      html.index(monthlyExpenses, monthlyTotals, f _, curr _, transLink _)
    } catch {
      case e: Exception => {
        e.printStackTrace();
        if (conn != null) try { conn.close } catch { case _ =>  }
        if (ps != null) try { ps.close } catch { case _ =>  }
        if (rs != null) try { rs.close } catch { case _ =>  }
      }
    }
  }

  def transactionList = {
    val exp = params.get("exp");
    val month = params.get("mo").toInt;

    val conn = play.db.DB.datasource.getConnection
    var ps: PreparedStatement = null;
    var rs: ResultSet = null;
    try {
      val sql = "select description, memo, value_num from public.transactions_tree " +
      "where account_type = 'EXPENSE' and yyyy = '2011' and name = ? and mm = ?";
      ps = conn.prepareStatement(sql);
      ps.setString(1, exp);
      val monthString =
        if (month < 10) "0" + month
        else month + ""
      ps.setString(2, monthString)
      rs = ps.executeQuery;

      val buffer = new scala.collection.mutable.ArrayBuffer[Transaction]();
      while (rs.next) {
        val desc = rs.getString("description");
        val memo = rs.getString("memo");
        val amount = rs.getInt("value_num");
        buffer += Transaction(desc, memo, amount)
      }

      val transactions = buffer.toList

      html.transactionlist(exp, month, transactions, curr _)
    } catch {
      case e: Exception => {
        e.printStackTrace();
        if (conn != null) try { conn.close } catch { case _ =>  }
        if (ps != null) try { ps.close } catch { case _ =>  }
        if (rs != null) try { rs.close } catch { case _ =>  }
      }
    }
  }
}

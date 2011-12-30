package models

case class MonthlyExpense(name: String, month: Int, amount: Int)

case class Transaction(desc: String, memo: String, amount: Int)//, date: java.util.Date)

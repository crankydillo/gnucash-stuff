-- I'm not taking credit for this.  I searched for how to do spreadsheet-based
-- expense reporting using gnucash data.  Something in those results led me to
-- this view.  I'm not sure how much, if any, of it I've modified.  Hopefully,
-- I'm not stepping on anyone's toes.

SELECT
accounts.account_type,
(SELECT a.name FROM accounts_tree a WHERE ((a.guid)::text = (accounts_tree.lev_guid1)::text)) AS level1,
(SELECT a.name FROM accounts_tree a WHERE ((a.guid)::text = (accounts_tree.lev_guid2)::text)) AS level2,
(SELECT a.name FROM accounts_tree a WHERE ((a.guid)::text = (accounts_tree.lev_guid3)::text)) AS level3,
(SELECT a.name FROM accounts_tree a WHERE ((a.guid)::text = (accounts_tree.lev_guid4)::text)) AS level4,
(SELECT a.name FROM accounts_tree a WHERE ((a.guid)::text = (accounts_tree.lev_guid5)::text)) AS level5,
accounts.name,
to_char(transactions.post_date, 'YYYY-MM-DD'::text) AS date,
to_char(transactions.post_date, 'YYYY-MM'::text) AS yearmo,
to_char(transactions.post_date, 'YYYY'::text) AS yyyy,
to_char(transactions.post_date, 'MM'::text) AS mm,
to_char(transactions.post_date, 'DD'::text) AS dd,
transactions.num,
transactions.description,
splits.memo,
((splits.quantity_num)::numeric / (splits.quantity_denom)::numeric) AS quantity_num,
((splits.value_num)::numeric / (splits.value_denom)::numeric) AS value_num
FROM accounts, accounts_tree, splits, transactions
WHERE
(
    (
        ((splits.account_guid)::text = (accounts.guid)::text)
        AND ((splits.account_guid)::text = (accounts_tree.guid)::text)
    )
    AND ((splits.tx_guid)::text = (transactions.guid)::text)
)
;
;

select a.dwAccountID as AccountID, sAccountClubID as AccountName, bBlocked as Blocked, nBlockReason as BlockReason, i.nItemID as ItemID, SUM(i.nNumber) as ItemCount, a.nDonationPoint as DonationPoint, a.nAccountCash as AccountCash
FROM `schema0`.`itemslotbundle` i
INNER JOIN `schema0`.`character` c ON i.dwcharacterid = c.dwcharacterid
INNER JOIN `schema`.`account` a ON a.dwaccountid = c.dwaccountid
INNER JOIN `schema0`.`trunkbundle` t ON t.dwaccountid = a.dwaccountid AND t.nitemid = i.nitemid
WHERE i.nItemID = 2049785
GROUP BY a.dwAccountID
order by ItemCount desc
limit 100
select a.dwAccountID, a.sAccountClubID, c.dwCharacterID, i.nItemID, i.*, t.* FROM
(`schema0`.`itemslotequip` i INNER JOIN `schema0`.`trunkequip` t) INNER JOIN (SELECT dwCharacterID, dwAccountID FROM `schema0`.`character`) c ON c.dwcharacterid = i.dwcharacterid INNER JOIN (SELECT dwAccountID, sAccountClubID, nDonationPoint, nAccountCash, nBlockReason FROM `schema`.`account`) a ON a.dwaccountid = c.dwaccountid AND a.nBlockReason = 0 INNER JOIN `schema0`.`trunkequip` tt ON tt.dwaccountid = a.dwaccountid
WHERE a.sAccountclubid = "Grazzle"
GROUP BY i.nPosition, t.nPosition
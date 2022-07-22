SELECT ah.liSN, ah.dwAuctionID as AuctionID, c.dwCharacterID as CharacterID, ah.dwAccountID as AccountID, a.sAccountClubID as AccountName, ah.nState as AuctionState, ah.nPrice as Price, ah.nItemID as ItemID, ah.nCount as Quantity, NULL as SPACER_COL, Traded_AccountID, Traded_AccountName, AuctionState_2, from_unixtime(ah.ftDate/1000) as AuctionItem_Created, ah2.AuctionItem_Sold FROM
schema0.auctionhistory ah
INNER join (SELECT dwCharacterID, dwAccountID FROM schema0.character) c ON c.dwcharacterid = ah.dwcharacterid
INNER join (SELECT dwAccountID, sAccountClubID FROM schema.account) a ON a.dwaccountid = c.dwaccountid
INNER join (SELECT dwAuctionID, dwAccountID, nState as AuctionState_2, from_unixtime(ftDate/1000) as AuctionItem_Sold FROM schema0.auctionhistory) ah2 ON ah.dwauctionid = ah2.dwauctionid
INNER join (SELECT sAccountClubID as Traded_AccountName, dwAccountID as Traded_AccountID FROM schema.account) a2 ON ah2.dwaccountid = Traded_AccountID OR ah.dwaccountid = Traded_AccountID
WHERE a.sAccountClubID = "gofurkle"
group by ah.nIncrementKey
order by AuctionID, AuctionItem_Sold, AuctionItem_Created desc
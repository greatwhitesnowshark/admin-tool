SELECT liSN, AuctionID, State, AccountID, AccountName, ItemID, Quantity, Price, AuctionTimestamp, CharacterID, CharacterName FROM
(select liSN, dwAuctionID as AuctionID, nState as State, nPrice as Price, a.dwAccountID as AccountID, a.sAccountClubID as AccountName, c.dwCharacterID as CharacterID, sCharacterName as CharacterName, nItemID as ItemID, nCount as Quantity, FROM_UNIXTIME(ftDate/1000) as DateTimeStamp from schema0.auctionhistory ah
INNER join (SELECT dwAccountID, dwCharacterID, sCharacterName FROM schema0.character) c ON c.dwcharacterid = ah.dwcharacterid
INNER join (SELECT dwAccountID, sAccountClubID FROM schema.account) a ON a.dwaccountid = c.dwaccountid) auctionview
INNER join (SELECT liSN as liSN2, dwAuctionID as AuctionID2, dwAccountID as BuyerAccountID FROM schema0.auctionhistory) ahh ON ahh.AuctionID2 = AuctionID
WHERE AccountID = 26402
UNION ALL
SELECT liSN, AuctionID, State, AccountID, AccountName, ItemID, Quantity, Price, AuctionTimestamp, CharacterID, CharacterName FROM
(select liSN, dwAuctionID as AuctionID, nState as State, nPrice as Price, a.dwAccountID as AccountID, a.sAccountClubID as AccountName, c.dwCharacterID as CharacterID, sCharacterName as CharacterName, nItemID as ItemID, nCount as Quantity, FROM_UNIXTIME(ftDate/1000) as DateTimeStamp from schema0.auctionhistory ah
INNER join (SELECT dwAccountID, dwCharacterID, sCharacterName FROM schema0.character) c ON c.dwcharacterid = ah.dwcharacterid
INNER join (SELECT dwAccountID, sAccountClubID FROM schema.account) a ON a.dwaccountid = c.dwaccountid) auctionview
INNER join (SELECT liSN as liSN2, dwAuctionID as AuctionID2, dwAccountID as BuyerAccountID FROM schema0.auctionhistory) ahh ON ahh.AuctionID2 = AuctionID AND ahh.BuyerAccountID = 26402
WHERE AccountID != 26402
ORDER BY DateTimeStamp, AuctionID
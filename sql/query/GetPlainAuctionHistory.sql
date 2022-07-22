SELECT liSN, dwAuctionID as AuctionID, dwAccountID as AccountID, nState as AuctionState, nPrice as Price, nItemID as ItemID, nCount as Quantity, from_unixtime(ftDate/1000) as TimePosted FROM schema0.auctionhistory
WHERE dwAccountID = 26402
ORDER BY dwAuctionID